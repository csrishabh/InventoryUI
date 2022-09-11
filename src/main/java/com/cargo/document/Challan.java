package com.cargo.document;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Getter @Setter
public class Challan {
	
	@Transient
	public static final String SEQUENCE_NAME = "challan_sequence"; 
	
	@Id
	String id;
	long refId;
	long invoiceNo;
	long amount;
	boolean isCancelled;
	Date createDate;
	Date updateDate;
	String updateBy;
	String createdBy;
	String payBy;
	ChallanType type;
	String bankName;
	String refNo;
	String note;

}
