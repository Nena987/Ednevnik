package com.iktpreobuka.ednevnik.entities.dto;

public class UserTokenDTO {

	private String user;
	private String token;

	public UserTokenDTO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UserTokenDTO(String user, String token) {
		super();
		this.user = user;
		this.token = token;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
