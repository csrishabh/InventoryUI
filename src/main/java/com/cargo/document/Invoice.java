package com.cargo.document;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Getter @Setter
public class Invoice {
	
	@Transient
	public static final String SEQUENCE_NAME = "invoice_sequence"; 
	
	@Id
	String id;
	long refId;
	long amount;
	long adjustment;
	boolean isCancelled;
	boolean isCurrent;
	Date createdDate;
	Date updateDate;
	String updateBy;
	String createdBy;
	String issuer;
	String note;
	String refNo;
	
}
