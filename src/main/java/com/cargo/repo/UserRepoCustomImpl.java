package com.cargo.repo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StringUtils;

public class UserRepoCustomImpl implements UserRepoCustom {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<Document> searchUsers(Map<String, Object> filters) {
		long pageNo=0;
		long pageSize = 20;
		if(filters.containsKey("pageNo")) {
			pageNo = Long.parseLong((String)filters.get("pageNo"));
		}
		if(filters.containsKey("pageSize")) {
			pageSize = Long.parseLong((String)filters.get("pageSize"));
		}
		ProjectionOperation projectStage = Aggregation.project("fullname","username","mobileNo","address","gstNo","roles","enabled").andExclude("_id");
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(applyFilter(filters)),projectStage,Aggregation.skip(pageNo*pageSize),Aggregation.limit(pageSize));
		AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "user",Document.class);
		return result.getMappedResults();
	}
	
	
	private Criteria applyFilter(Map<String, Object> filters) {
		Criteria criteria = new Criteria();
		filters.forEach((k,v)->{
			switch(k) {
			
			case "fullname": {
				if(!StringUtils.isEmpty(v))
				criteria.and(k).regex((String)v,"i");
				break;
			}
			case "username": {
				if(!StringUtils.isEmpty(v))
					criteria.and(k).regex((String)v,"i");
				break;
			}
			case "roles": {
				if(!StringUtils.isEmpty(v) && !((String) v).equalsIgnoreCase("ALL"))
				criteria.and(k).in(Arrays.asList(((String) v).split(",")));
				break;
			}
			case "mobileNo": {
				if(!StringUtils.isEmpty(v))
					criteria.and(k).regex((String)v,"i");
				break;
			}
			
			case "type": {
				if(!StringUtils.isEmpty(v) && !((String) v).equalsIgnoreCase("ALL")) {
				if(v.toString().equalsIgnoreCase("true")) {	
				criteria.and("enabled").is(true);
				}
				else {
					criteria.and("enabled").is(false);
				}
			}	
				break;	
			}
			
			}
		});
		return criteria;
	}

}
