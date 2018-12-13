package com.recyan.ES.Test;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;

/**
 * Created by Yan_Jiang on 2018/10/11.
 * 客户端连接测试<非集群>
 */
public class Testcon {

    private static String host = "192.168.80.100"; //服务器地址
    private static int port = 9300; //javaAPI端口

    public static void main(String[] args) throws Exception {

        TransportClient client = new PreBuiltTransportClient(Settings.EMPTY) //Settings.EMPTY -- Setting 集群配置文件
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));

        System.out.println(client);
        client.close();

    }
}
