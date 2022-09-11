package com.cargo.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cargo.document.AppResponse;
import com.cargo.document.Challan;
import com.cargo.document.ChallanType;
import com.cargo.document.Invoice;
import com.cargo.repo.ChallanRepo;
import com.cargo.repo.InvoiceRepo;
import com.cargo.repo.SequenceGenerator;
import com.cargo.utility.Config;
import com.cargo.utility.StringConstant;

@Service
public class ChallanService {
	
	
	@Autowired
	private InvoiceRepo invoiceRepo;
	
	@Autowired
	private ChallanRepo challanRepo;
	
	@Autowired
	private SequenceGenerator sequenceGenerator;
	
	public AppResponse<Void> addChallan(Challan challan){
		
		AppResponse<Void> response = new AppResponse<>();
		
		try {
			if(challan.getInvoiceNo()==0 || challan.getAmount() ==0 || challan.getType()==null || challan.getCreateDate()==null) {
				response.setSuccess(false);
				response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
				return response;
			}
			
			else if(challan.getType() != ChallanType.CASH && (StringUtils.isEmpty(challan.getRefNo()) || StringUtils.isEmpty(challan.getBankName()))) {
				
				response.setSuccess(false);
				response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
				return response;
			}
			else {

				Invoice invoice = invoiceRepo.getCurrentInvoiceByrefId(challan.getInvoiceNo());
				
				if(null== invoice || invoice.isCancelled()) {
					response.setSuccess(false);
					response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
					return response;
				}
				
				
				long totalAmountPaidOnInvoice = challanRepo.getTotalPaidAmountOnInvoice(challan.getInvoiceNo());
				long remainingAmt = invoice.getAmount()+invoice.getAdjustment()- totalAmountPaidOnInvoice;
				
				if(remainingAmt < challan.getAmount()) {
					response.setSuccess(false);
					response.setMsg(Arrays.asList(StringConstant.CHALLAN_AMT_MORE_THEN_INVOICE));
					return response;
				}
				
				String userId = SecurityContextHolder.getContext().getAuthentication().getName();
				challan.setCreatedBy(userId);
				challan.setUpdateDate(new Date());
				challan.setCreateDate(Config.fomatDate(challan.getCreateDate()));
				long refId = sequenceGenerator.generateSequence(Challan.SEQUENCE_NAME);
				challan.setRefId(refId);
				if(!StringUtils.isEmpty(challan.getRefNo())) {
					challan.setRefNo(challan.getRefNo().toUpperCase());
				}
				if(!StringUtils.isEmpty(challan.getBankName())) {
					challan.setBankName(challan.getBankName().toUpperCase());
				}
				challanRepo.save(challan);
				response.setSuccess(true);
				response.setMsg(Arrays.asList(StringConstant.CHALLAN_CREATED_SUCCESS));
				return response;
			}	
		}
		catch(Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return response;
		}
	}
	
	
	public AppResponse<Void> cancelChallan(long refId){
		
		AppResponse<Void> response = new AppResponse<>();
		
		try {
			Challan challan = challanRepo.getChallanByRefId(refId);
			
			if(null == challan) {
				response.setSuccess(false);
				response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
				return response;
			}
			else {
				
				Challan latestChallanOnInvoice = challanRepo.getLatestChallanOnInvoice(challan.getInvoiceNo());
				if(null!= latestChallanOnInvoice && challan.getId().equals(latestChallanOnInvoice.getId())) {
					latestChallanOnInvoice.setCancelled(true);
					String userId = SecurityContextHolder.getContext().getAuthentication().getName();
					latestChallanOnInvoice.setUpdateBy(userId);
					latestChallanOnInvoice.setUpdateDate(new Date());
					challanRepo.save(latestChallanOnInvoice);
					response.setSuccess(true);
					response.setMsg(Arrays.asList(StringConstant.CHALLAN_CALCELED_SUCCESS));
					return response;
				}
				else {
					response.setSuccess(false);
					response.setMsg(Arrays.asList(StringConstant.CHALLAN_CALCELED_FAILD));
					return response;
				}
			}
		}
		catch(Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return response;
		}
	}
	
	
	public AppResponse<List<Document>> getChallanList(Map<String, Object> filters){
		
		AppResponse<List<Document>> response = new AppResponse<>();
		try {
			List<Document> challanList = challanRepo.getChallanList(filters);
			response.setSuccess(true);
			response.setData(challanList);
			return response;
		}
		catch(Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return response;
		}
		
	}
	
	public AppResponse<List<Document>> getAllActiveChallanOnInvoice(long invRefId){
		
		AppResponse<List<Document>> response = new AppResponse<>();
		try {
			List<Document> challanList = challanRepo.getAllActiveChallanOnInvoice(invRefId);
			response.setSuccess(true);
			response.setData(challanList);
			return response;
		}
		catch(Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return response;
		}
		
	}

}
