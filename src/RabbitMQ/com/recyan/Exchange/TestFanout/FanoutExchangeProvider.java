package com.recyan.Exchange.TestFanout;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * Created by Yan_Jiang on 2018/12/16.
 * fanoutExchange 生产端
 *   不处理任何路由键 性能最佳
 */
public class FanoutExchangeProvider {

    public static void main(String[] args) throws Exception {

        //1 创建ConnectionFactory
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.80.100");
        connectionFactory.setPort(5672);
        connectionFactory.setVirtualHost("/");

        //2 创建Connection
        Connection connection = connectionFactory.newConnection();
        //3 创建Channel
        Channel channel = connection.createChannel();
        //4 声明
        String exchangeName = "test_fanout_exchange";
        //5 发送
        for(int i = 0; i < 10; i ++) {
            String msg = "Hello World RabbitMQ 4 FANOUT Exchange Message ...";
            channel.basicPublish(exchangeName, "", null , msg.getBytes());
        }
        channel.close();
        connection.close();
    }
}
