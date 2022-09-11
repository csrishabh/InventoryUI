package com.cargo.service;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cargo.document.AppResponse;
import com.cargo.document.CommonData;
import com.cargo.repo.CommonDataRepo;
import com.cargo.utility.StringConstant;

@Service
public class CommonDataService {

	@Autowired
	private CommonDataRepo repo;
	
	
	public AppResponse<Void> updateStatus(CommonData data) {
		
		AppResponse<Void> res = new AppResponse<>();
		if(data.getId()!=null) {
			try {
			CommonData commonData = repo.findById(data.getId()).get();
			commonData.setDisabled(data.isDisabled());
			res.setSuccess(true);
			res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			}
			catch(Exception e) {
				res.setSuccess(false);
				res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
			}
		}
		else {
			res.setSuccess(false);
			res.setMsg(Arrays.asList(StringConstant.TRY_AGAIN));
		}
		return res;
	}
}
