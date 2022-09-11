package com.cargo.repo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Divide;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.StringUtils;

import com.cargo.document.Transction;
import com.cargo.document.TransctionType;
import com.cargo.utility.Config;
import com.cargo.utility.CustomProjectAggregationOperation;

public class TransctionRepoImpl implements TransctionRepoCustom {

	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public List<Transction> getAuditPandingTransction( Map<String, Object> map) {

		Criteria regex = Criteria.where("isAdtable").is(true).andOperator(Criteria.where("isAdtDone").is(false),Criteria.where("isDeleted").ne(true),applyFilter(map));
		return mongoTemplate.find(new Query().addCriteria(regex), Transction.class);
	}

	@Override
	public Transction updateTransction(Transction t) {
		
		Query query = new Query();
		Update update = new Update();
		
		if(t.getType() == TransctionType.AUDIT){
			query.addCriteria(Criteria.where("id").is(t.getId()).and("isAdtDone").is(false));
			update.set("quantityBack", t.getQuantityBack());
			update.set("amountBack", t.getAmountBack());
			update.set("isAdtDone", true);
			update.set("adtDate", Config.fomatDate(new Date()));
			update.set("adtBy", t.getAdtBy());
		}
		
		return mongoTemplate.findAndModify(query, update, Transction.class);
		
	}

	@Override
	public Transction deleteTransction(Transction t) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(t.getId()).and("isAdtDone").is(false));
		return mongoTemplate.findAndRemove(query, Transction.class);
	}

	@Override
	public List<Document> getTransctionByUser(Map<String, Object> map, String userID) {
		Criteria regex = Criteria.where("addBy").is(userID).andOperator(Criteria.where("isDeleted").ne(true),applyFilter(map));
		
		String getProduct =
	            "{ $lookup: { " +
	                    "from: 'product'," +	
	                    "let: { 'prdId':{'$toObjectId':'$productId'}}," +
	                    "pipeline: [{$match: {$expr: {$and: [{ $eq: ['$_id', '$$prdId']}]}}},{ $project: { _id: 0, name: 1, unit: 1} }]," +
	                    "as: 'product'}}";
		
		ProjectionOperation projectStage = Aggregation.project("date","isDeleted","type","id").andExclude("_id").
				and("addBy.fullname").arrayElementAt(0).as("addBy").
				and("product.name").arrayElementAt(0).as("prdName").
				and("product.unit").arrayElementAt(0).as("prdUnit").
				and(Divide.valueOf("quantityBack").divideBy(1000)).as("qty").
				and(Divide.valueOf("amountBack").divideBy(100)).as("amount");
			
		String getRemainingAmt = "{$addFields: { id:" + 
				"{ $toString: '$_id'}}}";
		
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(regex),
				Aggregation.lookup("user", "addBy", "username", "addBy"),
				new CustomProjectAggregationOperation(getProduct),
				new CustomProjectAggregationOperation(getRemainingAmt),
				projectStage,Aggregation.match(applyProductFilter(map)));
		
		AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "transction",Document.class);
		
		return result.getMappedResults();
	}

	@Override
	public List<Document> getAllTransction(Map<String, Object> map) {
		Criteria regex = Criteria.where("isDeleted").ne(true).andOperator(applyFilter(map));
		
		String getProduct =
	            "{ $lookup: { " +
	                    "from: 'product'," +	
	                    "let: { 'prdId':{'$toObjectId':'$productId'}}," +
	                    "pipeline: [{$match: {$expr: {$and: [{ $eq: ['$_id', '$$prdId']}]}}},{ $project: { _id: 0, name: 1, unit: 1,lastPriceBack: 1 , lstAdtDate: 1} }]," +
	                    "as: 'product'}}";
		
		ProjectionOperation projectStage = Aggregation.project("date","isDeleted","type","id").andExclude("_id").
				and("addBy.fullname").arrayElementAt(0).as("addBy").
				and("product.name").arrayElementAt(0).as("prdName").
				and("product.unit").arrayElementAt(0).as("prdUnit").
				and("product.lastPriceBack").arrayElementAt(0).as("lastPriceBack").
				and("product.lstAdtDate").arrayElementAt(0).as("lstAdtDate").
				and(Divide.valueOf("quantityBack").divideBy(1000)).as("qty").
				and(Divide.valueOf("amountBack").divideBy(100)).as("amount");
			
		String getRemainingAmt = "{$addFields: { id:" + 
				"{ $toString: '$_id'}}}";
		
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(regex),
				Aggregation.lookup("user", "addBy", "username", "addBy"),
				new CustomProjectAggregationOperation(getProduct),
				new CustomProjectAggregationOperation(getRemainingAmt),
				projectStage,Aggregation.match(applyProductFilter(map)));
		
		AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "transction",Document.class);
		
		return result.getMappedResults();
	}
	
	private Criteria applyProductFilter(Map<String, Object> filters) {
		
		Criteria criteria = new Criteria();
		filters.forEach((k,v)->{
			switch(k) {
			case "productName": {
				if(!StringUtils.isEmpty(v))
					criteria.and("prdName").regex((String)v,"i");
				break;
			}
			}
		});
		return criteria;
		
	}
	
	private Criteria applyFilter(Map<String, Object> filters) {
		Criteria criteria = new Criteria();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		filters.forEach((k,v)->{
			switch(k) {
			case "type": {
				if(!StringUtils.isEmpty(v) && !((String) v).equalsIgnoreCase("ALL")) {
				criteria.and(k).is(v);
				}
				break;
			}
			case "addBy": {
				if(!StringUtils.isEmpty(v))
				criteria.and(k).regex((String)v,"i");
				break;
			}
			case "productName": {
				if(!StringUtils.isEmpty(v))
					criteria.and(k).regex((String)v,"i");
				break;
			}
			case "startDate": {
				if(!StringUtils.isEmpty(v) && !StringUtils.isEmpty(filters.get("endDate"))) {
					try {
						criteria.andOperator(Criteria.where("date").gte(format.parse((String) v)),Criteria.where("date").lte(format.parse((String) filters.get("endDate"))));
					} catch (ParseException e) {
						
					}
				}
				else if(!StringUtils.isEmpty(v)) {
					try {
						criteria.and("date").is(format.parse((String) v));
					} catch (ParseException e) {
					}
				}
				break;
			}
			}
		});
		return criteria;
	}

}
