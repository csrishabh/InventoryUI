package com.cargo.repo;

import java.util.List;

import com.cargo.document.Person;

public interface PersonRepoCustom {

	 List<Person> findByNameStartingWith(String regexp, String type);
}
