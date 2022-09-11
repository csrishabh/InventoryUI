package com.cargo.document;

import java.util.ArrayList;
import java.util.List;

public class Crown {
	
	private int count;
	private List<CrownDetail> details = new ArrayList<CrownDetail>();
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public List<CrownDetail> getDetails() {
		return details;
	}
	public void setDetails(List<CrownDetail> details) {
		this.details = details;
	}
	@Override
	public String toString() {
		String s = "Details: "+details;
		return s;
		//return "Crown [count=" + count + ", shade=" + shade + ", details=" + details + "]";
	}
	
}
