package com.recyan.ReturnListener;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ReturnListener;
import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * Created by Yan_Jiang on 2018/12/17.
 * return 确认机制
 */
public class Producer {

	
	public static void main(String[] args) throws Exception {
		
		
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost("192.168.80.100");
		connectionFactory.setPort(5672);
		connectionFactory.setVirtualHost("/");
		
		Connection connection = connectionFactory.newConnection();
		Channel channel = connection.createChannel();
		
		String exchange = "test_return_exchange";
		String routingKey = "return.save";
		String routingKeyError = "abc.save";
		
		String msg = "Hello RabbitMQ Return Message";
		
		
		channel.addReturnListener(new ReturnListener() {
			@Override
			public void handleReturn(int replyCode, String replyText, String exchange,
					String routingKey, BasicProperties properties, byte[] body) throws IOException {
				
				System.err.println("---------handle  return----------");
				System.err.println("replyCode: " + replyCode);
				System.err.println("replyText: " + replyText);
				System.err.println("exchange: " + exchange);
				System.err.println("routingKey: " + routingKey);
				System.err.println("properties: " + properties);
				System.err.println("body: " + new String(body));
			}
		});
		/**
		 *
		 * ---------handle  return----------
		 replyCode: 312
		 replyText: NO_ROUTE
		 exchange: test_return_exchange
		 routingKey: abc.save
		 properties: #contentHeader<basic>(content-type=null, content-encoding=null, headers=null, delivery-mode=null,
		 priority=null, correlation-id=null, reply-to=null, expiration=null, message-id=null, timestamp=null, type=null,
		 user-id=null, app-id=null, cluster-id=null)
		 body: Hello RabbitMQ Return Message

		 */
		//mandatory -- true 生产端监听路由不可达的信息
		channel.basicPublish(exchange, routingKeyError, true, null, msg.getBytes());
		
		//channel.basicPublish(exchange, routingKeyError, true, null, msg.getBytes());
		
		
		
		
	}
}
