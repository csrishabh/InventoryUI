package com.cargo.document;

public enum Unit{

	KILOGRAM("Kg"), LITER("Lt"), PIECES("Pc");

	private String name;

	private Unit(String name) {

		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
