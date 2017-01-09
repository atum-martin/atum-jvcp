package org.atum.jvcp.account;

import java.util.ArrayList;

/**
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 21 Nov 2016 22:47:32
 */

public class Account {

	/**
	 * Strings representing the username and password of the account. Due to
	 * poor CCcam implementation this must be kept in plaintext.
	 */
	private String username, password;
	/**
	 * A list of integers that represents the groups this user is apart of.
	 */
	@SuppressWarnings("unused")
	private ArrayList<Integer> groups = new ArrayList<Integer>(2);

	/**
	 * Constructs a new Account object with a specified username and password.
	 * This is not persisted.
	 * 
	 * @param username Username of the account.
	 * @param password Password of the account.
	 */
	public Account(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Returns a string with the properties of this user account.
	 */
	public String toString() {
		return username;
	}

	/**
	 * Returns the username associated with this account.
	 * @return Returns the username associated with this account.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Returns the password associated with this account.
	 * @return Returns the password associated with this account.
	 */
	public String getPassword() {
		return password;
	}
	
}
