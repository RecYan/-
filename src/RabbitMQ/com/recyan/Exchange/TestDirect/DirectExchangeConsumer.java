package com.recyan.Exchange.TestDirect;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

public class DirectExchangeConsumer {

	public static void main(String[] args) throws Exception {
		
		
        ConnectionFactory connectionFactory = new ConnectionFactory() ;  
        
        connectionFactory.setHost("192.168.80.100");
        connectionFactory.setPort(5672);
		connectionFactory.setVirtualHost("/");

        /**
         *  每隔3秒 自动重连
         */
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.setNetworkRecoveryInterval(3000);

        Connection connection = connectionFactory.newConnection();
        
        Channel channel = connection.createChannel();  
		//4 声明
		String exchangeName = "test_direct_exchange";
		String exchangeType = "direct";
		String queueName = "test_direct_queue";
		String routingKey = "test.direct";

        /** 表示声明了一个交换机
         * @param exchange the name of the exchange
         * @param type the exchange type
         * @param durable true if we are declaring a durable exchange (the exchange will survive a server restart)
         * @param autoDelete true if the server should delete the exchange when it is no longer in use
         * @param internal true if the exchange is internal, i.e. can't be directly
         * published to by a client.
         * @param arguments other properties (construction arguments) for the exchange
         */
		channel.exchangeDeclare(exchangeName, exchangeType, true, false, false, null);

        //表示声明了一个队列
		channel.queueDeclare(queueName, false, false, false, null);

        /** 建立一个绑定关系:
         * @param queue the name of the queue
         * @param exchange the name of the exchange
         * @param routingKey the routine key to use for the binding
         */
		channel.queueBind(queueName, exchangeName, routingKey);
		
        //durable 是否持久化消息
        QueueingConsumer consumer = new QueueingConsumer(channel);

        //参数：队列名称、是否自动ACK、Consumer
        channel.basicConsume(queueName, true, consumer);

        //循环获取消息  
        while(true){  
            //获取消息，如果没有消息，这一步将会一直阻塞  
            Delivery delivery = consumer.nextDelivery();  
            String msg = new String(delivery.getBody());    
            System.out.println("收到消息：" + msg);  
        } 
	}
}
