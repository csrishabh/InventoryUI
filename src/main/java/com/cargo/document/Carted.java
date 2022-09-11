package com.cargo.document;

import java.util.Date;

import org.springframework.data.annotation.Transient;

public class Carted {
	
	private String cartId;
	@Transient
	private double qty;
	private long qtyBack;
	private Date timeStamp;
	public String getCartId() {
		return cartId;
	}
	public void setCartId(String cartId) {
		this.cartId = cartId;
	}
	public double getQty() {
		return qty;
	}
	public void setQty(double qty) {
		this.qty = qty;
	}
	public long getQtyBack() {
		return qtyBack;
	}
	public void setQtyBack(long qtyBack) {
		this.qtyBack = qtyBack;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
}
