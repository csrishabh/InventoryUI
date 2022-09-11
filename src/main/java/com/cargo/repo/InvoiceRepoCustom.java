package com.cargo.repo;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;

public interface InvoiceRepoCustom {

	List<Document> getInvoiceList(Map<String, Object> filters);
	
	List<Document> getInvoiceHistory(long refId);
	
	boolean isDuplicateInvoice(String refNumber, Date invoiceDate, String issuer);
}
