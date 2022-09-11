package com.cargo.repo;

import static org.springframework.data.mongodb.core.aggregation.ConditionalOperators.when;
import static org.springframework.data.mongodb.core.query.Criteria.where;

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
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Divide;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators.Multiply;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.StringUtils;

import com.cargo.document.Consignment;
import com.cargo.document.Unit;

public class ConsignmentRepoCustomImpl implements ConsignmentRepoCustom{

	@Autowired
	MongoTemplate  mongoTemplate;
	
	@Override
	public List<Document> searchConsignment(Map<String, Object> filters) {
		long pageNo=0;
		long pageSize = 20;
		if(filters.containsKey("pageNo")) {
			pageNo = Long.parseLong((String)filters.get("pageNo"));
		}
		if(filters.containsKey("pageSize")) {
			pageSize = Long.parseLong((String)filters.get("pageSize"));
		}
		ProjectionOperation projectStage = Aggregation.project("biltyNo","des","totalParcel","bookingDate","unit","maniFestRefNo","isDeliverd","isDeleted").andExclude("_id").and("consignee.fullname").as("consignee").and("consignor.fullname").as("consignor").
				and(when(where("unit").is(Unit.KILOGRAM)).then(Multiply.valueOf(Divide.valueOf("weight").divideBy(1000)).multiplyBy(Divide.valueOf("rate").divideBy(100))).otherwise(Multiply.valueOf("totalParcel").multiplyBy(Divide.valueOf("rate").divideBy(100)))).as("total").
				and(Divide.valueOf("discount").divideBy(100)).as("discount").
				and(Divide.valueOf("tax").divideBy(100)).as("tax").
				and(Divide.valueOf("remark1").divideBy(100)).as("other1").
				and(Divide.valueOf("remark2").divideBy(100)).as("other2").
				and(Divide.valueOf("weight").divideBy(1000)).as("weight").
				and(Divide.valueOf("rate").divideBy(100)).as("rate");
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(applyFilter(filters)),Aggregation.lookup("user", "consignee", "username", "consignee"),Aggregation.lookup("user", "consignor", "username", "consignor"), projectStage,Aggregation.skip(pageNo*pageSize),Aggregation.limit(pageSize));
		AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "consignment",Document.class);
		return result.getMappedResults();
	}
	
	private Criteria applyFilter(Map<String, Object> filters) {
		Criteria criteria = new Criteria();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		filters.forEach((k,v)->{
			switch(k) {
			case "biltyNo": {
				if(!StringUtils.isEmpty(v))
				criteria.and(k).in(Arrays.asList(v.toString().toUpperCase().split(",")));
				break;
			}
			case "status": {
				if(!StringUtils.isEmpty(v) && !((String) v).equalsIgnoreCase("ALL")) {
				if(v.toString().equalsIgnoreCase("true")) {	
				criteria.and("isDeliverd").is(true);
				}
				else {
					criteria.and("isDeliverd").is(false);
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
			case "des": {
				if(!StringUtils.isEmpty(v))
				criteria.and(k).regex((String)v,"i");
				break;
			}
			case "consignor": {
				if(!StringUtils.isEmpty(v))
					criteria.and(k).is((String)v);
				break;
			}
			case "consignee": {
				if(!StringUtils.isEmpty(v))
					criteria.and(k).is((String)v);
				break;
			}
			case "bookingDate1": {
				if(!StringUtils.isEmpty(v) && !StringUtils.isEmpty(filters.get("bookingDate2"))) {
					try {
						criteria.andOperator(Criteria.where("bookingDate").gte(format.parse((String) v)),Criteria.where("bookingDate").lte(format.parse((String) filters.get("bookingDate2"))));
					} catch (ParseException e) {
						
					}
				}
				else if(!StringUtils.isEmpty(v)) {
					try {
						criteria.and("bookingDate").is(format.parse((String) v));
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
	public boolean isAnyConsignmentDeliverd(List<String> consignments) {
	
		Query query = new Query();
		query.addCriteria(Criteria.where("biltyNo").in(consignments).and("isDeliverd").is(true));
		long count = mongoTemplate.count(query, "consignment");
		if(count>0) {
			return true;
		}
		return false;
		
	}
	
	@Override
	public boolean isAnyConsignmentDeleted(List<String> consignments) {
	
		Query query = new Query();
		query.addCriteria(Criteria.where("biltyNo").in(consignments).and("isDeleted").is(true));
		long count = mongoTemplate.count(query, "consignment");
		if(count>0) {
			return true;
		}
		return false;
		
	}

	@Override
	public boolean isDuplicateConsignment(String biltyNo) {
		Query query = new Query();
		query.addCriteria(Criteria.where("biltyNo").is(biltyNo));
		long count = mongoTemplate.count(query, "consignment");
		if(count>0) {
			return true;
		}
		return false;
	}

	@Override
	public void setConsignmentDeliverd(List<String> consignments, String des) {
		
		Query query = new Query();
		Update update = new Update();
		query.addCriteria(Criteria.where("biltyNo").in(consignments.toArray()).and("des").is(des));
		update.set("isDeliverd", true);
		mongoTemplate.updateMulti(query, update, Consignment.class);		
	}

	@Override
	public void updateManifest(List<String> consignments, String manifestNo,boolean isDeleted) {
		Query query = new Query();
		Update update = new Update();
		query.addCriteria(Criteria.where("biltyNo").in(consignments));
		if(isDeleted) {
			update.pull("maniFestRefNo", manifestNo);
		}
		else {
		update.push("maniFestRefNo", manifestNo);
		}
		mongoTemplate.updateMulti(query, update, Consignment.class);		
		
	}

	@Override
	public boolean isConsignmentProcessed(String biltyNo) {
		Query query = new Query();
		query.addCriteria(Criteria.where("biltyNo").is(biltyNo)).addCriteria(Criteria.where("maniFestRefNo").ne(null));
		long count = mongoTemplate.count(query, "consignment");
		if(count>0) {
			return true;
		}
		return false;
	}

	@Override
	public void deleteConsignment(String biltyNo) {
		Query query = new Query();
		Update update = new Update();
		query.addCriteria(Criteria.where("biltyNo").is(biltyNo));
		update.set("isDeleted", true);
		mongoTemplate.findAndModify(query, update, Consignment.class);		
		
	}
	
	@Override
	public Consignment getConsignment(String biltyNo) {
		Query query = new Query();
		query.addCriteria(Criteria.where("biltyNo").is(biltyNo));
		return mongoTemplate.findOne(query, Consignment.class);
	}

	@Override
	public void setConsignmentUnDeliverd(List<String> consignments, String des) {
		Query query = new Query();
		Update update = new Update();
		query.addCriteria(Criteria.where("biltyNo").in(consignments.toArray()).and("des").is(des));
		update.set("isDeliverd", false);
		mongoTemplate.updateMulti(query, update, Consignment.class);
		
	}

}
