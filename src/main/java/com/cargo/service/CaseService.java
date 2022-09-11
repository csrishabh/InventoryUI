package com.cargo.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cargo.document.AppResponse;
import com.cargo.document.Case;
import com.cargo.document.CaseSearchResult;
import com.cargo.document.CaseStatus;
import com.cargo.document.CaseSubStatus;
import com.cargo.document.User;
import com.cargo.repo.CaseRepo;
import com.cargo.utility.Config;
import com.cargo.utility.StringConstant;

@Service
public class CaseService {
	
	@Autowired
	private CaseRepo caseRepo;
	
	@Autowired
	private CustomUserDetailsService userService;
	
	
	public AppResponse<Case> saveNewCase(Case report) {
		AppResponse<Case> response = new AppResponse<>();
		try {
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		report.setStatus(CaseStatus.BOOKED);
		report.setCreatedBy(userId);
		if(null == report.getBookingDate()) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.BOOKING_DATE_NOT_FOUND));
			return response;	
		}
		if(null == report.getAppointmentDate()) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.APPOINTMENT_DATE_NOT_FOUND));
			return response;
		}
		if(null == report.getDeliveredDate()){
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.DELIVERY_DATE_NOT_FOUND));
			return response;
		}
		if(null == report.getOpdNo()){
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.OPD_NO_NOT_FOUND));
			return response;
		}
		if(null == report.getCrown() || null == report.getCrown().getDetails() 
				|| report.getCrown().getDetails().size() == 0) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.CROWN_NOT_FOUND));
			return response;
		}
		
		Case duplicateCase = caseRepo.getCaseByOpdAndVersionNo(report.getOpdNo().toUpperCase(), 1 , Config.fomatDate(report.getBookingDate()));
		
		if(null != duplicateCase) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.FOUND_DUPLICATE_CASE));
			return response;
		}
		
		AppResponse<Case> dCase = getPatientNameByOpdNo(report.getOpdNo());
		
		if(dCase.isSuccess() && !report.getPatient().equalsIgnoreCase(dCase.getData().getPatient())) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.FOUND_DUPLICATE_CASE));
			return response;
		}
		
		report.setOpdNo(report.getOpdNo().toUpperCase());
		report.setBookingDate(Config.fomatDate(report.getBookingDate()));
		report.setDeliveredDate(Config.fomatDate(report.getDeliveredDate()));
		report.setUpdateBy(userId);
		report.setUpdateDate(Config.fomatDate(new Date()));
		report.setVersion(1);
		report.setRemark(StringConstant.INITIAL_ENTRY);
		report = caseRepo.save(report);	
		response.setData(report);
		response.setSuccess(true);
		response.setMsg(Arrays.asList(StringConstant.CASE_CREATED_SUCCESS));
		}
		catch (Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			return response;
		}
		return response;
	}
	
	
	public AppResponse<Void> updateCase(Case report){
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		AppResponse<Void> response = new AppResponse<>();
		try {
			Case c = caseRepo.findById(report.getId()).get();
			if(c.getStatus().equals(CaseStatus.CANCELED) || !c.getOpdNo().equals(report.getOpdNo()) || !DateUtils.isSameDay(c.getBookingDate(), report.getBookingDate()) || !c.getPatient().equalsIgnoreCase(report.getPatient())) {
				response.setSuccess(false);
				response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			}
			else if(c.getVersion()!= report.getVersion()) {
				response.setSuccess(false);
				response.setMsg(Arrays.asList(StringConstant.CASE_ALREADY_UPDATED));
			}
			else if (!c.getVender().getId().equals(report.getVender().getId())) {
				response.setSuccess(false);
				response.setMsg(Arrays.asList(StringConstant.VENDOR_CHANGE_NOT_ALLOWED));
			}
			else {
				report.setId(null);
				report.setUpdateBy(userId);
				report.setUpdateDate(Config.fomatDate(new Date()));
				report.setVersion(report.getVersion()+1);
				caseRepo.save(report);
				response.setSuccess(true);
				response.setMsg(Arrays.asList(report.getOpdNo() + StringConstant.CASE_UPDATED_SUCCESS));
			}
		}
		catch (Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
		}
		
		return response;
	}
	
	public List<CaseSearchResult> getVendorPayment(Map<String, Object> filters){
		List<CaseSearchResult> results = caseRepo.getVendorReport(filters);
		final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy hh.mm aa");
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		results.stream().forEach(c ->{
			Case latestStatus = getLatestCase(c.getCase().getOpdNo(), dateFormat.format(c.getCase().getBookingDate())).getData();
			c.getCase().setStatus(latestStatus.getStatus());
			c.getCase().setSubStatus(latestStatus.getSubStatus());
			c.getCase().setCrown(latestStatus.getCrown());
			try {
					if(caseRepo.isPaidBefore(c.getCase().getOpdNo(),c.getCase().getBookingDate(), (String)filters.get("vender"), Config.fomatDate(dateFormat.parse((String) filters.get("updateDate1"))))) {
						c.setAlreadyPaid(true);
					}
			} catch (ParseException e1) {
			}
			c.setAppointmentDate(dateTimeFormat.format(c.getCase().getAppointmentDate()));
			c.setBookingDate(dateFormat.format(c.getCase().getBookingDate()));
			c.setDeliverdDate(dateFormat.format(c.getCase().getDeliveredDate()));
			c.setPatientName(c.getCase().getPatient());
			c.setVendorName(c.getCase().getVender().getFullname());
			c.setDoctorName(c.getCase().getDoctor().getFullname());
			c.setStatus(c.getCase().getStatus().getName());
			c.setCreatedBy(userService.findUserByEmail(c.getCase().getCreatedBy()).getFullname());
			c.setSubStatus(c.getCase().getSubStatus().getName());
			c.setCrownDetails(c.getCase().getCrown().toString());
			c.setId(c.getCase().getOpdNo());
			c.setCrown(c.getCase().getCrown());
			c.setCase(null);
		});
		return results;
	}
	
	public List<CaseSearchResult> getCaseHistory(Map<String, Object> filters){
		
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userService.findUserByEmail(userId);
		List<CaseSearchResult> cases ;
		if(filters.get("status") == null || filters.get("status").equals("ALL") ) {
			filters.put("status", CaseStatus.DELIVERD+","+CaseStatus.INPROCESS+","+CaseStatus.BOOKED);
		}
		if(user.getRoles().contains("USER_CASE")) {
			cases = caseRepo.findAllLatestCase(filters);
		}
		else if(user.getRoles().contains("VENDOR")) {
			filters.put("vender", user.getId());
			cases = caseRepo.findAllLatestCase(filters);
		}
		else {
		cases = new ArrayList<>();
		}
		final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy hh.mm aa");
		final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		cases.stream().forEach(c ->{
			c.setAppointmentDate(dateTimeFormat.format(c.getCase().getAppointmentDate()));
			c.setBookingDate(dateFormat.format(c.getCase().getBookingDate()));
			c.setDeliverdDate(dateFormat.format(c.getCase().getDeliveredDate()));
			c.setPatientName(c.getCase().getPatient());
			c.setVendorName(c.getCase().getVender().getFullname());
			c.setDoctorName(c.getCase().getDoctor().getFullname());
			c.setStatus(c.getCase().getStatus().getName());
			c.setCreatedBy(userService.findUserByEmail(c.getCase().getCreatedBy()).getFullname());
			c.setSubStatus(c.getCase().getSubStatus().getName());
			c.setCrownDetails(c.getCase().getCrown().toString());
			c.setId(c.getCase().getOpdNo());
			c.setCrown(c.getCase().getCrown());
			c.setCase(null);
		});
		return cases;
		
	}
	
	public AppResponse<Document> getLateCaseCount(){
		
		AppResponse<Document> response = new AppResponse<>();
		try {
			List<Document> doc = caseRepo.findLateCaseCount();
			response.setSuccess(true);
			if(null != doc && doc.size() > 0 ) {
				response.setData(doc.get(0));
			}
			else if(doc.size() == 0){
				Document d = new Document();
				d.append("count", 0);
				response.setData(d);
			}	
		}
		catch (Exception e) {
			response.setSuccess(false);
		}
		
		return response;
	}
	
	
	public AppResponse<List<CaseSearchResult>> getCaseHistoryByOpdNo(String opdNo, String date){
		AppResponse<List<CaseSearchResult>> response = new AppResponse<>();
		try {
			final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy hh.mm aa");
			final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			String userId = SecurityContextHolder.getContext().getAuthentication().getName();
			Date bookingDate = dateFormat.parse(date);
			User user = userService.findUserByEmail(userId);
			List<CaseSearchResult> result = new ArrayList<CaseSearchResult>();
				Sort sort = new Sort(Sort.Direction.DESC, "version");
				List<Case> cases = caseRepo.getCaseHistroy(opdNo,bookingDate,sort);
				cases.stream().forEach(c ->{
					CaseSearchResult r = new CaseSearchResult();
					r.setAppointmentDate(dateTimeFormat.format(c.getAppointmentDate()));
					r.setBookingDate(dateFormat.format(c.getBookingDate()));
					r.setDeliverdDate(dateFormat.format(c.getDeliveredDate()));
					r.setUpdateDate(dateFormat.format(c.getUpdateDate()));
					r.setPatientName(c.getPatient());
					r.setVendorName(c.getVender().getFullname());
					r.setDoctorName(c.getDoctor().getFullname());
					r.setStatus(c.getStatus().getName());
					r.setUpdateBy(userService.findUserByEmail(c.getUpdateBy()).getFullname());
					r.setCrownDetails(c.getCrown().toString());
					r.setId(c.getOpdNo());
					r.setRemark(c.getRemark());
					r.setCrown(c.getCrown());
					r.setSubStatus(c.getSubStatus().getName());
					result.add(r);
				});
				response.setSuccess(true);
			    response.setData(result);
		}
		catch (Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
		}
		return response;
	}
	
	public AppResponse<List<CaseSearchResult>> getAllLateCase(){
		AppResponse<List<CaseSearchResult>> response = new AppResponse<>();
		try {
			List<CaseSearchResult> cases = caseRepo.findAllLateCase();
			final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy hh.mm aa");
			final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			cases.stream().forEach(c ->{
				c.setAppointmentDate(dateTimeFormat.format(c.getCase().getAppointmentDate()));
				c.setBookingDate(dateFormat.format(c.getCase().getBookingDate()));
				c.setDeliverdDate(dateFormat.format(c.getCase().getDeliveredDate()));
				c.setPatientName(c.getCase().getPatient());
				c.setVendorName(c.getCase().getVender().getFullname());
				c.setDoctorName(c.getCase().getDoctor().getFullname());
				c.setStatus(c.getCase().getStatus().getName());
				c.setCreatedBy(userService.findUserByEmail(c.getCase().getCreatedBy()).getFullname());
				c.setActions(c.getCase().getnextActions());
				c.setCrownDetails(c.getCase().getCrown().toString());
				c.setId(c.getCase().getOpdNo());
				c.setCrown(c.getCase().getCrown());
				c.setSubStatus(c.getCase().getSubStatus().getName());
				c.setCase(null);
			});
			response.setSuccess(true);
		    response.setData(cases);	
		}
		catch (Exception e) {
			response.setSuccess(false);
		}
		
		return response;
	}
	
	public AppResponse<Map<String, CaseStatus>> changeCaseStatus(Map<String, CaseStatus> cases){
		
		AppResponse<Map<String, CaseStatus>> response = new AppResponse<>();
		Map<String, CaseStatus> failed = new HashMap<>();
		String userId = SecurityContextHolder.getContext().getAuthentication().getName();
		try {
			
			cases.forEach((key,value) ->{
				List<CaseSearchResult> c = caseRepo.findLatestCase(key);
				if(c.size() >0) {
					if(c.get(0).getCase().getStatus() == value || !c.get(0).getCase().getnextActions().contains(value)){
						failed.put(key, value);
					}
					else {
						Case report = c.get(0).getCase();
						report.setId(null);
						report.setStatus(value);
						report.setUpdateBy(userId);
						report.setUpdateDate(Config.fomatDate(new Date()));
						report.setVersion(report.getVersion()+1);
						caseRepo.save(report);
					}
				}
			 
			});
			if(failed.size()>0) {
			response.setSuccess(false);
			response.setData(failed);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			}
			else {
			response.setSuccess(true);
			response.setMsg(Arrays.asList(StringConstant.DONE));
			}
		}
		catch (Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			
		}
		return response;
		
	}
	
	public AppResponse<Case> getLatestCase(String OpdNo , String date){
		AppResponse<Case> response = new AppResponse<>();
		Map<String, Object> filter = new HashMap<>();
		try {
		filter.put("opdNo", OpdNo);
		filter.put("bookingDate1", date);
		List<CaseSearchResult> cases =  caseRepo.findAllLatestCase(filter);
		if(null != cases && cases.size() > 0) {
		Case c = cases.get(0).getCase();
		response.setSuccess(true);
		response.setData(c);
		}
		else {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.CASE_NOT_FOUND));
			
		}
		}
		catch (Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
		}
		return response;
	}
	
	public AppResponse<Case> getPatientNameByOpdNo(String OpdNo){
		AppResponse<Case> response = new AppResponse<>();
		Map<String, Object> filter = new HashMap<>();
		try {
		filter.put("opdNo", OpdNo);
		List<CaseSearchResult> cases =  caseRepo.findAllLatestCase(filter);
		if(null != cases && cases.size() > 0) {
		Case c = cases.get(0).getCase();
		c.setId(null);
		c.setRemark("");
		c.setStatus(CaseStatus.BOOKED);
		c.setSubStatus(CaseSubStatus.NONE);
		response.setSuccess(true);
		response.setData(c);
		}
		else {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.CASE_NOT_FOUND));
			
		}
		}
		catch (Exception e) {
			response.setSuccess(false);
			response.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
		}
		return response;
	}

}
