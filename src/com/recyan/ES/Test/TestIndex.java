package com.recyan.ES.Test;

import com.google.gson.JsonObject;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.*;

import java.net.InetAddress;

/**
 * Created by Yan_Jiang on 2018/10/11.
 * es索引测试
 */
public class TestIndex {

    private static String host = "192.168.80.100"; //服务器地址
    private static int port = 9300; //javaAPI端口
    private static TransportClient client = null;

    @Before
    public void getClient() throws Exception{

        client = new PreBuiltTransportClient(Settings.EMPTY) //Settings.EMPTY -- Setting 集群配置文件
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
    }

    @After
    public void close() {

        if(client != null) {
            client.close();
        }
    }

    //索引 推荐
    @Test
    public void createIndex() throws Exception {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "java编程思想");
        jsonObject.addProperty("publicDate", "2012-11-11");
        jsonObject.addProperty("price", "100");

        IndexResponse response = client.prepareIndex("book", "java", "1")
                .setSource(jsonObject.toString(), XContentType.JSON) //XContentType: 指定解析类型
                .get();

        System.out.println("索引名称：" + response.getIndex());
        System.out.println("类型: " + response.getType());
        System.out.println("文档ID： " + response.getId());
        System.out.println("当前实例状态：" + response.status());

    }

    /**
     * 根绝文档ID属性 获取相关信息
     * @throws Exception
     */
    @Test
    public void testGet() throws Exception {

        GetResponse response = client.prepareGet("book", "java", "1").get();

        System.out.println(response.getSourceAsString());
    }


    /**
     * 根绝文档ID属性 修改相关信息
     * @throws Exception
     */
    @Test
    public void testUpdate() throws Exception {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", "java编程思想-update");
        jsonObject.addProperty("publicDate", "2012-11-11-update");
        jsonObject.addProperty("price", "100-update");

        UpdateResponse response = client.prepareUpdate("book", "java", "1")
                .setDoc(jsonObject.toString(), XContentType.JSON)
                .get();

        System.out.println("索引名称：" + response.getIndex());
        System.out.println("类型: " + response.getType());
        System.out.println("文档ID： " + response.getId());
        System.out.println("当前实例状态：" + response.status());
    }


    /**
     * 根绝文档ID属性 删除相关信息
     * @throws Exception
     */
    @Test
    public void testDelete() throws Exception {

        DeleteResponse response = client.prepareDelete("book", "java", "1").get();

        System.out.println("索引名称：" + response.getIndex());
        System.out.println("类型: " + response.getType());
        System.out.println("文档ID： " + response.getId());
        System.out.println("当前实例状态：" + response.status());


    }

}
