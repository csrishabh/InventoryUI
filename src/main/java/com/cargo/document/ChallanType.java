package com.cargo.document;

public enum ChallanType {

	
	CASH("Cash"),
	INTERNET_BANKING("Internet Banking"),
	CHEQUE("Cheque");
	
	private String name;

	private ChallanType(String name) {

		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
