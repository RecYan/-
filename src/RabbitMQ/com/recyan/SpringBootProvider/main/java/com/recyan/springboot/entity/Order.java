package com.recyan.springboot.entity;

import java.io.Serializable;

/**
发送实体类时 需要实现序列化接口  
消息发送的默认支持类型为： String byte[]
 */
public class Order implements Serializable {

	private String id;
	private String name;
	
	public Order() {
	}
	public Order(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	
}
