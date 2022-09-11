package com.cargo.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class Case implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5933068066540755430L;

	@Id
	private String id;
	
	
	private String patient;
	
	@DBRef
	private User doctor;
	
	@DBRef
	private User vender;
	
	@Indexed
	private String opdNo;
	
	private Date bookingDate;
	
	private Date appointmentDate;
	
	private Crown crown;
	
	private CaseStatus status;
	
	private CaseSubStatus subStatus = CaseSubStatus.NONE;
	
	private Date deliveredDate;
	
	private Date updateDate;
	
    private String updateBy;
    
    @Field("createdBy")
    private String createdBy;
    
    private int version;
    
    private String remark;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPatient() {
		return patient;
	}

	public void setPatient(String patient) {
		this.patient = patient;
	}

	public User getDoctor() {
		return doctor;
	}

	public void setDoctor(User doctor) {
		this.doctor = doctor;
	}

	public User getVender() {
		return vender;
	}

	public void setVender(User vender) {
		this.vender = vender;
	}

	public String getOpdNo() {
		return opdNo;
	}

	public void setOpdNo(String opdNo) {
		this.opdNo = opdNo;
	}

	public Date getBookingDate() {
		return bookingDate;
	}

	public void setBookingDate(Date bookingDate) {
		this.bookingDate = bookingDate;
	}

	public Date getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(Date appointmentDate) {
		this.appointmentDate = appointmentDate;
	}

	public Crown getCrown() {
		return crown;
	}

	public void setCrown(Crown crown) {
		this.crown = crown;
	}

	public CaseStatus getStatus() {
		return status;
	}

	public void setStatus(CaseStatus status) {
		this.status = status;
	}

	public Date getDeliveredDate() {
		return deliveredDate;
	}

	public void setDeliveredDate(Date deliveredDate) {
		this.deliveredDate = deliveredDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateBy() {
		return updateBy;
	}

	public void setUpdateBy(String updateBy) {
		this.updateBy = updateBy;
	}
	
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public CaseSubStatus getSubStatus() {
		return subStatus;
	}

	public void setSubStatus(CaseSubStatus subStatus) {
		this.subStatus = subStatus;
	}

	public List<CaseStatus> getnextActions(){
		List<CaseStatus> actions = new ArrayList<CaseStatus>();
		switch (this.status) {
		case BOOKED:
			actions.add(CaseStatus.INPROCESS);
			break;
		case INPROCESS:
			actions.add(CaseStatus.DELIVERD);
			actions.add(CaseStatus.TRIAL);
			break;
		case DELIVERD:
			actions.add(CaseStatus.INSERTION_DONE);
			actions.add(CaseStatus.INPROCESS);
			break;
		case TRIAL:
			actions.add(CaseStatus.INPROCESS);
			break;	
		case INSERTION_DONE:
			actions.add(CaseStatus.INPROCESS);
			break;		
		}
	  return actions;
	}
}
