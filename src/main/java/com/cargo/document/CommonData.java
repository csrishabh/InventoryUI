package com.cargo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Getter @Setter
public class CommonData{

	@Id
	private String id;
	private int typeId;
	private String name;
	private String value;
	private boolean isDisabled;
}
