package com.ailk.gis.coord.mosaic;

import com.ailk.gis.geocoder.util.GeoPoint;
import net.sf.json.JSONObject;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018/9/25.
 * 读取ftpx下载到本地的csv文件的数据 并上传到ES中
 */
public class InsertEsData {

    private static TransportClient client;
    private static String elasticIp = "XXXX";
    private static int elasticPort = 9320;
    private static BufferedReader br;

    /**
     * 初始化ElasticSearch对象
     */
    public static void init() throws UnknownHostException {
        //通过 setting对象来指定集群配置信息
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "XXX")//指定集群名称
                /**
                 * 通过client.transport.sniff启动嗅探功能，这样只需要指定集群中的某一个节点(不一定是主节点)，
                 * 然后会加载集群中的其他节点，这样只要程序不停即使此节点宕机仍然可以连接到其他节点。
                 */
                .put("client.transport.sniff", true)//启动嗅探功能
                .build();

        client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticIp), elasticPort));
        System.out.println("连接建立成功");
    }

    /**
     * 使用es的帮助类
     */
    public static XContentBuilder createJson(Map<String,Object> map){
        // 创建json对象, 其中一个创建json的方式
        XContentBuilder source = null;
        try {
            source = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("City", map.get("City"))
                    .field("RSRP", map.get("RSRP"))
                    .field("Quality", map.get("Quality"))
                    .field("left_up_longitude", map.get("left_up_longitude"))
                    .field("left_up_latitude", map.get("left_up_latitude"))
                    .field("right_down_longitude", map.get("right_down_longitude"))
                    .field("right_down_latitude", map.get("right_down_latitude"))
                    .field("location", map.get("location"))
                    .endObject();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return source;
    }


    /**
     * 使用map创建json
     */
    public static Map<String, Object> createJson(double minx, double miny) {
        Map<String,Object> json = new HashMap<String, Object>();
        json.put("lon", minx);
        json.put("lat",miny);
        return json;
    }

    /**
     * 【描述】：  创建index，把其中的文档转化为json的格式存储
     * @param     　　<Node>:节点ip <port>:节点端口号，默认9320 <Index>:索引名 <Type>:索引类型 <ID>:操作对象的ID号
     * @return
     * @throws
     */
    public static void createIndex(String indexName, String type1) throws ElasticsearchException,IOException {

        //1:settings
        HashMap<String, Object> settings_map = new HashMap<String, Object>();
        //settings_map.put("number_of_shards", 5); //分片数量
        //新建索引
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("properties") //下面是设置文档列属性。
                .startObject("left_up_longitude").field("type", "double").endObject()
                .startObject("left_up_latitude").field("type", "double").endObject()
                .startObject("right_down_longitude").field("type", "double").endObject()
                .startObject("right_down_latitude").field("type", "double").endObject()
                .startObject("City").field("type", "string").endObject()
                .startObject("RSRP").field("type", "double").endObject()
                .startObject("Quality").field("type", "string").endObject()
                .startObject("location").field("type", "geo_point").endObject()
                .endObject()
                .endObject();
        CreateIndexRequestBuilder prepareCreate = client.admin().indices().prepareCreate(indexName);
        prepareCreate.setSettings(settings_map).addMapping(type1, mapping).execute().actionGet();
        System.out.println("创建成功！");
    }
    /**
     * 关闭连接
     */
    public static void close(){
        //on shutdown 断开集群
        client.close();
    }


    public static void BulkProcessor(List<Map<String,Object>> jsonList, String indexName, String typeName){
        try {
            // 创建BulkPorcessor对象
            BulkProcessor bulkProcessor = BulkProcessor.builder(client, new BulkProcessor.Listener() {

                public void beforeBulk(long paramLong, BulkRequest request) {
                    // TODO Auto-generated method stub
                    System.out.println("---尝试插入{}条数据---"+request.numberOfActions());
                }

                // 执行出错时执行
                public void afterBulk(long paramLong, BulkRequest request, Throwable failure) {
                    // TODO Auto-generated method stub
                    System.out.println("[es错误]---尝试插入数据失败---"+failure);
                }

                public void afterBulk(long paramLong, BulkRequest request, BulkResponse response) {
                    // TODO Auto-generated method stub
                    System.out.println("---尝试插入{}条数据成功---"+ request.numberOfActions());
                }

            })
                    // 1w次请求执行一次bulk
                    .setBulkActions(10000)
                    // 1gb的数据刷新一次bulk
                    .setBulkSize(new ByteSizeValue(500, ByteSizeUnit.MB))
                    // 固定5s必须刷新一次
                    .setFlushInterval(TimeValue.timeValueSeconds(5))
                    // 并发请求数量, 0不并发, 1并发允许执行
                    .setConcurrentRequests(1)
                    // 设置退避, 100ms后执行, 最大请求3次
                    .setBackoffPolicy(
                            BackoffPolicy.exponentialBackoff(TimeValue.timeValueMillis(100), 3))
                    .build();

            // 添加单次请求
            for(Map<String,Object> jsonM : jsonList){
                XContentBuilder json = (XContentBuilder) jsonM.get("json");
                String id = jsonM.get("id").toString();
                if(jsonM != null){
                    bulkProcessor.add(new IndexRequest(indexName,typeName,id).source(json));
                }
            }
            // 关闭

            bulkProcessor.awaitClose(10, TimeUnit.MINUTES);
            // 或者
            bulkProcessor.close();

        }catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void main( String[] args ) {
        //输入文件
        String in_fileDir1 = "C:\\gisTaskData\\0912\\";
        String in_fileName1 = "JS_RSRP_Coverage_Grid_20180912.csv";
        String indexName = "";//索引名
        String typeName  = "js_rsrp_coverage_grid";//类型名
        /*//读取输入参数
        if(args.length>=2){
            in_fileDir1 = args[0];
            in_fileName1 = args[1];
        }*/
        try {

            init();//建立连接

            String infileAllName1 = in_fileDir1 + in_fileName1; //单独一个文件 直接写死
            indexName = "js_rsrp_coverage_grid"; //索引名 -- 文件名 index名 必须小写 否则会报错
            File file1 = new File(infileAllName1);

            if(file1.exists()) {
                createIndex(indexName,typeName);
            }
            br = new BufferedReader(new InputStreamReader(new FileInputStream(file1),"GBK")); //文件中 中文乱码处理

            String data = null;
            int count = 0;
            if(count == 0) {
                br.readLine(); //保证从第二行开始读数据
            }
            List<Map<String,Object>> list = new ArrayList<>();
            while((data = br.readLine())!=null)
            {
                count++;
                String[] lineArr =  data.split(",");
                String City = lineArr[4]; //文件中的城市列索引
                //读取 指定城市
                if(!City.equals("null")&&!City.equals("")){

                    String RSRP = lineArr[5];
                    String Quality = lineArr[6];
                    double curMinLonD = Double.parseDouble(lineArr[1]);
                    double curMinLatD = Double.parseDouble(lineArr[0]);
                    double curMaxLonD = Double.parseDouble(lineArr[3]);
                    double curMaxLatD = Double.parseDouble(lineArr[2]);
//                    GeoPoint newPoint = MosaicID.getBD09MosaicFromWGS(curMinLonD, curMinLatD, curMaxLonD, curMaxLatD);
//                    double newX = newPoint.getX();
//                    double newY = newPoint.getY();
                    double newX = (curMinLonD + curMaxLonD) /2.000000; //取中心点
                    double newY = (curMinLatD + curMaxLatD) /2.000000; //取中心点
                    Map<String,Object> json = createJson(newX,newY);

                    Map<String,Object> map = new HashMap<String,Object>();
                    map.put("left_up_longitude", curMinLonD);
                    map.put("left_up_latitude", curMaxLatD);
                    map.put("right_down_longitude", curMaxLonD);
                    map.put("right_down_latitude", curMinLatD);
                    map.put("City", City);
                    map.put("RSRP", RSRP);
                    map.put("Quality", Quality);
                    map.put("location", json);

                    XContentBuilder source = createJson(map); //根据文件修改
                    Map<String,Object> jsonMap = new HashMap<String,Object>();
                    jsonMap.put("json", source);
                    jsonMap.put("id", String.valueOf(count));
                    list.add(jsonMap);
                    if(count%10000==0){
                        BulkProcessor(list,indexName,typeName);//批量提交
                        list.clear();
                    }
                }

            }
            BulkProcessor(list,indexName,typeName);//批量提交
            br.close();

            close();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }
}
