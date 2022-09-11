package com.cargo.repo;

import java.util.List;

import com.cargo.document.CROWNTYPE;
import com.cargo.document.CrownMapping;

public interface CrownMappingRepoCustom {

	public List<CrownMapping> getCurrentCrownTypeByVendor(String vendorId , CROWNTYPE... type);
	
	public CrownMapping getLatestCrownMappingByVendor(String vendorId, CROWNTYPE type);
	
	
}
