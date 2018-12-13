package com.ailk.gis.InsertIntoES;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;

/**
从数据库读取相应字段 并写入es
*/
public class InsertIntoEsForRRU {

    private static Client client = null;

    //集团数据库连接
    private static String drive = "oracle.jdbc.driver.OracleDriver";
    private static String dburl = "XXXX";
    private static String DBUSER = "XX";
    private static String password = "XXX";



    private static BulkRequestBuilder bulkRequest = null;
    private static int respcnt = 0;
    private static int count = 0;
    private static Connection conn = null;


    public static void main(String[] args){
        try {
            Class.forName(drive);
            conn = DriverManager.getConnection(dburl, DBUSER, password); // 连接数据库
            initClient();
            CreateIndexFile();
            addIndex();
            // handingException();
            System.out.println("============finish!");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initClient() throws UnknownHostException {
        // 按集群名称创建
        /**
         * clusterName需要跟elasticsearch.yml里的clusterName相同
         */
        Settings settings = Settings.builder().put("cluster.name", "XXX")// 集团XXX  测试：ailk_escluster
                .build();

        InetAddress inetAddress = InetAddress.getByName("XXXX"); //集团 XXXX  测试：192.168.74.189
        client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(inetAddress, 9320)); //集团端口 XXX  测试端口: 9320
        bulkRequest = client.prepareBulk();
    }

    private static void CreateIndexFile() throws IOException {
        XContentBuilder mapping = XContentFactory.jsonBuilder()
                .startObject()
                .startObject("properties") //下面是设置文档列属性。
                .startObject("objectid").field("type", "string").endObject()
                .startObject("equipment_status").field("type", "string").endObject()
                .startObject("int_id").field("type", "string").endObject()
                .startObject("is_indoor_source").field("type", "string").endObject()
                .startObject("is_invented").field("type", "string").endObject()
                .startObject("is_share_unicom").field("type", "string").endObject()
                .startObject("lifecycle_status").field("type", "string").endObject()
                .startObject("related_enb_id").field("type", "string").endObject()
                .startObject("related_enb_int_id").field("type", "string").endObject()
                .startObject("related_sit_int_id").field("type", "string").endObject()
                .startObject("room_int_id").field("type", "string").endObject()
                .startObject("rru_code").field("type", "string").endObject()
                .startObject("rru_grade").field("type", "string").endObject()
                .startObject("vendor_id").field("type", "string").endObject()
                .startObject("vendor_name").field("type", "string").endObject()
                .startObject("area_id_2").field("type", "string").endObject()
                .startObject("area_id_3").field("type", "string").endObject()
                .startObject("latitude").field("type", "string").endObject()
                .startObject("longitude").field("type", "string").endObject()
                .startObject("location").field("type", "geo_point").endObject()
                .endObject()
                .endObject();
        CreateIndexRequestBuilder prepareCreate = client.admin().indices().prepareCreate("gp_lte_rru"); //索引 索引type
        prepareCreate.addMapping("gp_lte_rru", mapping).execute().actionGet();

        System.out.println("创建成功！");
    }

    private static void addIndexFile(ResultSet result) {
        IndexRequestBuilder resq = null;
        try {
            XContentBuilder jsonBuild = XContentFactory.jsonBuilder();
            jsonBuild.startObject()
                    .field("objectid ",result.getInt("OBJECTID"))
                    .field("equipment_status", result.getString("EQUIPMENT_STATUS"))
                    .field("int_id", result.getString("INT_ID"))
                    .field("is_indoor_source", result.getString("IS_INDOOR_SOURCE"))
                    .field("is_invented", result.getString("IS_INVENTED"))
                    .field("is_share_unicom", result.getString("IS_SHARE_UNICOM"))
                    .field("lifecycle_status", result.getString("LIFECYCLE_STATUS"))
                    .field("related_enb_id", result.getString("RELATED_ENB_ID"))
                    .field("related_enb_int_id", result.getString("RELATED_ENB_INT_ID"))
                    .field("related_sit_int_id", result.getString("RELATED_SIT_INT_ID"))
                    .field("room_int_id", result.getString("ROOM_INT_ID"))
                    .field("rru_code", result.getString("RRU_CODE"))
                    .field("rru_grade", result.getString("RRU_GRADE"))
                    .field("vendor_id", result.getString("VENDOR_ID"))
                    .field("vendor_name", result.getString("VENDOR_NAME"))
                    .field("area_id_2", result.getString("AREA_ID_2"))
                    .field("area_id_3", result.getString("AREA_ID_3"))
                    .field("longitude", result.getDouble("LONGITUDE"))
                    .field("latitude", result.getDouble("LATITUDE"))
                    .startArray("location").value(result.getDouble("LONGITUDE")).value(result.getDouble("LATITUDE")).endArray()
                    .endObject();
            count++; //id自动增长
            respcnt++;
            resq = client.prepareIndex("gp_lte_enb", "gp_lte_enb", String.valueOf(count)).setSource(jsonBuild);
            bulkRequest.add(resq);
            if (respcnt % 1000 == 0) {
                try {
                    bulkRequest.execute();
                    respcnt = 0;
                    bulkRequest = client.prepareBulk();
                } catch (Exception e) {
                    try {
                        Thread.sleep(120000);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    bulkRequest.execute();
                    respcnt = 0;
                    bulkRequest = client.prepareBulk();
                    e.printStackTrace();
                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SQLException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

    }

    private static void addIndex()  {
        try {
            action();
            if (respcnt>0){
                bulkRequest.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void action()  {
        PreparedStatement pstmt;
        try {
            String sql = "SELECT OBJECTID, EQUIPMENT_STATUS, INT_ID, IS_INDOOR_SOURCE, IS_INVENTED, IS_SHARE_UNICOM, LIFECYCLE_STATUS, RELATED_ENB_ID, RELATED_ENB_INT_ID, RELATED_SIT_INT_ID, ROOM_INT_ID, RRU_CODE, RRU_GRADE, VENDOR_ID, VENDOR_NAME, AREA_ID_2, AREA_ID_3, LONGITUDE, LATITUDE FROM GP_LTE_RRU";
            pstmt = conn.prepareStatement(sql);
            ResultSet result = pstmt.executeQuery();
            while (result.next()) {
                addIndexFile(result);
            }
            result.close();
            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private static void handingException(){
        for (int i = 0; i < list.size(); i++) {
            bulkRequest.add((IndexRequestBuilder) list.get(i));
        }
        if (list.size()>0){
            bulkRequest.execute();
        }
    }*/

}

