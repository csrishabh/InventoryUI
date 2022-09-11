package com.cargo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;


@Document
@Getter @Setter
public class UnitMapping {
	
	@Id
	private String id;
	
	@DBRef
	private User company;
	
	private Unit unit;
	
	private long price;
	
	private int version;

}
