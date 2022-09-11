package com.cargo.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cargo.document.AppResponse;
import com.cargo.document.Cart;
import com.cargo.document.Order;
import com.cargo.document.Product;
import com.cargo.document.Transction;
import com.cargo.document.TransctionType;
import com.cargo.document.User;
import com.cargo.repo.CartRepo;
import com.cargo.repo.OrderRepo;
import com.cargo.repo.ProductRepo;
import com.cargo.repo.TransctionRepo;
import com.cargo.service.CustomUserDetailsService;
import com.cargo.service.EmailService;
import com.cargo.utility.Config;
import com.cargo.utility.StringConstant;

@RestController
@RequestMapping("/backend")
public class TransctionController {

	@Autowired
	private TransctionRepo tRepo;

	@Autowired
	private ProductRepo pRepo;
	
	@Autowired
	
	private CartRepo cartRepo;
	
	@Autowired 
	private CustomUserDetailsService userService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	OrderRepo orderRepo;

	
	@PostMapping("/delTransction")
	public ResponseEntity<Transction> deleteTrasction(@RequestBody Transction transction) {
		try {
			if(transction.getId()!=null) {
				transction = tRepo.findById(transction.getId()).get();
				transction.setDltBy(SecurityContextHolder.getContext().getAuthentication().getName());
				transction.setDeleted(true);
				Transction t = tRepo.save(transction);
				if(t == null) {
					return new ResponseEntity<Transction>(HttpStatus.NOT_ACCEPTABLE);
				}
				return new ResponseEntity<Transction>(HttpStatus.OK);
			}
			return new ResponseEntity<Transction>(HttpStatus.NOT_FOUND);
			
		}
		catch (Exception e) {
			return new ResponseEntity<Transction>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PreAuthorize ("hasAuthority('ADMIN_INV')")
	@GetMapping("/audit/transction")
	public ResponseEntity<List<Object[]>> getAuditableTransctions(@RequestParam Map<String, Object> map) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		try {
			List<Transction> transctions = tRepo.getAuditPandingTransction(map);
			
			List<Object[]> response = new ArrayList<>();
			
			
			transctions.stream().forEach(t ->{
				Product p = pRepo.findById(t.getProductId()).get();
				User user = userService.findUserByEmail(t.getAddBy());
				Object[] arr = new Object[7];
				arr[0] = formatter.format(t.getDate());
				arr[1] = p.getName();
				arr[2] = Config.format(t.getQuantityBack(), Config.QTY_FORMATTER);
				arr[3] = Config.format(t.getAmountBack(), Config.PRICE_FORMATTER);
				arr[4]= user.getFullname();
				arr[5]= String.valueOf(Config.format(p.getLastPriceBack(), Config.PRICE_FORMATTER));
				arr[6]= t.getId();
				response.add(arr);
			});
			
			return new ResponseEntity<List<Object[]>>(response, HttpStatus.OK);
		}
		catch (Exception e) {
			return new ResponseEntity<List<Object[]>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@GetMapping("/transction")
	public AppResponse<List<Document>> getTransctions(@RequestParam Map<String, Object> map) {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		AppResponse<List<Document>> response = new AppResponse<>();
		try {
			User user = userService.findUserByEmail(userId);
			List<Document> transctions;
			if(user.getRoles().contains("ADMIN_INV")) {
				transctions = tRepo.getAllTransction(map);
			}
			else {
				transctions = tRepo.getTransctionByUser(map,userId);
			}
			response.setSuccess(true);
			response.setData(transctions);
			return response;
		}
		catch (Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return response;
		}
		
	}
	
	
	@PostMapping("/addTransctions")
	public ResponseEntity<List<Transction>> saveTransctions( @RequestBody List<Transction> transctions) {
		List<Transction> unSuccessFulTrns = new ArrayList<>();
		try {
			
			transctions.stream().forEach(t->{
			ResponseEntity<Transction> res =  addTranactionInCart(t);
			if(res.getStatusCode() == HttpStatus.NOT_ACCEPTABLE) {
				unSuccessFulTrns.add(res.getBody());
			}
			});
			if(unSuccessFulTrns.size()>0) {
				return new ResponseEntity<>(unSuccessFulTrns,HttpStatus.NOT_ACCEPTABLE);
			}
			return new ResponseEntity<>(HttpStatus.OK);
		}
		catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
	@PostMapping("/audit")
	public ResponseEntity<Transction> auditTransction( @RequestBody Transction transction) {
		try {
			Transction trns = new Transction();
			Product product = new Product();
			trns.setId(transction.getId());
			double price = transction.getAmount() / transction.getQuantity();
			price = Math.round(price * 100.0) / 100.0;
			product.setLastPriceBack((long) (price * Config.PRICE_FORMATTER));
			product.setLstAdtBy(SecurityContextHolder.getContext().getAuthentication().getName());
		
			
			trns.setAmountBack((long)(transction.getAmount()*Config.PRICE_FORMATTER));
			trns.setQuantityBack((long)(transction.getQuantity()*Config.QTY_FORMATTER));
			trns.setAdtBy(SecurityContextHolder.getContext().getAuthentication().getName());
			trns.setType(TransctionType.AUDIT);
			trns = tRepo.updateTransction(trns);
			if(trns==null) {
				return new ResponseEntity<Transction>(HttpStatus.NOT_ACCEPTABLE);
			}
			product.setId(trns.getProductId());
			product = pRepo.updateProduct(product, TransctionType.AUDIT, transction.getQuantity());
			return new ResponseEntity<Transction>(trns, HttpStatus.OK);
		}
		catch (Exception e) {
			return new ResponseEntity<Transction>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
	@PostMapping("/update/carted")
	public ResponseEntity<Transction> updateTranactionFromCart(@RequestBody Transction transction) {
		
		try {
			Product product = new Product();
			product.setId(transction.getProductId());
				Cart cart = cartRepo.getCartByUserId(SecurityContextHolder.getContext().getAuthentication().getName());
				if(cart == null) {
					return new ResponseEntity<Transction>(HttpStatus.NOT_FOUND);
				}
			
				cart.setLastModifiedDate(new Date());
				Transction duplicateTrns = cart.getDuplicateTransction(transction);
				
				if(duplicateTrns == null) {
					return new ResponseEntity<Transction>(HttpStatus.NOT_FOUND);
				}
				double delta = transction.getQuantity()-Config.format(duplicateTrns.getQuantityBack(), Config.QTY_FORMATTER);
				if (transction.getType() == TransctionType.ADD) {
					product = pRepo.findById(product.getId()).get();
					
				} else if (transction.getType() == TransctionType.DISPATCH) {
						 product = pRepo.editCarted(product, TransctionType.DISPATCH, delta ,cart);
						 if(product == null) {
								return new ResponseEntity<Transction>(transction,HttpStatus.NOT_ACCEPTABLE);
						 }
						 if(product.getQtyAblBack() < product.getAlertBack()) {
								emailService.sendAlertMail(product);
						 }		 
				}
				if( transction.getQuantity() == 0) {
					cart.getTransctions().remove(duplicateTrns);
				}
				else {
				duplicateTrns.setQuantityBack((long)(transction.getQuantity()*Config.QTY_FORMATTER));
				}
				cartRepo.save(cart);
				return new ResponseEntity<Transction>(duplicateTrns,HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<Transction>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
	@PostMapping("/carted")
	public ResponseEntity<Transction> addTranactionInCart(@RequestBody Transction transction) {
		
		try {
			Product product = new Product();
			product.setId(transction.getProductId());
				transction.setAddBy(SecurityContextHolder.getContext().getAuthentication().getName());
				Cart cart = cartRepo.getCartByUserId(SecurityContextHolder.getContext().getAuthentication().getName());
				if(cart == null) {
					cart = new Cart();
					cart.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
					cart = cartRepo.save(cart);
				}
			
				cart.setLastModifiedDate(Config.fomatDate(new Date()));
				
				if (transction.getType() == TransctionType.ADD) {
					transction.setAdtable(true);
					product = pRepo.findById(product.getId()).get();
					
				} else if (transction.getType() == TransctionType.DISPATCH) {
					if(cart.getTransctions()!=null) {
					Optional<Transction> duplicateTrns = cart.getTransctions().stream()
							.filter(t -> t.getProductId().equals(transction.getProductId()) && t.getType().equals(TransctionType.DISPATCH)).findFirst(); 
					if (duplicateTrns.isPresent()) {
						product = pRepo.editCarted(product, transction.getType(), transction.getQuantity(), cart);
					} else {
						product = pRepo.addCarted(product, transction.getType(), transction.getQuantity(), cart);
					}
					}else {
						product = pRepo.addCarted(product, transction.getType(), transction.getQuantity(), cart);
					}
					if (product == null) {
						return new ResponseEntity<Transction>(transction, HttpStatus.NOT_ACCEPTABLE);
					}
					else if(product.getQtyAblBack() < product.getAlertBack()) {
						emailService.sendAlertMail(product);
					}
				}
				transction.setAmountBack((long)(transction.getAmount()*Config.PRICE_FORMATTER));
				transction.setQuantityBack((long)(transction.getQuantity()*Config.QTY_FORMATTER));
				transction.setProductName(product.getName());
				transction.setDate(Config.fomatDate(transction.getDate()));
				cart.addTransction(transction);
				cartRepo.save(cart);
				return new ResponseEntity<Transction>(HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<Transction>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@PostMapping("/assign/{userId}")
	public ResponseEntity<Transction> assignTranaction(@PathVariable("userId") String userId, @RequestBody Transction transction) {
		
		Product p = pRepo.addAssigned(userId, transction.getProductId(), transction.getQuantity());
		
		if(p==null) {
			return new ResponseEntity<Transction>(transction, HttpStatus.NOT_ACCEPTABLE);
		}
		else {
			return new ResponseEntity<Transction>(transction, HttpStatus.OK);
		}
	}
	
	@PostMapping("/revert")
	public ResponseEntity<Transction> revertTranaction(@RequestBody Transction transction) {

		String userId = SecurityContextHolder.getContext().getAuthentication().getName();

		Optional<Transction> trns = tRepo.findById(transction.getId());

		if (trns.isPresent()) {

			Transction orgTrns = trns.get();
			Product product = pRepo.findById(orgTrns.getProductId()).get();
			if (orgTrns.getType() == TransctionType.ADD) {

				pRepo.updateProduct(product, TransctionType.REVERT,
						-Config.format(orgTrns.getQuantityBack(), Config.QTY_FORMATTER));
			} else if (orgTrns.getType() == TransctionType.DISPATCH) {

				pRepo.updateProduct(product, TransctionType.REVERT,
						Config.format(orgTrns.getQuantityBack(), Config.QTY_FORMATTER));
			}
			orgTrns.setRemark(transction.getRemark());
			orgTrns.setDeleted(true);
			orgTrns.setDltBy(userId);
			orgTrns.setDltDate(Config.fomatDate(new Date()));
			tRepo.save(orgTrns);
			return new ResponseEntity<Transction>(orgTrns, HttpStatus.OK);
		} else {
			return new ResponseEntity<Transction>(HttpStatus.NOT_FOUND);
		}
	}
	
	
	@GetMapping("/getCart")
	public ResponseEntity<List<Transction>> getCart() {
		
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		
		if(userId != null) {
			Cart cart = cartRepo.getCartByUserId(userId);
			if(cart != null) {
			return new ResponseEntity<List<Transction>>(cart.getTransctions(),HttpStatus.OK);
			}
			else {
				return new ResponseEntity<List<Transction>>(HttpStatus.NOT_FOUND);
			}	
		}
		else {
			return new ResponseEntity<List<Transction>>(HttpStatus.NOT_FOUND);
		}
		
	}
	
	@PostMapping("/dispatch/Cart")
	public ResponseEntity<Void> dispatchCart() {
		
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		
		if(userId == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}	
		Cart cart = cartRepo.getCartByUserId(userId);
		if(cart == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
		}
		cart.getTransctions().stream().forEach(t->{
		if(t.getType() == TransctionType.DISPATCH) {	
		pRepo.deleteCarted(t.getProductId(), cart.getId());
		}else if(t.getType() == TransctionType.ADD){
		Product p = new Product();	
		p.setId(t.getProductId());
		p.setLstAddBy(userId);
		p.setLstAddDate(t.getDate());
		pRepo.updateProduct(p,t.getType(), Config.format(t.getQuantityBack(),Config.QTY_FORMATTER));	
		}
		});
		Order order = new Order();
		order.setUserId(userId);
		order.setTransctions(cart.getTransctions());
		order.setDate(Config.fomatDate(new Date()));
		orderRepo.save(order);
		cartRepo.delete(cart);	
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
	
}
