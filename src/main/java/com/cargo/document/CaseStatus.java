package com.cargo.document;

public enum CaseStatus {

	BOOKED("Booked"),INPROCESS("In Process"),TRIAL("Trial"),DELIVERD("Delivered"),INSERTION_DONE("Insertion Done"),REPEAT("Repeat"),CANCELED("Canceled");
	
	private String name;

	private CaseStatus(String name) {

		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
