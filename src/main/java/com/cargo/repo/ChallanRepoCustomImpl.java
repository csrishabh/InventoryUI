package com.cargo.repo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Divide;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.StringUtils;

import com.cargo.document.Challan;
import com.cargo.utility.CustomProjectAggregationOperation;

public class ChallanRepoCustomImpl implements ChallanRepoCustom {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public long getTotalPaidAmountOnInvoice(long invoiceId) {

		Aggregation aggregation = Aggregation.newAggregation(
			     Aggregation.match(Criteria.where("invoiceNo").is(invoiceId).and("isCancelled").is(false)),
			     Aggregation.group().sum("amount").as("total"));
		Document doc = mongoTemplate.aggregate(aggregation, "challan",Document.class).getUniqueMappedResult();
		if(null!=doc){
			return doc.getLong("total");
		}
		return 0;
	}

	@Override
	public Challan getLatestChallanOnInvoice(long invoiceId) {
		
		Aggregation aggregation = Aggregation.newAggregation(
			     Aggregation.match(Criteria.where("invoiceNo").is(invoiceId).and("isCancelled").is(false)),
			     Aggregation.sort(Sort.Direction.DESC, "updateDate") , Aggregation.limit(1));
		Challan challan  = mongoTemplate.aggregate(aggregation, "challan",Challan.class).getUniqueMappedResult();
		return challan;
		
	}

	@Override
	public List<Document> getChallanList(Map<String, Object> filters) {
		long pageNo=0;
		long pageSize = 20;
		if(filters.containsKey("pageNo")) {
			pageNo = Long.parseLong((String)filters.get("pageNo"));
		}
		if(filters.containsKey("pageSize")) {
			pageSize = Long.parseLong((String)filters.get("pageSize"));
		}
		
		String getCurrentInvoice =
	            "{ $lookup: { " +
	                    "from: 'invoice'," +	
	                    "let: { 'invNo': '$invoiceNo' }," +
	                    "pipeline: [{$match: {$expr: {$and: [{ $eq: ['$isCurrent', true]},{ $eq: ['$refId', '$$invNo']}]}}},{ $project: { _id: 0, refNo: 1} }]," +
	                    "as: 'invoice'}}";

		ProjectionOperation projectStage = Aggregation.project("refId","isCancelled","note","refNo","bankName",
				"createDate","updateDate","type").andExclude("_id").
				and("crBy.fullname").arrayElementAt(0).as("createdBy").
				and("upBy.fullname").arrayElementAt(0).as("updateBy").
				and("invoice.refNo").arrayElementAt(0).as("invRefNo").
				and(Divide.valueOf("amount").divideBy(100)).as("amount");
					
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(applyFilter(filters)),
				Aggregation.lookup("user", "updateBy", "username", "upBy"),
				Aggregation.lookup("user", "createdBy", "username", "crBy"),
				new CustomProjectAggregationOperation(getCurrentInvoice),
				projectStage, Aggregation.match(applyInvoiceFilter(filters)) ,Aggregation.skip(pageNo*pageSize),Aggregation.limit(pageSize));
		
		AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "challan",Document.class);
		
		return result.getMappedResults();
	}
	
	@Override
	public List<Document> getAllActiveChallanOnInvoice(long invoiceId) {
		
		ProjectionOperation projectStage = Aggregation.project("refId","note","refNo","bankName",
				"createDate","type").andExclude("_id").
				and("crBy.fullname").arrayElementAt(0).as("createdBy").
				and(Divide.valueOf("amount").divideBy(100)).as("amount");
					
		SortOperation sortByupdateDate = Aggregation.sort(new Sort(Direction.ASC, "updateDate"));	
		
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where("invoiceNo").is(invoiceId).and("isCancelled").is(false)),
				sortByupdateDate,
				Aggregation.lookup("user", "createdBy", "username", "crBy"),projectStage);
		
		
		
		AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "challan",Document.class);
		return result.getMappedResults();
	}
	
	private Criteria applyInvoiceFilter(Map<String, Object> filters) {
		
		Criteria criteria = new Criteria();
		filters.forEach((k,v)->{
			switch(k) {
			case "invRefNo": {
				if(!StringUtils.isEmpty(v))
					criteria.and(k).is(v.toString().toUpperCase());
					break;
			}
			}
		});
		return criteria;
		
	}

	private Criteria applyFilter(Map<String, Object> filters) {
		Criteria criteria = new Criteria();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		filters.forEach((k,v)->{
			switch(k) {
			
			case "refId": {
				if(!StringUtils.isEmpty(v))
				criteria.and(k).is(Long.parseLong((String) v));
				break;
			}
			
			case "invoiceNo": {
				if(!StringUtils.isEmpty(v))
				criteria.and(k).is(Long.parseLong((String) v));
				break;
			}
			
			case "refNo": {
				if(!StringUtils.isEmpty(v))
				criteria.and(k).is(v.toString().toUpperCase());
				
				break;
			}
			
			case "bankName": {
				if(!StringUtils.isEmpty(v))
				criteria.and(k).is(v.toString().toUpperCase());
				
				break;
			}
						
			case "createdBy": {
				if(!StringUtils.isEmpty(v))
					criteria.and(k).is((String)v);
				break;
			}
			
			case "type": {
				if(!StringUtils.isEmpty(v) && !((String) v).equalsIgnoreCase("ALL")) {
					criteria.and(k).is((String)v);
			}	
				break;	
			}
			
			case "isCancelled": {
				if(!StringUtils.isEmpty(v) && !((String) v).equalsIgnoreCase("ALL")) {
					if(v.toString().equalsIgnoreCase("true")) {	
						criteria.and("isCancelled").is(true);
					}
					else {
						criteria.and("isCancelled").is(false);
					}
				}	
				break;	
			}
			
			case "createdDate1": {
				if(!StringUtils.isEmpty(v) && !StringUtils.isEmpty(filters.get("createdDate2"))) {
					try {
						criteria.andOperator(Criteria.where("createDate").gte(format.parse(((String) v)+" 00:00:00")),
								Criteria.where("createDate").lte(format.parse(((String) filters.get("createdDate2"))+" 23:59:59")));
					} catch (ParseException e) {
						
					}
				}
				else if(!StringUtils.isEmpty(v)) {
					try {
						criteria.and("createDate").gte(format.parse(((String) v)+" 00:00:00"));
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
