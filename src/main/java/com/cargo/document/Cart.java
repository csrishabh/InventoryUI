package com.cargo.document;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Cart {

	@Id
	private String id;
	
	@Indexed(unique = true, direction = IndexDirection.DESCENDING, dropDups = true)
	private String userId;
	
	private Date lastModifiedDate;
	
	private String status;
	
	private List<Transction> transctions;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Transction> getTransctions() {
		return transctions;
	}

	public void setTransctions(List<Transction> transctions) {
		this.transctions = transctions;
	}
	
	public void addTransction(Transction transction) {
		if (this.transctions == null) {
			this.transctions = new ArrayList<>();
		}
		Optional<Transction> duplicateTrns = this.transctions.stream().filter(
				t -> t.getProductId().equals(transction.getProductId()) && getDatePart(t.getDate()) == getDatePart(transction.getDate()) && t.getType().equals(transction.getType()))
				.findFirst();
		if (duplicateTrns.isPresent()) {
			duplicateTrns.get().setQuantityBack(duplicateTrns.get().getQuantityBack()+transction.getQuantityBack());
		} else {
			this.transctions.add(transction);
		}
	}
	
	public Transction getDuplicateTransction(Transction transction) {
		if (transction == null) {
			return null;
		}
		Optional<Transction> duplicateTrns = this.transctions.stream().filter(
				t -> t.getProductId().equals(transction.getProductId()) && getDatePart(t.getDate()) == getDatePart(transction.getDate()) && t.getType().equals(transction.getType()))
				.findFirst();
		if (duplicateTrns.isPresent()) {
			return duplicateTrns.get();
		} else {
			return null;
		}
	}
	
	private static long getDatePart(Date date) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTime(date);
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
	    return cal.getTimeInMillis();
	}
	
}
