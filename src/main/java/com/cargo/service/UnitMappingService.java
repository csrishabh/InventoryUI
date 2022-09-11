package com.cargo.service;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cargo.document.AppResponse;
import com.cargo.document.Unit;
import com.cargo.document.UnitMapping;
import com.cargo.document.User;
import com.cargo.repo.UnitMappingRepo;
import com.cargo.repo.UserRepository;
import com.cargo.utility.Config;
import com.cargo.utility.StringConstant;

@Service
public class UnitMappingService {

	@Autowired
	private UnitMappingRepo repo;
	
	@Autowired
	private UserRepository userRepo;
	
	public UnitMapping getUnitMapping(Unit type, String companyId, int version) {
		return repo.getUnitMappingByTypeAndCompany(type, new ObjectId(companyId), version);
	}

	public AppResponse<List<UnitMapping>> getUnitMappingByVendor(String vendorId) {

		AppResponse<List<UnitMapping>> response = new AppResponse<>();
		try {
			List<UnitMapping> mappings = repo.getCurrentUnitByCompany(vendorId);
			response.setData(mappings);
			response.setSuccess(true);
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.CROWN_MAPPING_NOT_FOUND));
		}
		return response;

	}
	
	public AppResponse<Void> saveUnitMapping(String companyId, Unit type, long price) {

		AppResponse<Void> response = new AppResponse<>();
		try {
			UnitMapping mapping = repo.getLatestUnitMappingByCompany(companyId, type);
			if (null == mapping) {
				mapping = new UnitMapping();
				mapping.setUnit(type);
				try {
					User vendor = userRepo.findById(companyId).get();
					mapping.setCompany(vendor);
					mapping.setPrice(price);
					mapping.setVersion(0);
					repo.save(mapping);
					response.setSuccess(true);
					response.setMsg(Arrays.asList("Unit Price added successfully"));
				} catch (NoSuchElementException ne) {
					response.setSuccess(false);
					response.setMsg(Arrays.asList("Company Not found"));
				}

			} else {
				try {
					User vendor = userRepo.findById(companyId).get();
					mapping.setCompany(vendor);
					mapping.setPrice(price);
					mapping.setVersion(mapping.getVersion() + 1);
					mapping.setId(null);
					repo.save(mapping);
					response.setSuccess(true);
					response.setMsg(Arrays.asList("Crown Price updated successfully"));
				} catch (NoSuchElementException ne) {
					response.setSuccess(false);
					response.setMsg(Arrays.asList("Vendor Not found"));
				}
			}
		} catch (Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
		}
		return response;
	}
	
	public AppResponse<Double> getLatestUnitPriceByVendor(String CompanyId,Unit type){
		AppResponse<Double> response = new AppResponse<>();
		try {
			UnitMapping mapping = repo.getLatestUnitMappingByCompany(CompanyId, type);
			if(null == mapping) {
				response.setData(new Double(0));
				response.setSuccess(true);
			}
			else {
				response.setData(new Double(Config.format(mapping.getPrice(), Config.PRICE_FORMATTER)));
				response.setSuccess(true);
			}
		}
		catch (Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
		}
		return response;
		
	}
	

}
