package com.cargo.repo;

import java.util.List;

import com.cargo.document.Unit;
import com.cargo.document.UnitMapping;

public interface UnitMappingRepoCustom {

	public List<UnitMapping> getCurrentUnitByCompany(String compamyId, Unit... type);

	public UnitMapping getLatestUnitMappingByCompany(String companyId, Unit type);

}
