package com.cargo.repo;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import com.cargo.document.Unit;
import com.cargo.document.UnitMapping;

public class UnitMappingRepoCustomImpl implements UnitMappingRepoCustom {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public List<UnitMapping> getCurrentUnitByCompany(String compamyId, Unit... type) {
		
		SortOperation sortByVersionAsc = Aggregation.sort(new Sort(Direction.DESC, "version"));	
		GroupOperation getMappingWithMaxVersion = Aggregation.group("unit").first("version").as("version").first("unit").as("unit").first("price").as("price");
		Criteria criteria ;
		if(type.length == 0) {
		criteria = Criteria.where("price").gt(0).and("company.$id").is(new ObjectId(compamyId));
		}
		else {
			criteria = Criteria.where("price").gt(0).and("company.$id").is(new ObjectId(compamyId)).and("unit").is(type[0]);
		}
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria),sortByVersionAsc,getMappingWithMaxVersion);
		AggregationResults<UnitMapping> result = mongoTemplate.aggregate(aggregation, "unitMapping",UnitMapping.class);
		return result.getMappedResults();
	}

	@Override
	public UnitMapping getLatestUnitMappingByCompany(String companyId, Unit type) {
		List<UnitMapping> unitMappings = getCurrentUnitByCompany(companyId, type);
		if(null != unitMappings && unitMappings.size() > 0) {
			return unitMappings.get(0);
		}
		else {
		return null;
		}
	}

}
