package com.cargo.service;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cargo.document.AppResponse;
import com.cargo.document.Consignment;
import com.cargo.repo.ConsignmentRepo;
import com.cargo.utility.Config;
import com.cargo.utility.StringConstant;

@Service
public class ConsignmentService {

	@Autowired
	ConsignmentRepo repo;

	public AppResponse<Consignment> addConsignment(Consignment c) {
		AppResponse<Consignment> res = new AppResponse<>();
		try {
			if (isValidate(c)) {
				if(repo.isDuplicateConsignment(c.getBiltyNo().toUpperCase())) {
					res.setSuccess(false);
					res.setMsg(Arrays.asList(StringConstant.FOUND_DUPLICATE_CONSIGNMENT));
					return res;
				}
				c.setBookingDate(Config.fomatDate(c.getBookingDate()));
				c.setBiltyNo(c.getBiltyNo().toUpperCase());
				c = repo.save(c);
				res.setData(c);
				res.setSuccess(true);
				res.setMsg(Arrays.asList(StringConstant.CONSIGNMENT_CREATED_SUCCESS));
			} else {
				res.setSuccess(false);
				res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			}
		} catch (Exception e) {
			res.setSuccess(false);
			res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
		}

		return res;

	}
	
	public List<Document> getConsignmentHistory(Map<String, Object> filters){
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		final DecimalFormat decimalFormat = new DecimalFormat("#.##");
		List<Document> results = repo.searchConsignment(filters);
		results.stream().forEach(r->{
			double total = r.getDouble("total");
			double tax = r.getDouble("tax");
			double discount = r.getDouble("discount");
			double other1 = r.getDouble("other1");
			double other2 = r.getDouble("other2");
			total = total+other1+other2;
			double totalTax = ((total-discount)*tax)/100;
			double subTotal = total-discount;
			double grandTotal = subTotal+totalTax;
			r.getDate("bookingDate");
			r.append("subTotal", decimalFormat.format(subTotal));
			r.append("grandTotal", decimalFormat.format(grandTotal));
			r.append("bookingDate", dateFormat.format(r.getDate("bookingDate")));
			if(r.getString("unit").equals("KILOGRAM")) {
				r.append("rate", r.getDouble("rate")+"/Kg");
			}else {
				r.append("rate", r.getDouble("rate")+"/Parcel");
			}
		});
		return results;
	}
	
	public AppResponse<Void> deletedConsignment(String biltyNo) {
		
		AppResponse<Void> res = new AppResponse<>();
		Consignment consignment = repo.getConsignment(biltyNo);
		if(consignment==null) {
			res.setSuccess(false);
			res.setMsg(Arrays.asList(StringConstant.CONSIGNMENT_NOT_FOUND));
			return res;
		}
		if(null != consignment.getManiFestRefNo() && consignment.getManiFestRefNo().size() >0) {
			res.setSuccess(false);
			res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return res;
		}
		else {
			repo.deleteConsignment(biltyNo);
			res.setSuccess(true);
			res.setMsg(Arrays.asList(StringConstant.CONSIGNEMT_DELETE_SUCCESS));
			return res;
		}
		
	}
	

	private boolean isValidate(Consignment c) {

		if (null == c.getConsignee() || null == c.getConsignor() || null == c.getBillingType()
				|| null == c.getBookingDate()) {
			return false;
		}
		return true;
	}
}
