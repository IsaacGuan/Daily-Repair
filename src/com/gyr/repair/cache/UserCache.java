package com.gyr.repair.cache;

public class UserCache {
	
	private static int usertype = 0;
	
	private static int guest = 0;
	private static int user = 1;
	private static int engineer = 2;
	
	private static String id = "";
	private static String mobile = "";
	private static String password = "";
	private static String name = "";
	
	private static String expert = "";
	private static String city = "";
	private static String district = "";
	private static String address = "";
	private static String score = "";
	private static String ordernumber = "";	

	public static boolean isGuest(){
		if (UserCache.usertype == guest)
			return true;
		else
			return false;
	}
	
	public static boolean isUser(){
		if (UserCache.usertype == user)
			return true;
		else
			return false;
	}
	
	public static boolean isEngineer(){
		if (UserCache.usertype == engineer)
			return true;
		else
			return false;
	}

	public static void setGuest() {
		UserCache.usertype = guest;
	}

	public static void setUser() {
		UserCache.usertype = user;
	}

	public static void setEngineer() {
		UserCache.usertype = engineer;
	}

	public static void setId(String id) {
		UserCache.id = id;
	}

	public static void setMobile(String mobile) {
		UserCache.mobile = mobile;
	}

	public static void setPassword(String password) {
		UserCache.password = password;
	}

	public static void setName(String name) {
		UserCache.name = name;
	}

	public static void setExpert(String expert) {
		UserCache.expert = expert;
	}

	public static void setCity(String city) {
		UserCache.city = city;
	}

	public static void setDistrict(String district) {
		UserCache.district = district;
	}

	public static void setAddress(String address) {
		UserCache.address = address;
	}

	public static void setScore(String score) {
		UserCache.score = score;
	}
	
	public static void setOrdernumber(String ordernumber) {
		UserCache.ordernumber = ordernumber;
	}
	
	public static String getId() {
		return id;
	}

	public static String getMobile() {
		return mobile;
	}

	public static String getPassword() {
		return password;
	}

	public static String getName() {
		return name;
	}

	public static String getExpert() {
		return expert;
	}

	public static String getCity() {
		return city;
	}

	public static String getDistrict() {
		return district;
	}

	public static String getAddress() {
		return address;
	}
	
	public static String getScore() {
		return score;
	}
	
	public static String getOrdernumber() {
		return ordernumber;
	}
	
}
