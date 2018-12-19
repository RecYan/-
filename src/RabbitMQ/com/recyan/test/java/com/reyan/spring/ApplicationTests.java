package com.reyan.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

	@Test
	public void contextLoads() {
	}
	
	@Autowired
	private RabbitAdmin rabbitAdmin;
	
	@Test
	public void testAdmin() throws Exception {

		rabbitAdmin.declareExchange(new DirectExchange("test.direct", false, false));
		rabbitAdmin.declareQueue(new Queue("test.direct.queue", false));

		rabbitAdmin.declareBinding(new Binding("test.direct.queue"
				,Binding.DestinationType.QUEUE, "test.direct.queue", "direct.save", null));

		rabbitAdmin.declareBinding(BindingBuilder
									.bind(new Queue("test.topic.queue", false))
									.to(new TopicExchange("test.topic.exchange", false, false))
									.with("topic.#"));

		//清空消息队列
		rabbitAdmin.purgeQueue("test.direct.queue", true);
	}


}
