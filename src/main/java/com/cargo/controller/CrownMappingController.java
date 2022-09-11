package com.cargo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cargo.document.AppResponse;
import com.cargo.document.CROWNTYPE;
import com.cargo.document.CrownMapping;
import com.cargo.service.CrownMappingService;

@RestController
@RequestMapping("/backend")
public class CrownMappingController {
	
	@Autowired
	CrownMappingService crownMappingService;
	
	@PreAuthorize ("hasAuthority('USER_CASE')")
	@GetMapping("/crown/{vendorId}")
	public AppResponse<List<CrownMapping>> getCrownMappingByVendor(@PathVariable("vendorId") String vendorId){
		return crownMappingService.getCrownMappingByVendor(vendorId);
	}
	
	@PreAuthorize ("hasAuthority('ADMIN_CASE')")
	@PostMapping("crown/save/{vendorId}/{type}")
	public AppResponse<Void> saveCrownMapping(@PathVariable("vendorId") String vendorId, @PathVariable("type") CROWNTYPE type , @RequestBody long price){
		return crownMappingService.saveCrownMapping(vendorId, type, price);
	}
	
	@PreAuthorize ("hasAuthority('ADMIN_CASE')")
	@GetMapping("/crown/price/{vendorId}/{type}")
	public AppResponse<Double> getLatestPrice(@PathVariable("vendorId") String vendorId, @PathVariable("type") CROWNTYPE type){
		return crownMappingService.getLatestCrownPriceByVendor(vendorId, type);
	}
}
