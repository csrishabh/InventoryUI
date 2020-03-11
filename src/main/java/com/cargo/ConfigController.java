package com.cargo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {
	
	@Value(value = "${backendUrl:http://localhost:8080}")
	private String backendUrl;
	
	
	@GetMapping("/backendUrl")
	public ResponseEntity<Map<String, String>> getBackendUrl() {
		
		Map<String, String> response = new HashMap<>();
		response.put("url", backendUrl);
		return new ResponseEntity<Map<String, String>>(response,HttpStatus.OK);
	}

}
