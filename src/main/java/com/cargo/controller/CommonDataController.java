package com.cargo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cargo.document.AppResponse;
import com.cargo.document.CommonData;
import com.cargo.repo.CommonDataRepo;

@RestController
@RequestMapping("/backend")
public class CommonDataController {
	
	@Autowired
	CommonDataRepo commonDataRepo;
	
	@GetMapping("/commmonData/{typeId}/{name}")
	public List<CommonData> getPersonByNameAndType(@PathVariable("name") String name, @PathVariable("typeId") int typeId) {
		if(name.trim().equals("")) {
			return new ArrayList<>();
		}
		List<CommonData> data = commonDataRepo.findByNameStartingWithAndType(name.trim(),typeId, false);
		return data;
	}
	
	/*@PostMapping("update/commmonData/status")
	public AppResponse<Void> updateStatus(@RequestBody CommonData data) {
		
	}*/

}
