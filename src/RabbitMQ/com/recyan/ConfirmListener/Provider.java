package com.recyan.ConfirmListener;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

/**
 * Created by Yan_Jiang on 2018/12/17.
 * confirm 确认机制
 *
 */
public class Provider {

    public static void main(String[] args) throws Exception {

        // 1 获取连接工厂
        ConnectionFactory connectionFactory = new ConnectionFactory();

        connectionFactory.setHost("192.168.80.100");
        connectionFactory.setPort(5672);
        connectionFactory.setVirtualHost("/");

        //2 获取连接
        Connection connection = connectionFactory.newConnection();

        //3 获取信道
        Channel channel = connection.createChannel();

        //4 开启确认模式
        channel.confirmSelect();

        //5 设置交换机相关参数
        String ExchangeName = "test_confirm_exchange";
        String routingKey = "confirm";

        //发送消息
        String msg = "test confirm....";
        channel.basicPublish(ExchangeName, routingKey, null, msg.getBytes());

        //channel 添加监听
        channel.addConfirmListener(new ConfirmListener() {
            @Override
            public void handleAck(long deliveryTag, boolean multiple) throws IOException {
                System.err.println("------------- ack --------");
            }

            @Override
            public void handleNack(long deliveryTag, boolean multiple) throws IOException {

                // 消息未送达
                System.err.println("------------- no ack --------");
            }
        });

    }
}
