package com.cargo.repo;

import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.cargo.document.Transction;

public interface TransctionRepoCustom {
	
	public List<Transction> getAuditPandingTransction( Map<String, Object> map);
	
	List<Document> getTransctionByUser(Map<String, Object> map,String userID);
	
	List<Document> getAllTransction(Map<String, Object> map);
	
	Transction updateTransction(Transction t);
	
	Transction deleteTransction(Transction t);

}
