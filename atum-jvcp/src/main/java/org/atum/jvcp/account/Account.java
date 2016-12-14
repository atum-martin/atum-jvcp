package org.atum.jvcp.account;

import java.util.ArrayList;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Nov 2016 22:47:32
 */


public class Account {

	private String username, password;
	@SuppressWarnings("unused")
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
