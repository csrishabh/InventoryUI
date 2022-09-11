package com.cargo.repo;

import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.cargo.document.Consignment;

public interface ConsignmentRepoCustom {
	
	public List<Document> searchConsignment(Map<String, Object> filters);
	
	public boolean isAnyConsignmentDeliverd(List<String> consignments);
	
	public boolean isDuplicateConsignment(String biltyNo);
	
	public boolean isConsignmentProcessed(String biltyNo);
	
	public void deleteConsignment(String biltyNo);
	
	public void setConsignmentDeliverd(List<String> consignments , String des);
	
	public void setConsignmentUnDeliverd(List<String> consignments, String des);
	
	public void updateManifest(List<String> consignments , String manifestNo, boolean isDeleted);
	
	public boolean isAnyConsignmentDeleted(List<String> consignments);
	
	public Consignment getConsignment(String biltyNo);

}
