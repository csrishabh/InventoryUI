package com.cargo.utility;

import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;

public class Config {
	
	
	public static final double QTY_FORMATTER = 1000.0;
	public static final double PRICE_FORMATTER = 100.0;
	
	
	public static double format(long value , double formetter) {
		double v = value / formetter;
		v = Math.round(v * formetter) / formetter;
		return v;
	}
	
	public static Date fomatDate(Date date) {
		
		date = DateUtils.setHours(date, 0);
		date = DateUtils.setMinutes(date, 0);
		date = DateUtils.setSeconds(date, 0);
		date = DateUtils.setMilliseconds(date, 0);
		return date;
	}

}
