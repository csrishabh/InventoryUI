package com.cargo.repo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AccumulatorOperators.Sum;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Add;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Divide;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

import com.cargo.document.Invoice;
import com.cargo.utility.CustomProjectAggregationOperation;

public class InvoiceRepoCustomImpl implements InvoiceRepoCustom {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	
	@Override
	public List<Document> getInvoiceList(Map<String, Object> filters) {

		long pageNo=0;
		long pageSize = 20;
		if(filters.containsKey("pageNo")) {
			pageNo = Long.parseLong((String)filters.get("pageNo"));
		}
		if(filters.containsKey("pageSize")) {
			pageSize = Long.parseLong((String)filters.get("pageSize"));
		}

		String getActiveChallan =
	            "{ $lookup: { " +
	                    "from: 'challan'," +	
	                    "let: { 'invNo': '$refId' }," +
	                    "pipeline: [{$match: {$expr: {$and: [{ $eq: ['$isCancelled', false]},{ $eq: ['$invoiceNo', '$$invNo']}]}}},{ $project: { _id: 0, amount: 1, isCancelled: 1, invoiceNo:1 } }]," +
	                    "as: 'challans'}}";
		
		String getRemainingAmt = "{$addFields: { remainingAmt:" + 
				"{ $subtract: [ '$finalValue', '$totalPaid']}}}";
		
		
		ProjectionOperation projectStage = Aggregation.project("refId","isCancelled","note","refNo").andExclude("_id").and("issuer.fullname").as("issuer").
				and("createBy.fullname").as("createBy").
				and(Divide.valueOf(Add.valueOf("amount").add("adjustment")).divideBy(100)).as("finalValue").
				and(Divide.valueOf("amount").divideBy(100)).as("amount").
				and(Divide.valueOf("adjustment").divideBy(100)).as("adjustment").
				and(Divide.valueOf(Sum.sumOf("challans.amount")).divideBy(100)).as("totalPaid").
				and("createdDate").as("createdDate");
					
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(applyFilter(filters)),
				Aggregation.lookup("user", "issuer", "username", "issuer"),
				Aggregation.lookup("user", "createdBy", "username", "createBy"),
				new CustomProjectAggregationOperation(getActiveChallan),
				projectStage, new CustomProjectAggregationOperation(getRemainingAmt),
				Aggregation.match(applyStatusFilter(filters)),Aggregation.skip(pageNo*pageSize),Aggregation.limit(pageSize));
		
		AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "invoice",Document.class);
		
		return result.getMappedResults();
	}
	
	
	@Override
	public List<Document> getInvoiceHistory(long refId) {
		
		ProjectionOperation projectStage = Aggregation.project("refId","isCancelled","refNo","createdDate","updateDate").andExclude("_id").and("issuer.fullname").as("issuer").
				and("createBy.fullname").as("createBy").
				and("updateBy.fullname").as("updateBy").
				and(Divide.valueOf(Add.valueOf("amount").add("adjustment")).divideBy(100)).as("finalValue").
				and(Divide.valueOf("amount").divideBy(100)).as("amount").
				and(Divide.valueOf("adjustment").divideBy(100)).as("adjustment");
		
		SortOperation sortByupdateDate = Aggregation.sort(new Sort(Direction.ASC, "updateDate"));			
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where("refId").is(refId)),sortByupdateDate,
				Aggregation.lookup("user", "issuer", "username", "issuer"),
				Aggregation.lookup("user", "createdBy", "username", "createBy"),
				Aggregation.lookup("user", "updateBy", "username", "updateBy"),
				projectStage);
		
		AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "invoice",Document.class);
		
		return result.getMappedResults();
	}
	
	private Criteria applyStatusFilter(Map<String, Object> filters) {
		
		Criteria criteria = new Criteria();
		filters.forEach((k,v)->{
			switch(k) {
			
			case "status": {
				if(!StringUtils.isEmpty(v) && ! "ALL".equals((String)v)) {
					if("DUE".equals((String)v)) {
						criteria.and("remainingAmt").gt(0);
					}
					else if("PAID".equals((String)v)) {
						criteria.and("remainingAmt").is(0);
					}
				}
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
			
			case "refNo": {
				if(!StringUtils.isEmpty(v))
				criteria.and(k).is(v.toString().toUpperCase());
				
				break;
			}
			case "issuer": {
				if(!StringUtils.isEmpty(v))
					criteria.and(k).is((String)v);
				break;
			}
			case "createdBy": {
				if(!StringUtils.isEmpty(v))
					criteria.and(k).is((String)v);
				break;
			}
			
			case "type": {
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
			
			case "isCurrent": {
				if(!StringUtils.isEmpty(v)) {
					if(v.toString().equalsIgnoreCase("true")) {	
						criteria.and("isCurrent").is(true);
					}
				}	
				break;	
			}
			
			case "createdDate1": {
				if(!StringUtils.isEmpty(v) && !StringUtils.isEmpty(filters.get("createdDate2"))) {
					try {
						criteria.andOperator(Criteria.where("createdDate").gte(format.parse(((String) v)+" 00:00:00")),
								Criteria.where("createdDate").lte(format.parse(((String) filters.get("createdDate2"))+" 23:59:59")));
					} catch (ParseException e) {
						
					}
				}
				else if(!StringUtils.isEmpty(v)) {
					try {
						criteria.and("createdDate").gte(format.parse(((String) v)+" 00:00:00"));
					} catch (ParseException e) {
					}
				}
				break;
			}
			}
		});
		return criteria;
	}


	@Override
	public boolean isDuplicateInvoice(String refNumber, Date invoiceDate, String issuer) {
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		Query query = new Query();
		try {
			query.addCriteria(Criteria.where("refNo").is(refNumber.toUpperCase())
					.and("issuer").is(issuer)
					.and("isCurrent").is(true)
					.and("isCancelled").is(false)
					.andOperator(Criteria.where("createdDate").gte(dateTimeFormat.parse(dateFormat.format(invoiceDate)+" 00:00:00")),
							Criteria.where("createdDate").lte(dateTimeFormat.parse(dateFormat.format(invoiceDate)+" 23:59:59"))));
		} catch (ParseException e) {
			System.out.println("unable to parse date");
		}
		long count = mongoTemplate.count(query, Invoice.class);
		if(count > 0) {
			return true;
		}
		return false;
	}
}
