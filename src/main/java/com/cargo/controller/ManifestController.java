package com.cargo.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cargo.document.AppResponse;
import com.cargo.document.Manifest;
import com.cargo.service.ManifestService;

@RestController
@RequestMapping("/backend")
public class ManifestController {

	
	@Autowired
	private ManifestService manifestService;
	
	@PostMapping("/add/manifest")
	public AppResponse<String> createManifest(@RequestBody Manifest manifest){
		
		return manifestService.createManifest(manifest);
	}
	
	@GetMapping("/get/manifest")
	public ResponseEntity<List<Document>> getManifestHistory(HttpServletRequest request, @RequestParam Map<String, Object> filters){
		try {
		List<Document> consignments = manifestService.getManifestHistory(filters);
		return new ResponseEntity<List<Document>>(consignments, HttpStatus.OK);
		}
		catch(Exception e) {
			return new ResponseEntity<List<Document>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	@PostMapping("/delete/manifest/consignment/{biltyNo}")
	public AppResponse<Void> updateManifest(HttpServletRequest request, @RequestBody String refId, @PathVariable("biltyNo") String biltyNo){
		
		return manifestService.deleteConsignmentFromManifest(refId, biltyNo);
		
	}
	
	@PostMapping("/delete/manifest")
	public AppResponse<Void> deleteManifest(HttpServletRequest request, @RequestBody String refId){
		
		return manifestService.deleteManifest(refId);
		
	}
}
