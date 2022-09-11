package com.cargo.repo;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.cargo.document.Assigned;
import com.cargo.document.Cart;
import com.cargo.document.Carted;
import com.cargo.document.Product;
import com.cargo.document.TransctionType;
import com.cargo.utility.Config;
import com.mongodb.BasicDBObject;

@Repository
public class ProductRepoImpl implements ProductRepoCustom {
	
	 @Autowired 
	 MongoTemplate  mongoTemplate;
	 
	 
	 public Product updateProduct(Product product, TransctionType type , double qty){
		 
		 	Query query = new Query();
			Update update = new Update();
			
			if(type == TransctionType.ADD) {
			query.addCriteria(Criteria.where("id").is(product.getId()));	
			update.inc("qtyAblBack", (long)(qty*Config.QTY_FORMATTER));
			update.set("lstAddBy", product.getLstAddBy());
			update.set("lstAddDate", product.getLstAddDate());
			}
			else if(type == TransctionType.AUDIT) {
				query.addCriteria(Criteria.where("id").is(product.getId()));
				update.set("lstAdtBy", product.getLstAdtBy());
				update.set("lstAdtDate", new Date());
				update.set("lastPriceBack", product.getLastPriceBack());
			}
			else if(type == TransctionType.DISPATCH){
				query.addCriteria(Criteria.where("id").is(product.getId()).and("qtyAblBack").gte((long)(qty*Config.QTY_FORMATTER)));
				update.inc("qtyAblBack", (long	)(-qty*Config.QTY_FORMATTER));
			}
			else if(type == TransctionType.REVERT){
				query.addCriteria(Criteria.where("id").is(product.getId()));
				update.inc("qtyAblBack", (long)(qty*Config.QTY_FORMATTER));
			}
			FindAndModifyOptions options = new FindAndModifyOptions();
			options.returnNew(true);
			return mongoTemplate.findAndModify(query, update, options,Product.class);
	 }
	 
	 public Product addCarted(Product product, TransctionType type , double qty, Cart cart){
		 
		 	Query query = new Query();
			Update update = new Update();
			
			if(type == TransctionType.DISPATCH){
				query.addCriteria(Criteria.where("id").is(product.getId()).and("qtyAblBack").gte((long)(qty*Config.QTY_FORMATTER)));
				update.inc("qtyAblBack", (long	)(-qty*Config.QTY_FORMATTER));
				Carted c = new Carted();
				c.setCartId(cart.getId());
				c.setQtyBack((long)(qty*Config.QTY_FORMATTER));
				c.setTimeStamp(Config.fomatDate(new Date()));
				update.push("carted", c);
			}
			FindAndModifyOptions options = new FindAndModifyOptions();
			options.returnNew(true);
			return mongoTemplate.findAndModify(query, update, options,Product.class);
	 }

	@Override
	public Product editCarted(Product product, TransctionType type, double qty, Cart cart) {
		
		Query query = new Query();
		Update update = new Update();
		
		if(type == TransctionType.DISPATCH){
			query.addCriteria(Criteria.where("id").is(product.getId()).and("qtyAblBack").gte((long)(qty*Config.QTY_FORMATTER)).and("carted.cartId").is(cart.getId()));
			update.inc("qtyAblBack", (long	)(-qty*Config.QTY_FORMATTER));
			update.inc("carted.$.qtyBack", (long)(qty*Config.QTY_FORMATTER));
			update.set("carted.$.timeStamp", Config.fomatDate(new Date()));
			
		}
		FindAndModifyOptions options = new FindAndModifyOptions();
		options.returnNew(true);
		return mongoTemplate.findAndModify(query, update, options,Product.class);
	}


	@Override
	public void deleteCarted(String productId, String cartId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("id").is(productId));
		Update update = new Update();
		BasicDBObject carted = new BasicDBObject("cartId",cartId);
		update.pull("carted", carted);
		FindAndModifyOptions options = new FindAndModifyOptions();
		options.returnNew(true);
		mongoTemplate.upsert(query, update, Product.class);
	}


	@Override
	public Product addAssigned(String userId, String productId, double qty) {
		
		Query query = new Query();
		Update update = new Update();
		query.addCriteria(Criteria.where("id").is(productId).and("qtyAblBack").gte((long)(qty*Config.QTY_FORMATTER)).and("assigned.userId").is(userId));
		if(mongoTemplate.find(query, Product.class)!=null) {
			update.inc("qtyAblBack", (long)(-qty*Config.QTY_FORMATTER));
			update.inc("assigned.$.qtyBack", (long)(qty*Config.QTY_FORMATTER));
			FindAndModifyOptions options = new FindAndModifyOptions();
			options.returnNew(true);
			return mongoTemplate.findAndModify(query, update, options,Product.class);
		}
		else {
			query.addCriteria(Criteria.where("id").is(productId).and("qtyAblBack").gte((long)(qty*Config.QTY_FORMATTER)));
			update.inc("qtyAblBack", (long	)(-qty*Config.QTY_FORMATTER));
			Assigned assign = new Assigned();
			assign.setUserId(userId);
			assign.setQtyBack((long	)(qty*Config.QTY_FORMATTER));
			update.push("assigned", assign);
			FindAndModifyOptions options = new FindAndModifyOptions();
			options.returnNew(true);
			return mongoTemplate.findAndModify(query, update, options,Product.class);
		}
		
		
	}
	
	public List<Product> getAllProduct(){
		
		Sort sort = Sort.by(Direction.ASC, "name");
		Query query = new Query();
		query.with(sort);
		query.collation(Collation.of("en").strength(Collation.ComparisonLevel.secondary()));
		return mongoTemplate.find(query, Product.class);
	}


	public Page<Product> SearchProduct(int pageNo, boolean reverseOrder, String orderBy,String name) {
		Pageable p;
		if(orderBy!=null && !orderBy.equals("")) {
			if(reverseOrder) {
				p = PageRequest.of(pageNo, 10, Direction.DESC,orderBy.trim());
			}
			else {
				p = PageRequest.of(pageNo, 10, Direction.ASC,orderBy.trim());
			}
		}
		else {
			p = PageRequest.of(pageNo, 10);
		}
		Query query = new Query();
		query.addCriteria(Criteria.where("enabled").is(true));
		if(name!=null && !name.equals("")) {
		query.addCriteria(Criteria.where("name").regex(name, "i"));
		}
		query.with(p);
		query.collation(Collation.of("en").strength(Collation.ComparisonLevel.secondary()));
		long count = mongoTemplate.getCollection("product").count();
		Page<Product> page = new PageImpl<>(mongoTemplate.find(query, Product.class), p, count);
		return page;
	}

}
