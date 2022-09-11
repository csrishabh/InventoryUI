package com.cargo.document;

public enum BillingType {
	
	PAID("Paid"),
	TO_PAY("To Pay"),
	CREDIT("Credit");
	
	private String name;

	private BillingType(String name) {

		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
