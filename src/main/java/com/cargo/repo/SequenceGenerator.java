package com.cargo.repo;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.cargo.document.DatabaseSequence;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;

@Repository
public class SequenceGenerator {

	@Autowired
	private MongoTemplate mongoTemplate;

	public long generateSequence(String seqName) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(seqName));
		DatabaseSequence counter = mongoTemplate.findAndModify(query, new Update().inc("seq", 1), options().returnNew(true).upsert(true),
				DatabaseSequence.class);
		return !Objects.isNull(counter) ? counter.getSeq() : 1;
	}

}
