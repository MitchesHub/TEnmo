package com.techelevator.tenmo.model;

public class AuthenticatedUser {
	private String token;
	private User user;

	@Override
	public String toString() {
		return getToken();
	}

	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
}
