package com.recyan.Dlx;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;

public class Consumer {

	
	public static void main(String[] args) throws Exception {
		
		
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost("192.168.80.100");
		connectionFactory.setPort(5672);
		connectionFactory.setVirtualHost("/");
		
		Connection connection = connectionFactory.newConnection();
		Channel channel = connection.createChannel();
		
		//普通队列 交换机设置
		String exchangeName = "test_dlx_exchange";
		String routingKey = "dlx.save";
		String queueName = "test_dlx_queue";

		Map<String, Object> arguments = new HashMap<>();
		arguments.put("x-dead-letter-exchange", "dlx.exchange");

		channel.exchangeDeclare(exchangeName, "topic", true, false, arguments);
		channel.queueDeclare(queueName, true, false, false, null);
		channel.queueBind(queueName, exchangeName, routingKey);

		//死信队列声明
		channel.exchangeDeclare("dlx.exchange", "topic", true, false, null);
		channel.queueDeclare("dlx.queue", true, false, false, null);
		channel.queueBind("dlx.queue", "dlx.exchange", "#");
		
		channel.basicConsume(queueName, true, new MyConsumer(channel));
		
		
	}
}
