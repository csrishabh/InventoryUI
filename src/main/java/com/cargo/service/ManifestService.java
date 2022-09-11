package com.cargo.service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cargo.document.AppResponse;
import com.cargo.document.Manifest;
import com.cargo.document.Unit;
import com.cargo.document.UnitMapping;
import com.cargo.document.User;
import com.cargo.repo.ConsignmentRepo;
import com.cargo.repo.ManifestRepo;
import com.cargo.utility.Config;
import com.cargo.utility.StringConstant;

@Service
public class ManifestService {

	
	@Autowired
	private ManifestRepo manifestRepo;
	
	@Autowired
	private ConsignmentRepo consignmentRepo;
	
	@Autowired
	private UnitMappingService unitMappingService;
	
	@Autowired
	private CustomUserDetailsService userService;
	
	
	public AppResponse<String> createManifest(Manifest manifest){
		AppResponse<String> res = new AppResponse<>();
		try {
		if(!isValidate(manifest)) {
			res.setSuccess(false);
			res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return res;
		}
		
		boolean isAnyConDeliverd = consignmentRepo.isAnyConsignmentDeliverd(manifest.getConsignments());
		
		if(isAnyConDeliverd) {
			res.setSuccess(false);
			res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return res;
		}
		boolean isAnyConDeleted = consignmentRepo.isAnyConsignmentDeleted(manifest.getConsignments());
		
		if(isAnyConDeleted) {
			res.setSuccess(false);
			res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return res;
		}
		else {
			manifest.setCreatedDate(Config.fomatDate(new Date()));
			manifest = manifestRepo.save(manifest);
			consignmentRepo.setConsignmentDeliverd(manifest.getConsignments(), manifest.getDes());
			consignmentRepo.updateManifest(manifest.getConsignments(), String.valueOf(manifest.getRefId()),false);
			res.setSuccess(true);
			res.setMsg(Arrays.asList(StringConstant.MANIFEST_CREATED_SUCCESS));
			res.setData(String.valueOf(manifest.getRefId()));
		}
		
		}
		catch (Exception e) {
			res.setSuccess(false);
			res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
		}
		
		return res;
	}
	
	public List<Document> getManifestHistory(Map<String, Object> filters){
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		List<Document> results = manifestRepo.searchManifest(filters);
		results.stream().forEach(r->{
			r.append("createdDate", dateFormat.format(r.getDate("createdDate")));
		    User user = userService.findUserByEmail(r.get("company", List.class).get(0).toString());
			UnitMapping mapping = unitMappingService.getUnitMapping(Unit.valueOf(r.getString("paidBy")), user.getId(), r.getInteger("unitComMappingVer").intValue());
			r.append("price", Config.format(mapping.getPrice(), Config.PRICE_FORMATTER));
		});
		return results;
	}
	
	private boolean isValidate(Manifest manifest) {

		if (null == manifest.getConsignments() || 0 == manifest.getConsignments().size() || null == manifest.getCompany()
				|| null == manifest.getDes()) {
			return false;
		}
		return true;
	}
	
	
	public AppResponse<Void> deleteManifest(String manifestId) {

		AppResponse<Void> res = new AppResponse<>();
		try {
			Manifest manifest = manifestRepo.getManifest(manifestId);
			if (manifest == null) {
				res.setSuccess(false);
				res.setMsg(Arrays.asList(StringConstant.MANIFEST_NOT_FOUND));
			} else {
				consignmentRepo.updateManifest(manifest.getConsignments(), manifestId, true);
				consignmentRepo.setConsignmentUnDeliverd(manifest.getConsignments(), manifest.getDes().toUpperCase());
				manifestRepo.deleteManifest(manifestId);
				res.setSuccess(true);
				res.setMsg(Arrays.asList(StringConstant.MANIFEST_DELETE_SUCCESS));
			}
		} catch (Exception e) {
			res.setSuccess(false);
			res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
		}
		return res;

	}
	
	public AppResponse<Void> deleteConsignmentFromManifest(String manifestId , String biltyNo) {

		AppResponse<Void> res = new AppResponse<>();
		try {
			Manifest manifest = manifestRepo.getManifest(manifestId);
			if (manifest == null) {
				res.setSuccess(false);
				res.setMsg(Arrays.asList(StringConstant.MANIFEST_NOT_FOUND));
			} else {
				if(manifest.getConsignments().contains(biltyNo)) {
				consignmentRepo.updateManifest(Arrays.asList(biltyNo), manifestId, true);
				consignmentRepo.setConsignmentUnDeliverd(manifest.getConsignments(), manifest.getDes().toUpperCase());
				if(manifest.getConsignments().size()==1) {
					manifestRepo.deleteManifest(manifestId);
				}
				else {
				manifestRepo.deleteConsignmentFromManifest(manifestId, biltyNo);
				}
				res.setSuccess(true);
				res.setMsg(Arrays.asList(StringConstant.MANIFEST_UPDATE_SUCCESS));
				}
				else {
					res.setSuccess(false);
					res.setMsg(Arrays.asList(StringConstant.CONSIGNMENT_NOT_FOUND));
				}
			}
		} catch (Exception e) {
			res.setSuccess(false);
			res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
		}
		return res;

	}
}
