package com.cargo.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cargo.document.AppResponse;
import com.cargo.document.Product;
import com.cargo.repo.CrownMappingRepo;
import com.cargo.repo.ProductRepo;
import com.cargo.repo.UserRepository;
import com.cargo.service.EmailService;
import com.cargo.service.ProductService;
import com.cargo.utility.Config;
import com.cargo.utility.StringConstant;

@RestController
@RequestMapping("/backend")
public class ProductController {

	@Autowired
	ProductRepo repo;
	
	@Autowired
	ProductService productService;
	
	@Autowired
	EmailService emailService;
	
	@PostMapping("/addProduct")
	public ResponseEntity<Product> addProduct(@RequestBody Product item) {
		try {	
		List<Product> products = repo.findByNameIgnoreCase(item.getName());
		if(products.size() == 0) {
		item.setName(item.getName().toUpperCase());	
		item.setAlertBack((long)(item.getAlert()*Config.QTY_FORMATTER));
		item.setEnabled(true);
		item = repo.save(item);
		return new ResponseEntity<Product>(item, HttpStatus.CREATED);
		}
		else {
			return new ResponseEntity<Product>(HttpStatus.ALREADY_REPORTED);
		}
		}
		catch(Exception e) {
			return new ResponseEntity<Product>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@PostMapping("/update/product")
	public AppResponse<String> updateProduct(@RequestBody Product item) {
		AppResponse<String> response = new AppResponse<>();
		try {
		Product p = repo.findById(item.getId()).get();
		p.setName(item.getName().toUpperCase());
		p.setAlertBack((long)(item.getAlert()*Config.QTY_FORMATTER));
		repo.save(p);
		response.setSuccess(true);
		response.setMsg(Arrays.asList(StringConstant.PRODUCT_UPDATED_SUCCESS));
		}
		catch (NullPointerException e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.PRODUCT_NOT_FOUND));
		}
		catch (Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
		}
		return response;
	}
	
	@PostMapping("/update/product/status")
	public AppResponse<String> updateProductStatus(@RequestBody Product item) {
		AppResponse<String> response = new AppResponse<>();
		try {
		Product p = repo.findById(item.getId()).get();
		p.setEnabled(item.isEnabled());
		repo.save(p);
		response.setSuccess(true);
		response.setMsg(Arrays.asList(StringConstant.PRODUCT_UPDATED_SUCCESS));
		}
		catch (NullPointerException e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.PRODUCT_NOT_FOUND));
		}
		catch (Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
		}
		return response;
	}
	
	

	@GetMapping("/products")
	public List<Product> getAllProducts(@RequestParam Map<String, Object> filters) {
		List<Product> items = new ArrayList<>();
		if(null != filters && null != filters.get("name")) {
			String regex = (String) filters.get("name");
			items = formatProducts(repo.findByNameStartingWith(regex));
		}
		else {
		items = formatProducts(repo.getAllProduct());
		}
		return items;
	}
	
	@GetMapping("/product/{id}")
	public Product getProduct(@PathVariable("id") String id) {
		Product product = formatProducts(Arrays.asList(repo.findById(id).get())).get(0);
		return product;
	}
	
	@GetMapping("/products/{name}")
	public List<Product> getProductByName(@PathVariable("name") String name) {
		if(name.trim().equals("")) {
			return new ArrayList<>();
		}
		List<Product> products = formatProducts(repo.findByNameStartingWith(name.trim()));
		return products;
	}
	
	
	private List<Product> formatProducts(List<Product> products){
		
		products.stream().forEach(product ->{
			product.setQtyAbl(Config.format(product.getQtyAblBack(),Config.QTY_FORMATTER));
			product.setAlert(Config.format(product.getAlertBack(),Config.QTY_FORMATTER));
			product.setLastPrice(Config.format(product.getLastPriceBack(),Config.PRICE_FORMATTER));
		});
		return products;
	}
	
	@GetMapping("/searchProducts/{pageNo}")
	public Page<Product> getProducts(@PathVariable("pageNo") int pageNo, @RequestParam(value = "reverseSort", required=false) final boolean reverseSort,
			@RequestParam(value = "orderBy", required=false) final String orderByField , @RequestParam(value = "name", required=false) final String name) {
		Page<Product> products = productService.SearchProducts(pageNo,reverseSort,orderByField,name);
		formatProducts(products.getContent());
		return products;
	}
	
}
