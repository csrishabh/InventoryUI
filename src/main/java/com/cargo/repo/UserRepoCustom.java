package com.cargo.repo;

import java.util.List;
import java.util.Map;

import org.bson.Document;

public interface UserRepoCustom {

	public List<Document> searchUsers(Map<String, Object> filters);
}
