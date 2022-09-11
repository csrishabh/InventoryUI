package com.cargo.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Order implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4227413197152424506L;

	@Id
	private String id;
	
	@DBRef
	@CascadeSave
	private List<Transction> transctions = new ArrayList<>();
	
	private String userId;
	
	private Date date;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Transction> getTransctions() {
		return transctions;
	}

	public void setTransctions(List<Transction> transctions) {
		this.transctions = transctions;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
