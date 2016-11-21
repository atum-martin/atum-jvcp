package org.atum.jvcp.atum_jvcp.account;

import java.util.ArrayList;

public class Account {

	private String username, password;
	private ArrayList<Integer> groups = new ArrayList<Integer>(2);
	
	public Account(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public String toString(){
		return username;
	}

	public String getUsername() {
		return username;
	}
	
	public String getPassword(){
		return password;
	}
}
