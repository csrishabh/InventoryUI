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

import com.cargo.document.CROWNTYPE;
import com.cargo.document.CrownMapping;

public class CrownMappingRepoCustomImpl implements CrownMappingRepoCustom {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public List<CrownMapping> getCurrentCrownTypeByVendor(String vendorId, CROWNTYPE... type) {
		SortOperation sortByVersionAsc = Aggregation.sort(new Sort(Direction.DESC, "version"));	
		GroupOperation getMappingWithMaxVersion = Aggregation.group("crown").first("version").as("version").first("crown").as("crown").first("price").as("price");
		Criteria criteria ;
		if(type.length == 0) {
		criteria = Criteria.where("price").gt(0).and("vendor.$id").is(new ObjectId(vendorId));
		}
		else {
			criteria = Criteria.where("price").gt(0).and("vendor.$id").is(new ObjectId(vendorId)).and("crown").is(type[0]);
		}
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria),sortByVersionAsc,getMappingWithMaxVersion);
		AggregationResults<CrownMapping> result = mongoTemplate.aggregate(aggregation, "crownMapping",CrownMapping.class);
		return result.getMappedResults();
	}

	@Override
	public CrownMapping getLatestCrownMappingByVendor(String vendorId, CROWNTYPE type) {
		List<CrownMapping> crownMappings = getCurrentCrownTypeByVendor(vendorId, type);
		if(null != crownMappings && crownMappings.size() > 0) {
			return crownMappings.get(0);
		}
		else {
		return null;
		}
	}
}
