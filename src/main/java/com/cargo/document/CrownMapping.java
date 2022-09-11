package com.cargo.document;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class CrownMapping implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4662206118197372436L;
	
	@Id
	private String id;
	
	@DBRef
	private User vendor;
	
	private CROWNTYPE crown;
	
	private long price;
	
	private int version;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public User getVendor() {
		return vendor;
	}

	public void setVendor(User vendor) {
		this.vendor = vendor;
	}

	public CROWNTYPE getCrown() {
		return crown;
	}

	public void setCrown(CROWNTYPE crown) {
		this.crown = crown;
	}

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
}
