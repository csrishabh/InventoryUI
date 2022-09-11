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
import com.cargo.document.Invoice;
import com.cargo.service.InvoiceService;

@RestController
@RequestMapping("/backend")
public class InvoiceController {
	
	@Autowired
	private InvoiceService invoiceService;

	@PostMapping("/add/invoice")
	@PreAuthorize ("hasAuthority('USER_INVOICE')")
	public AppResponse<Void> addInvoice(@RequestBody Invoice invoice){
	
		return invoiceService.addInvoice(invoice);
	}
	
	@PostMapping("/update/invoice")
	@PreAuthorize ("hasAuthority('USER_INVOICE')")
	public AppResponse<Void> updateInvoice(@RequestBody Invoice invoice){
	
		return invoiceService.updateInvoice(invoice);
	}
	
	@PostMapping("/cancel/invoice")
	@PreAuthorize ("hasAuthority('ADMIN_INVOICE')")
	public AppResponse<Void> cancelInvoice(@RequestBody long refId){
	
		return invoiceService.cancelInvoice(refId);
	}
	
	@GetMapping("/get/invoice")
	@PreAuthorize ("hasAuthority('USER_INVOICE')")
	public AppResponse<List<Document>> getInvoiceList(@RequestParam Map<String, Object> filters) {
		
		return invoiceService.getInvoiceList(filters);
	}
	
	@GetMapping("/get/invoice/{refId}")
	@PreAuthorize ("hasAuthority('ADMIN_INVOICE')")
	public AppResponse<List<Document>> getInvoiceHistory(@PathVariable("refId") long refId) {
		
		return invoiceService.getInvoiceHistroy(refId);
	}
	
	
	
}
