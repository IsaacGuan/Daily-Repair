package com.gyr.repair.bean;

public class Order {
	
	private String title;
	private String mobileuser;
	private String mobileengineer;
	private String budget;
	private String date;
	private String city;
	private String district;
	private String adress;
	private String detail;
	
	public Order(String title, String mobileuser, String mobileengineer,
			String budget, String date, String city, String district,
			String adress, String detail) {
		super();
		this.title = title;
		this.mobileuser = mobileuser;
		this.mobileengineer = mobileengineer;
		this.budget = budget;
		this.date = date;
		this.city = city;
		this.district = district;
		this.adress = adress;
		this.detail = detail;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMobileuser() {
		return mobileuser;
	}

	public void setMobileuser(String mobileuser) {
		this.mobileuser = mobileuser;
	}

	public String getMobileengineer() {
		return mobileengineer;
	}

	public void setMobileengineer(String mobileengineer) {
		this.mobileengineer = mobileengineer;
	}

	public String getBudget() {
		return budget;
	}

	public void setBudget(String budget) {
		this.budget = budget;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getAdress() {
		return adress;
	}

	public void setAdress(String adress) {
		this.adress = adress;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

}
