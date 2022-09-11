package com.cargo.service;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cargo.document.AppResponse;
import com.cargo.document.CROWNTYPE;
import com.cargo.document.CrownMapping;
import com.cargo.document.User;
import com.cargo.repo.CrownMappingRepo;
import com.cargo.repo.UserRepository;
import com.cargo.utility.Config;
import com.cargo.utility.StringConstant;

@Service
public class CrownMappingService {
	@Autowired
	private CrownMappingRepo repo;
	
	@Autowired
	private UserRepository userRepo;

	public CrownMapping getCrownMapping(CROWNTYPE type, String vendorId, int version) {
		return repo.getCrownMappingByTypeAndVendor(type, new ObjectId(vendorId), version);
	}
	
	public AppResponse<List<CrownMapping>> getCrownMappingByVendor(String vendorId){
		
		AppResponse<List<CrownMapping>> response = new AppResponse<>();
		try {
		List<CrownMapping> mappings = repo.getCurrentCrownTypeByVendor(vendorId);
		response.setData(mappings);
		response.setSuccess(true);
		}
		catch (Exception e) {
			response.setSuccess(false);	
			response.setMsg(Arrays.asList(StringConstant.CROWN_MAPPING_NOT_FOUND));
		}
		return response;
		
	}
	
	public AppResponse<Void> saveCrownMapping(String vendorId, CROWNTYPE type, long price) {

		AppResponse<Void> response = new AppResponse<>();
		try {
			CrownMapping mapping = repo.getLatestCrownMappingByVendor(vendorId, type);
			if (null == mapping) {
				mapping = new CrownMapping();
				mapping.setCrown(type);
				try {
					User vendor = userRepo.findById(vendorId).get();
					mapping.setVendor(vendor);
					mapping.setPrice(price);
					mapping.setVersion(0);
					repo.save(mapping);
					response.setSuccess(true);
					response.setMsg(Arrays.asList("Crown Price added successfully"));
				} catch (NoSuchElementException ne) {
					response.setSuccess(false);
					response.setMsg(Arrays.asList("Vendor Not found"));
				}

			} else {
				try {
					User vendor = userRepo.findById(vendorId).get();
					mapping.setVendor(vendor);
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
	
	public AppResponse<Double> getLatestCrownPriceByVendor(String vendorId,CROWNTYPE type){
		AppResponse<Double> response = new AppResponse<>();
		try {
			CrownMapping mapping = repo.getLatestCrownMappingByVendor(vendorId, type);
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
