package com.cargo.repo;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.cargo.document.CaseSearchResult;

public interface CaseRepoCustom {
	
	public List<CaseSearchResult> findAllLatestCase(Map<String, Object> filters);
	
	public List<CaseSearchResult> findAllLatestCaseByUser(String user,Map<String, Object> filters);
	
	public List<CaseSearchResult> findLatestCase(String opdNo);
	
	public List<Document> findLateCaseCount();
	
	public List<CaseSearchResult> findAllLateCase() ;
	
	public List<CaseSearchResult> getVendorReport(Map<String, Object> filters);
	
	public boolean isPaidBefore(String OpdNo, Date bookingDate ,String vendorId, Date updateDate);
	

}
