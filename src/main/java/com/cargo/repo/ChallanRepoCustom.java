package com.cargo.repo;

import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.cargo.document.Challan;

public interface ChallanRepoCustom {
	
	
	public long getTotalPaidAmountOnInvoice(long invoiceId);

	
	public Challan getLatestChallanOnInvoice(long invoiceId);
	
	public List<Document> getAllActiveChallanOnInvoice(long invoiceId);
	
	public List<Document> getChallanList(Map<String, Object> filters);
}
