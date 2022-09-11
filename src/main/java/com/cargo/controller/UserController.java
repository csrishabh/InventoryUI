package com.cargo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.cargo.document.User;
import com.cargo.repo.UserRepository;
import com.cargo.service.CustomUserDetailsService;

@RestController
@RequestMapping("/backend")
public class UserController {

	@Autowired
	UserRepository userRepo;
	
	@Autowired
	CustomUserDetailsService userService;

	@GetMapping(value = "/getUsers")
    public ResponseEntity<List<String>> getAllUsers() {
		List<User> users = userRepo.findAll();
		return new ResponseEntity<List<String>>(users.stream().map(user -> user.getUsername()).collect(Collectors.toList()),HttpStatus.OK);
    }
	
	@GetMapping("/user/{name}/{type}")
	public List<User> getPersonByNameAndType(@PathVariable("name") String name, @PathVariable("type") String type) {
		if(name.trim().equals("")) {
			return new ArrayList<>();
		}
		List<User> users = userRepo.findByNameStartingWithAndType(name.trim(),type.toUpperCase().trim(),true);
		return users;
	}
	
	@GetMapping("/get/user")
	public ResponseEntity<List<Document>> searchUsers(@RequestParam Map<String, Object> filters) {
		try {
			List<Document> users = userRepo.searchUsers(filters);
			return new ResponseEntity<List<Document>>(users, HttpStatus.OK);
		}
		catch (Exception e) {
			return new ResponseEntity<List<Document>>(HttpStatus.INTERNAL_SERVER_ERROR);	
		}
	}
	
	@GetMapping("/user/{name}")
	public List<User> getPersonByName(@PathVariable("name") String name) {
		if(name.trim().equals("")) {
			return new ArrayList<>();
		}
		List<User> users = userRepo.findByNameStartingWith(name.trim());
		return users;
	}
	
	@PostMapping("/password/reset")
	public AppResponse<String> resetPassword(@RequestBody HashMap<String, String> param) {
		return userService.resetPassword(param);
	}
	
	@PostMapping("/create/user")
	public AppResponse<Void> createUser(@RequestBody User user) {
		return userService.saveUser(user);
	}
	
	@PostMapping("/update/user")
	public AppResponse<Void> updateUser(@RequestBody User user) {
		return userService.updateUser(user);
	}
	

}
