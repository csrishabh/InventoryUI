package com.cargo.service;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cargo.document.AppResponse;
import com.cargo.document.Invoice;
import com.cargo.repo.ChallanRepo;
import com.cargo.repo.InvoiceRepo;
import com.cargo.repo.SequenceGenerator;
import com.cargo.utility.Config;
import com.cargo.utility.StringConstant;

@Service
public class InvoiceService {

	@Autowired
	InvoiceRepo invoiceRepo;
	
	@Autowired
	ChallanRepo challanRepo;
	
	@Autowired
	private SequenceGenerator sequenceGenerator;
	
	public AppResponse<Void> addInvoice(Invoice invoice){
		
		AppResponse<Void> response = new AppResponse<>();
		try {
			if(invoice.getAmount()!=0 && null != invoice.getIssuer() && null != invoice.getCreatedDate() && null != invoice.getRefNo()) {
				if(invoiceRepo.isDuplicateInvoice(invoice.getRefNo(), invoice.getCreatedDate(), invoice.getIssuer())) {
					response.setSuccess(false);
					response.setMsg(Arrays.asList(StringConstant.INVOICE_DUPLICATE));
					return response;
				}
				String userId = SecurityContextHolder.getContext().getAuthentication().getName();
				long refId = sequenceGenerator.generateSequence(Invoice.SEQUENCE_NAME);
				invoice.setRefId(refId);
				invoice.setRefNo(invoice.getRefNo().toUpperCase());
				invoice.setCreatedDate(Config.fomatDate(invoice.getCreatedDate()));
				invoice.setCreatedBy(userId);
				invoice.setCurrent(true);
				invoice.setUpdateDate(new Date());
				invoiceRepo.save(invoice);
				response.setSuccess(true);
				response.setMsg(Arrays.asList(StringConstant.INVOICE_CREATED_SUCCESS));
				return response;
			}
			else {
				response.setSuccess(false);
				response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
				return response;
			}
		}
		catch(Exception e)
		{
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return response;
		}
		
		
	}
	
	public AppResponse<Void> updateInvoice(Invoice invoice){
		AppResponse<Void> response = new AppResponse<>();
		try {
			Invoice i = invoiceRepo.getCurrentInvoiceByrefId(invoice.getRefId());
			
			if(i != null){
				Invoice currInvoice = i;
				if(currInvoice.isCancelled() || invoice.getAdjustment() == 0 || !currInvoice.isCurrent()) {
					response.setSuccess(false);
					response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
					return response;
				}
				long totalAmountPaidOnInvoice = challanRepo.getTotalPaidAmountOnInvoice(currInvoice.getRefId());
				long remaingAmountAfterUpdate = currInvoice.getAmount()+invoice.getAdjustment()-totalAmountPaidOnInvoice;
				if(remaingAmountAfterUpdate < 0) {
					response.setSuccess(false);
					response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
					return response;
				}
				String userId = SecurityContextHolder.getContext().getAuthentication().getName();
				currInvoice.setCurrent(false);
				currInvoice.setUpdateBy(userId);
				currInvoice.setUpdateDate(new Date());
				
				Invoice updatedInvoice = new Invoice();
				updatedInvoice.setAmount(currInvoice.getAmount());
				updatedInvoice.setCreatedBy(currInvoice.getCreatedBy());
				updatedInvoice.setCreatedDate(currInvoice.getCreatedDate());
				updatedInvoice.setRefId(currInvoice.getRefId());
				updatedInvoice.setIssuer(currInvoice.getIssuer());
				updatedInvoice.setAdjustment(invoice.getAdjustment());
				updatedInvoice.setRefNo(currInvoice.getRefNo());
				updatedInvoice.setNote(currInvoice.getNote());
				updatedInvoice.setUpdateDate(new Date());
				updatedInvoice.setCurrent(true);

				invoiceRepo.save(currInvoice);
				invoiceRepo.save(updatedInvoice);
				response.setSuccess(true);
				response.setMsg(Arrays.asList(StringConstant.INVOICE_UPDATED_SUCCESS));
				return response;
			}
			else {
				response.setSuccess(false);
				response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
				return response;
			}
		}
		catch(Exception e)
		{
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return response;
		}
		
	}
	
	
	
	public AppResponse<Void> cancelInvoice(long refId){
		AppResponse<Void> response = new AppResponse<>();
		try {
			Invoice invoice = invoiceRepo.getCurrentInvoiceByrefId(refId);
			
			if(null!=invoice){
				Invoice currInvoice = invoice;
				if(currInvoice.isCancelled() || !currInvoice.isCurrent()) {
					response.setSuccess(false);
					response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
					return response;
				}
				long totalAmountPaidOnInvoice = challanRepo.getTotalPaidAmountOnInvoice(currInvoice.getRefId());
				
				if(totalAmountPaidOnInvoice > 0) {
					response.setSuccess(false);
					response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
					return response;
				}
				
				String userId = SecurityContextHolder.getContext().getAuthentication().getName();
				currInvoice.setCancelled(true);
				currInvoice.setUpdateDate(new Date());	
				currInvoice.setUpdateBy(userId);
				invoiceRepo.save(currInvoice);
				response.setSuccess(true);
				response.setMsg(Arrays.asList(StringConstant.INVOICE_CALCELED_SUCCESS));
				return response;
			}
			else {
				response.setSuccess(false);
				response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
				return response;
			}
		}
		catch(Exception e)
		{
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return response;
		}
		
	}
	
	public AppResponse<List<Document>> getInvoiceList(Map<String, Object> filters){
		
		AppResponse<List<Document>> response = new AppResponse<>();
		filters.put("isCurrent", "true");
		try {
			List<Document> invoiceList = invoiceRepo.getInvoiceList(filters);
			response.setSuccess(true);
			response.setData(invoiceList);
			return response;
		}
		catch(Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return response;
		}
		
	}
	
	public AppResponse<List<Document>> getInvoiceHistroy(long refId){
		
		
		AppResponse<List<Document>> response = new AppResponse<>();
		Map<String, Object> filters = new HashMap();
		filters.put("refId", refId);
		try {
			List<Document> invoiceList = invoiceRepo.getInvoiceHistory(refId);
			response.setSuccess(true);
			response.setData(invoiceList);
			return response;
		}
		catch(Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return response;
		}
		
	}
	
}
