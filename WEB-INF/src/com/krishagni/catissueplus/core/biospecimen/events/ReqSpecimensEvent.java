package com.krishagni.catissueplus.core.biospecimen.events;

import java.util.List;

import com.krishagni.catissueplus.core.common.events.RequestEvent;


public class ReqSpecimensEvent extends RequestEvent{

	private String searchString;

	private int startAt;

	private int maxRecords;

	private boolean countReq;
	
	private List<String> specimenLabels;

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public int getStartAt() {
		return startAt;
	}

	public void setStartAt(int startAt) {
		this.startAt = startAt;
	}

	public int getMaxRecords() {
		return maxRecords;
	}

	public void setMaxRecords(int maxRecords) {
		this.maxRecords = maxRecords;
	}

	public boolean isCountReq() {
		return countReq;
	}

	public void setCountReq(boolean countReq) {
		this.countReq = countReq;
	}

	public List<String> getSpecimenLabels() {
		return specimenLabels;
	}

	public void setSpecimenLabels(List<String> specimenLabels) {
		this.specimenLabels = specimenLabels;
	}
	
}
