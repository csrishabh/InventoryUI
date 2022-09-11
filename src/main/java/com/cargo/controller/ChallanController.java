package com.cargo.controller;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cargo.document.AppResponse;
import com.cargo.document.Challan;
import com.cargo.service.ChallanService;

@RestController
@RequestMapping("/backend")
public class ChallanController {
	
	
	@Autowired
	private ChallanService challanService;
	
	@PostMapping("/add/challan")
	@PreAuthorize ("hasAuthority('USER_INVOICE')")
	public AppResponse<Void> addChallan(@RequestBody Challan challan){
	
		return challanService.addChallan(challan);
	}
	
	@PostMapping("/cancel/challan")
	@PreAuthorize ("hasAuthority('ADMIN_INVOICE')")
	public AppResponse<Void> cancelChallan(@RequestBody long refId){
		
		return challanService.cancelChallan(refId);
	}
	
	@GetMapping("/get/challan")
	@PreAuthorize ("hasAuthority('USER_INVOICE')")
	public AppResponse<List<Document>> getChallanList(@RequestParam Map<String, Object> filters) {
		
		return challanService.getChallanList(filters);
	}
	
	@GetMapping("/get/challan/{invRefId}")
	@PreAuthorize ("hasAuthority('USER_INVOICE')")
	public AppResponse<List<Document>> getAllActiveChallanOnInvoice(@PathVariable("invRefId") long invRefId) {
		
		return challanService.getAllActiveChallanOnInvoice(invRefId);
	}
	

}
