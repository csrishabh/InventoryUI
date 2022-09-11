package com.cargo.document;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Setter @Getter
public class User {
	
	@Id
	private String id;
	@Indexed(unique = true, direction = IndexDirection.DESCENDING, dropDups = true)
	private String username;
	private String password;
	private String fullname;
	private String mobileNo;
	private String address;
	private boolean enabled;
	private Set<String> roles;
	private String gstNo;
	
}
