package com.cargo.document;

public enum CROWNTYPE {
	
	FULL_METAL_CERAMIC("Full Metal Creamic"),
	FULL_METAL_CERAMIC_PLUS("Full Metal Creamic Plus"),
	CERAMIC_FACING("Creamic Facing"), 
	METAL_FREE("Metal Free"), 
	TEMPORARY("Temporary"),
	REMOVEABLE_PARTIAL_DENTURE("Removeable Partial Denture"),
	COMPLETE_DENTURE_UPPER("Complete Denture Upper"),
	COMPLETE_DENTURE_LOWER("Complete Denture Lower"),
	NIGHT_GUARD("Night Gard"),
	METAL_CROWN("Metal Crown");
	
	private String name;

	private CROWNTYPE(String name) {

		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
