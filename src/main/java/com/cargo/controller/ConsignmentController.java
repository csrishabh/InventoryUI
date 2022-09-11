package com.cargo.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cargo.document.AppResponse;
import com.cargo.document.Consignment;
import com.cargo.service.ConsignmentService;

@RestController
@RequestMapping("/backend")
public class ConsignmentController {
	
	@Autowired
	private ConsignmentService consignmentService;
	
	@PostMapping("/add/consignment")
	public AppResponse<Consignment> addConsignment(@RequestBody Consignment c) {
	
		return consignmentService.addConsignment(c);
	}
	
	@PostMapping("/deleted/consignment")
	public AppResponse<Void> deletedConsignment(@RequestBody String biltyNo) {
	
		return consignmentService.deletedConsignment(biltyNo);
	}
	
	@GetMapping("/get/consignments")
	public ResponseEntity<List<Document>> getConsignmentHistory(HttpServletRequest request, @RequestParam Map<String, Object> filters){
		try {
		List<Document> consignments = consignmentService.getConsignmentHistory(filters);
		return new ResponseEntity<List<Document>>(consignments, HttpStatus.OK);
		}
		catch(Exception e) {
			return new ResponseEntity<List<Document>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
}
