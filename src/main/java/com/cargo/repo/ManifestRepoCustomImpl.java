package com.cargo.repo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.StringUtils;

import com.cargo.document.Manifest;

public class ManifestRepoCustomImpl implements ManifestRepoCustom {

	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<Document> searchManifest(Map<String, Object> filters) {
		long pageNo=0;
		long pageSize = 20;
		if(filters.containsKey("pageNo")) {
			pageNo = Long.parseLong((String)filters.get("pageNo"));
		}
		if(filters.containsKey("pageSize")) {
			pageSize = Long.parseLong((String)filters.get("pageSize"));
		}
		ProjectionOperation projectStage = Aggregation.project("refId","des","createdDate","consignments","paidBy","unitComMappingVer","isDeleted").andExclude("_id").and("company.fullname").as("company");
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(applyFilter(filters)),Aggregation.lookup("user", "company", "username", "company"),projectStage,Aggregation.skip(pageNo*pageSize),Aggregation.limit(pageSize));
		AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "manifest",Document.class);
		return result.getMappedResults();
	}
	
	
	private Criteria applyFilter(Map<String, Object> filters) {
		Criteria criteria = new Criteria();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		filters.forEach((k,v)->{
			switch(k) {
			case "refId": {
				if(!StringUtils.isEmpty(v)) {
				criteria.and(k).in(Arrays.asList(v.toString().toUpperCase().split(",")));
				}
				break;
			}
			
			case "des": {
				if(!StringUtils.isEmpty(v))
				criteria.and(k).regex((String)v,"i");
				break;
			}
			case "company": {
				if(!StringUtils.isEmpty(v))
					criteria.and(k).is((String)v);
				break;
			}
			case "createdDate1": {
				if(!StringUtils.isEmpty(v) && !StringUtils.isEmpty(filters.get("createdDate2"))) {
					try {
						criteria.andOperator(Criteria.where("createdDate").gte(format.parse((String) v)),Criteria.where("createdDate").lte(format.parse((String) filters.get("createdDate2"))));
					} catch (ParseException e) {
						
					}
				}
				else if(!StringUtils.isEmpty(v)) {
					try {
						criteria.and("createdDate").is(format.parse((String) v));
					} catch (ParseException e) {
					}
				}
				break;
			}
			
			case "type": {
				if(!StringUtils.isEmpty(v) && !((String) v).equalsIgnoreCase("ALL")) {
				if(v.toString().equalsIgnoreCase("true")) {	
				criteria.and("isDeleted").is(true);
				}
				else {
					criteria.and("isDeleted").is(false);
				}
			}	
				break;	
			}
			
			}
		});
		return criteria;
	}


	@Override
	public void deleteManifest(String refId) {
		Query query = new Query();
		Update update = new Update();
		query.addCriteria(Criteria.where("refId").is(refId));
		update.set("isDeleted", true);
		update.unset("consignments");
		mongoTemplate.findAndModify(query, update, Manifest.class);		
		
	}


	@Override
	public void deleteConsignmentFromManifest(String refId,String biltyNo) {
		Query query = new Query();
		Update update = new Update();
		query.addCriteria(Criteria.where("refId").is(refId));
		update.pull("consignments", biltyNo);
		mongoTemplate.updateMulti(query, update, Manifest.class);
		
	}


	@Override
	public Manifest getManifest(String manifestId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("refId").is(manifestId));
		return mongoTemplate.findOne(query, Manifest.class);
	}
	
	
}
