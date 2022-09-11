package com.cargo.document;

public enum CaseSubStatus {
	
	NONE("None"),TRIAL("Trial"),REPEAT("Repeat");
	
	private String name;

	private CaseSubStatus(String name) {

		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
