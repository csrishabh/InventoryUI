package com.cargo.document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Setter
@Getter
public class Product implements Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	private String id;
	private String name;
	@Transient
	private double alert;
	private long alertBack;
	private Unit unit;
	@Transient
	private double lastPrice;
	private long lastPriceBack;
	@Transient
	private double qtyAbl;
	private long qtyAblBack;
	private String lstAddBy;
	private Date lstAddDate;
	private String lstAdtBy;
	private Date lstAdtDate;
	private List<Carted> carted;
	private List<Assigned> assigned;
	private boolean enabled;

}
