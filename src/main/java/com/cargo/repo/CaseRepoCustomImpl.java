package com.cargo.repo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;

import com.cargo.document.Case;
import com.cargo.document.CaseSearchResult;
import com.cargo.document.CaseStatus;
import com.cargo.utility.Config;



public class CaseRepoCustomImpl implements CaseRepoCustom {
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	
	public List<CaseSearchResult> findAllLatestCase(Map<String, Object> filters) {
		
		
		SortOperation sortByVersionAsc = Aggregation.sort(new Sort(Direction.DESC, "version"));	
		GroupOperation getCaseWithMaxVersion = Aggregation.group("opdNo","bookingDate").first(Aggregation.ROOT).as("Case");
		SortOperation sortByBookingDate = Aggregation.sort(new Sort(Direction.DESC, "Case.bookingDate"));	
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(applyFilter(filters)),sortByVersionAsc,getCaseWithMaxVersion,
				Aggregation.match(applyPostFilter(filters)),Aggregation.match(applyFilter2(filters)),sortByBookingDate);
		AggregationResults<CaseSearchResult> result = mongoTemplate.aggregate(aggregation, "case",CaseSearchResult.class);
		return result.getMappedResults();	
		
	}
	
	 public List<CaseSearchResult> findLatestCase(String opdNo) {
		
		
		SortOperation sortByVersionAsc = Aggregation.sort(new Sort(Direction.DESC, "version"));	
		GroupOperation getCaseWithMaxVersion = Aggregation.group("opdNo").first(Aggregation.ROOT).as("Case");	
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where("opdNo").is(opdNo)),sortByVersionAsc,getCaseWithMaxVersion);
		AggregationResults<CaseSearchResult> result = mongoTemplate.aggregate(aggregation, "case",CaseSearchResult.class);
		return result.getMappedResults();	
		
	}
	 
	 public List<Document> findLateCaseCount() {
			
			
			SortOperation sortByVersionAsc = Aggregation.sort(new Sort(Direction.DESC, "version"));	
			GroupOperation getCaseWithMaxVersion = Aggregation.group("opdNo","bookingDate").first("status").as("status").first("deliveredDate").as("deliveredDate");	
			Aggregation aggregation = Aggregation.newAggregation(sortByVersionAsc,getCaseWithMaxVersion,
					Aggregation.match(Criteria.where("status").is(CaseStatus.INPROCESS).and("deliveredDate").lt(Config.fomatDate(new Date()))),Aggregation.count().as("count"));
			AggregationResults<Document> result = mongoTemplate.aggregate(aggregation, "case",Document.class);
			return result.getMappedResults();	
	}
	 
	public List<CaseSearchResult> findAllLateCase() {
			
			SortOperation sortByVersionAsc = Aggregation.sort(new Sort(Direction.DESC, "version"));	
			GroupOperation getCaseWithMaxVersion = Aggregation.group("opdNo","bookingDate").first(Aggregation.ROOT).as("Case");	
			Aggregation aggregation = Aggregation.newAggregation(sortByVersionAsc,getCaseWithMaxVersion,
					Aggregation.match(Criteria.where("Case.status").is(CaseStatus.INPROCESS).and("Case.deliveredDate").lt(Config.fomatDate(new Date()))));
			AggregationResults<CaseSearchResult> result = mongoTemplate.aggregate(aggregation, "case",CaseSearchResult.class);
			return result.getMappedResults();	
			
	}	


	@Override
	public List<CaseSearchResult> findAllLatestCaseByUser(String user,Map<String, Object> filters) {
		MatchOperation filterUsers = Aggregation.match(new Criteria("createdBy").is(user).andOperator(applyFilter(filters)));
		SortOperation sortByVersionAsc = Aggregation.sort(new Sort(Direction.DESC, "version"));	
		GroupOperation getCaseWithMaxVersion = Aggregation.group("opdNo").first(Aggregation.ROOT).as("Case");
		SortOperation sortByBookingDate = Aggregation.sort(new Sort(Direction.DESC, "Case.bookingDate"));
		Aggregation aggregation = Aggregation.newAggregation(filterUsers,sortByVersionAsc,getCaseWithMaxVersion,Aggregation.match(applyPostFilter(filters)),Aggregation.match(applyFilter2(filters)),sortByBookingDate);
		AggregationResults<CaseSearchResult> result = mongoTemplate.aggregate(aggregation, "case", CaseSearchResult.class);
		return result.getMappedResults();
	}
	
	public boolean isPaidBefore(String OpdNo, Date bookingDate,String vendorId, Date updateDate) {
		Criteria criteria = new Criteria();
		criteria.and("opdNo").is(OpdNo);
		criteria.and("bookingDate").is(bookingDate);
		criteria.and("updateDate").lt(updateDate);
		criteria.and("status").is(CaseStatus.DELIVERD);
		criteria.and("vender.$id").is(new ObjectId(vendorId));
		Query query = new Query();
		query.addCriteria(criteria);
		return mongoTemplate.find(query, Case.class).size() == 0 ? false : true;
	}
	@Override
	public List<CaseSearchResult> getVendorReport(Map<String, Object> filters){
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		Criteria criteria = new Criteria();
		criteria.and("vender.$id").is(new ObjectId((String) filters.get("vender")));
		criteria.and("status").in(Arrays.asList((CaseStatus.DELIVERD)));
		criteria.and("subStatus").in(Arrays.asList(((String) filters.get("subStatus")).split(",")));
		try {
			criteria.andOperator(Criteria.where("updateDate").gte(format.parse((String) filters.get("updateDate1"))),Criteria.where("updateDate").lte(format.parse((String) filters.get("updateDate2"))));
		} catch (ParseException e) {
			
		}
		MatchOperation report = Aggregation.match(criteria);
		GroupOperation getCaseWithMaxVersion = Aggregation.group("opdNo","bookingDate").first(Aggregation.ROOT).as("Case");
		SortOperation sortByBookingDate = Aggregation.sort(new Sort(Direction.DESC, "Case.bookingDate"));
		Aggregation aggregation = Aggregation.newAggregation(report,getCaseWithMaxVersion,sortByBookingDate);
		AggregationResults<CaseSearchResult> result = mongoTemplate.aggregate(aggregation, "case", CaseSearchResult.class);
		return result.getMappedResults();
	}
	
	private Criteria applyFilter2(Map<String, Object> filters) {
		Criteria criteria = new Criteria();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		if(!StringUtils.isEmpty(filters.get("dlvDate1")) && !StringUtils.isEmpty(filters.get("dlvDate2"))) {
			try {
				Date dlvDate2 = format.parse((String) filters.get("dlvDate2"));
				criteria.andOperator(Criteria.where("Case.deliveredDate").gte(format.parse((String) filters.get("dlvDate1"))),Criteria.where("Case.deliveredDate").lte(dlvDate2));
			} catch (ParseException e) {
				
			}
		}
		else if(!StringUtils.isEmpty(filters.get("dlvDate1"))) {
			try {
				criteria.and("Case.deliveredDate").is(format.parse((String) filters.get("dlvDate1")));
			} catch (ParseException e) {
			}
		}
		return criteria;
	}
	
	private Criteria applyPostFilter(Map<String, Object> filters) {
		Criteria criteria = new Criteria();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		filters.forEach((k,v)->{
			switch(k) {
			case "status": {
				if(!StringUtils.isEmpty(v) && !((String) v).equalsIgnoreCase("ALL"))
				criteria.and("Case."+k).in(Arrays.asList(((String) v).split(",")));
				break;
			}
			case "subStatus": {
				if(!StringUtils.isEmpty(v) && !((String) v).equalsIgnoreCase("ALL"))
				criteria.and("Case."+k).in(Arrays.asList(((String) v).split(",")));
				break;
			}
			case "vender": {
				if(!StringUtils.isEmpty(v))
				criteria.and("Case."+k+".$id").is(new ObjectId((String)v));
				break;
			}
			case "doctor": {
				if(!StringUtils.isEmpty(v))
				criteria.and("Case."+k+".$id").is(new ObjectId((String)v));
				break;
			}
			case "aptDate1": { 
				if(!StringUtils.isEmpty(v) && !StringUtils.isEmpty(filters.get("aptDate2"))) {
					try {
						Date aptDate2 = format.parse((String) filters.get("aptDate2"));
						aptDate2 = DateUtils.setHours(aptDate2, 23);
						aptDate2 = DateUtils.setMinutes(aptDate2, 59);
						aptDate2 = DateUtils.setSeconds(aptDate2, 59);
						criteria.andOperator(Criteria.where("Case.appointmentDate").gte(format.parse((String) v)),Criteria.where("Case.appointmentDate").lte(aptDate2));
					} catch (ParseException e) {
						
					}
				}
				else if(!StringUtils.isEmpty(v)) {
					try {
						criteria.and("Case.appointmentDate").is(format.parse((String) v));
					} catch (ParseException e) {
					}
				}
				break;
			}
			
			case "updateDate1": {
				if(!StringUtils.isEmpty(v) && !StringUtils.isEmpty(filters.get("updateDate2"))) {
					try {
						criteria.andOperator(Criteria.where("Case.updateDate").gte(format.parse((String) v)),Criteria.where("Case.updateDate").lte(format.parse((String) filters.get("updateDate2"))));
					} catch (ParseException e) {
						
					}
				}
				else if(!StringUtils.isEmpty(v)) {
					try {
						criteria.and("Case.updateDate").is(format.parse((String) v));
					} catch (ParseException e) {
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
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		filters.forEach((k,v)->{
			switch(k) {
			
			case "createdBy": {
				if(!StringUtils.isEmpty(v))
				criteria.and(k).regex((String)v,"i");
				break;
			}
			case "patient": {
				if(!StringUtils.isEmpty(v))
				criteria.and(k).regex((String)v,"i");
				break;
			}
			case "opdNo": {
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
	

}
