package com.gyr.repair.bean;

public class Engineer {
	
	private String mobile;
	private String password;
	private String name;
	private String expert;
	private String city;
	private String district;
	private String address;
	
	public Engineer(String mobile, String password, String name, String expert,
			String city, String district, String address) {
		super();
		this.mobile = mobile;
		this.password = password;
		this.name = name;
		this.expert = expert;
		this.city = city;
		this.district = district;
		this.address = address;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExpert() {
		return expert;
	}

	public void setExpert(String expert) {
		this.expert = expert;
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

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
}
