package com.cargo.document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Getter @Setter
public class Consignment implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4902031113089445414L;

	@Transient
	public static final String SEQUENCE_NAME = "consignment_sequence";

	@Id
	private String id;
	private long refId;
	private String consignee;
	private String consignor;
	private BillingType billingType;
	private int paidBy;
	@Indexed
	private String biltyNo;
	private String des;
	private String contents;
	private String ewayNo;
	private Unit unit;
	private int totalParcel;
	private long weight;
	private long rate;
	private long discount;
	private long tax;
	private long remark1;
	private long remark2;
	private Date bookingDate;
	private boolean isDeliverd;
	private boolean isDeleted;
	private String remark;
	private List<String> maniFestRefNo;
	
}
