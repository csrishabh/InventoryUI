package com.cargo.document;

import java.util.ArrayList;
import java.util.List;

public class AppResponse<T>
{
	T data;
	boolean isSuccess;
	List<String> msg = new ArrayList<String>();
	
	public AppResponse() {
		
	}
	
	public AppResponse(T data) {
		this.data = data;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public List<String> getMsg() {
		return msg;
	}

	public void setMsg(List<String> msg) {
		this.msg = msg;
	}
	
}
