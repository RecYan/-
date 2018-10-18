package com.ailk.gis.coord.mosaic;


import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Administrator on 2018/9/25.
 * 查询es中的相关数据
 */
public class QueryEsData {

    private static TransportClient client;
    private static String elasticIp = "XXXX";
    private static int elasticPort = 9320;
    private static BufferedReader br;
    private static Object location;

    /**
     * 初始化ElasticSearch对象
     */
    public static void init() throws UnknownHostException {
        //通过 setting对象来指定集群配置信息
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", "XXX")//指定集群名称
                .put("client.transport.sniff", true)//启动嗅探功能
                .build();

        client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticIp), elasticPort));
        System.out.println("连接建立成功");
    }

    /**
     * 关闭连接
     */
    public static void close() {
        //on shutdown 断开集群
        client.close();
    }



    /**
     * 返回es中所有的数据
     *
     */
    public static String queryAllData(String indexName) {

        //构造查询对象
        QueryBuilder builder = QueryBuilders.matchAllQuery();
        //搜索结果存入SearchResponse
        SearchResponse searchResponse = client.prepareSearch(indexName)
                .setQuery(builder) //设置查询器
                .setScroll(TimeValue.timeValueMinutes(8)) //游标维持多长时间
                .get();

        StringBuffer buffer = new StringBuffer();
        while(true){
            for (SearchHit hit : searchResponse.getHits()) {
                System.out.println(hit.getSourceAsString());
                buffer.append(hit.getSourceAsString()+"\n");
            }
            searchResponse = client.prepareSearchScroll(searchResponse.getScrollId())
                    .setScroll(TimeValue.timeValueMinutes(8))
                    .execute().actionGet();
            if (searchResponse.getHits().getHits().length == 0) {
                break;
            }
        }

        return buffer.toString();
    }

    /**
     * 根据传入的经纬度信息 查询相关信息
     */
    public static List<HashMap<String, Object>> qureyES(double longtitude, double latitude) {

        String index = "js_rsrp_coverage_grid";
        String type = "js_rsrp_coverage_grid";
        double RSRPAvg = 0.00; //RSRP平均值初始化
        ArrayList<Double> list = new ArrayList<>();

        ArrayList<HashMap<String, Object>> resultList = new ArrayList<>(); //存放查询的结果

        //查询的限定范围
        double minX = longtitude - 0.002;
        double maxX = longtitude + 0.002;
        double minY = latitude - 0.0018;
        double maxY = latitude + 0.0018;

        //预查询准备 -- 待查的索引信息和类型
        SearchRequestBuilder srb = client.prepareSearch(index).setTypes(type);

        //准备范围查询 过滤条件中,must相当于and,should相当于or,must_not相当于not
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        qb.must(QueryBuilders.rangeQuery("left_up_longitude").gt(String.valueOf(minX)))
            .must(QueryBuilders.rangeQuery("left_up_latitude").lt(String.valueOf(maxY)))
            .must(QueryBuilders.rangeQuery("right_down_longitude").lt(String.valueOf(maxX)))
            .must(QueryBuilders.rangeQuery("right_down_latitude").gt(String.valueOf(minY)));

        SearchResponse searchResponse = srb.setQuery(qb)
                .execute()
                .actionGet();

        //处理查询结果
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHists = hits.getHits();
        System.out.println("查询共命中：" + hits.getTotalHits() + " 条");

        for(SearchHit hit : searchHists) {
            //取出对应的RSRP值 存入list
            double RSRP = Double.parseDouble(hit.getSource().get("RSRP").toString());
            list.add(RSRP);
            System.out.println(hit.getSourceAsString());

            //取需要的坐标点
            String left_up_longitude = (String) hit.getSource().get("left_up_longitude").toString();
            String left_up_latitude = (String) hit.getSource().get("left_up_latitude").toString();
            String right_down_longitude = (String) hit.getSource().get("right_down_longitude").toString();
            String right_down_latitude = (String) hit.getSource().get("right_down_latitude").toString();

            HashMap<String, Object> map = new HashMap<>();
            map.put("left_up_longitude", left_up_longitude);
            map.put("left_up_latitude", left_up_latitude);
            map.put("right_down_longitude", right_down_longitude);
            map.put("right_down_latitude", right_down_latitude);
            map.put("RSRP", hit.getSource().get("RSRP"));
            resultList.add(map);

        }
        //计算RSRP平均值
        for(int i=0; i<list.size(); i++) {
            RSRPAvg = (RSRPAvg + list.get(i)) / (list.size());
        }
        //保留小数点后两位--四舍五入
        BigDecimal b = new BigDecimal(RSRPAvg);
        RSRPAvg = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        HashMap<String, Object> map = new HashMap<>();
        map.put("RSRPAvg", RSRPAvg);
        resultList.add(map);

        return resultList;
    }

    public static void main(String[] args) {
        try {
            // 初始化连接
            init();

            String indexName = "js_rsrp_coverage_grid";//索引名称

            /*String data = queryAllData(indexName);
            System.out.println(data);*/
            //queryTest(indexName);
            System.out.println(qureyES(119.5885512, 32.0039513));

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally{
            close();
        }

    }
}
