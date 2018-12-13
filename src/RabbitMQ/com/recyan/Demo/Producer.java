package com.recyan.Demo;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
/**
 * Created by Yan_Jiang on 2018/12/13.
 * rabbitmq 生产者示例
 */
public class Producer {


    public static void main(String[] args) throws Exception {

        //1 创建一个ConnectionFactory, 并进行配置
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.80.100");
        connectionFactory.setPort(5672);
        connectionFactory.setVirtualHost("/");

        //2 通过连接工厂创建连接
        Connection connection = connectionFactory.newConnection();

        //3 通过connection创建一个Channel
        Channel channel = connection.createChannel();

        //4 通过Channel发送数据
        for(int i=0; i < 5; i++){
            String msg = "Hello RabbitMQ!";
            //1 exchange   2 routingKey
            /**
             * 这里 默认的exchange 为 AMQPDefault
             * 路由规则为 若有与routingkey同名的消息队列 则可以互相通信
             * The default exchange is implicitly bound to every queue, with a routing key equal to the queue name.
             * It is not possible to explicitly bind to, or unbind from the default exchange. It also cannot be deleted.
             */
            channel.basicPublish("", "test", null, msg.getBytes());
        }

        //5 关闭相关的连接
        channel.close();
        connection.close();


    }



}
