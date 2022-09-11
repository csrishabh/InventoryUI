package com.cargo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.cargo.document.Product;
import com.cargo.repo.ProductRepo;

@Service
public class ProductService {
	
	@Autowired
	ProductRepo repo;
	
	public Page<Product> SearchProducts(int pageNo, boolean reverseOrder, String orderBy,String name) {
		
		return repo.SearchProduct(pageNo, reverseOrder, orderBy, name);
	}
	

}
