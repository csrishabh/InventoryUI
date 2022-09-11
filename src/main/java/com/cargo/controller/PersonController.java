package com.cargo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cargo.document.Person;
import com.cargo.repo.PersonRepo;

@RestController
@RequestMapping("/backend")
public class PersonController {
	
	@Autowired
	private PersonRepo personRepo;

	@GetMapping("/person/{name}/{type}")
	public List<Person> getPersonByNameAndType(@PathVariable("name") String name, @PathVariable("type") String type) {
		if(name.trim().equals("")) {
			return new ArrayList<>();
		}
		List<Person> persons = personRepo.findByNameStartingWith(name.trim(),type.toUpperCase().trim());
		return persons;
	}
	
	
	@PostMapping("/add/person")
	public ResponseEntity<Person> addPerson(@RequestBody Person person) {
		try {	
		person = personRepo.save(person);
		return new ResponseEntity<Person>(person, HttpStatus.CREATED);
		}
		catch(Exception e) {
			return new ResponseEntity<Person>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
}
