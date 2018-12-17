package com.recyan.ConfirmListener;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * Created by Yan_Jiang on 2018/12/17.
 * confirm 确认机制
 */
public class Consumer {

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

        String ExchangeName = "test_confirm_exchange";
        String routingKey = "confirm";
        String QueueName = "test_confirm_queue";

        //声明交换机 队列
        channel.queueDeclare(QueueName, true, false, false, null);
        channel.exchangeDeclare(ExchangeName, "topic", true, false,false, null);
        //队列 交换机绑定
        channel.queueBind(QueueName, ExchangeName, routingKey);

        //创建消费端
        QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
        channel.basicConsume(QueueName, true, queueingConsumer);

        //消费消息
        while(true) {
            QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
            String msg = new String(delivery.getBody());

            System.out.println("消费端：" + msg);

        }


    }
}
