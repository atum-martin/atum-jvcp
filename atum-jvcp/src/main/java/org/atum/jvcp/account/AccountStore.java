package org.atum.jvcp.account;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A simple json account store that uses gson to load the accounts.json file.
 * 
 * @author <a href="https://github.com/atum-martin">atum-martin</a>
 * @since 18 Nov 2016 20:58:29
 */

public class AccountStore {

	/**
	 * The instance of the log4j {@link Logger}.
	 */
	private Logger logger = Logger.getLogger(AccountStore.class);

	/**
	 * A map of {link: {@link Account#getUsername()} that maps to the user
	 * {@link Account} which contains the users settings.
	 */
	private HashMap<String, Account> accountStore = new HashMap<String, Account>();

	/**
	 * Only one instance of the account store should be loaded so a singleton is
	 * used.
	 */
	private static AccountStore singleton = null;

	/**
	 * Uses a singleton to obtain an instance of {link: AccountStore}.
	 * 
	 * @return the only instance of the {link: AccountStore}.
	 */
	public static AccountStore getSingleton() {
		if (singleton == null) {
			singleton = new AccountStore();
		}
		return singleton;
	}

	/**
	 * The default constructor. Will load the resource accounts.json and parse
	 * using google gson. Any null entries are cleaned from the output before
	 * being added to the account map {link: {@link AccountStore#accountStore}
	 */
	public AccountStore() {
		Gson gson = new GsonBuilder().create();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("accounts.json");
		Account[] accounts = gson.fromJson(new InputStreamReader(is), Account[].class);
		for (Account account : accounts) {
			if (account != null) {
				logger.info("Creating account: " + account);
				accountStore.put(account.getUsername(), account);
			}
		}
	}

	/**
	 * An {link: Account} object containing the user settings for the specified
	 * user. Returns null in the event no account is found.
	 * 
	 * @param username
	 * @return An {link: Account} object containing the user settings for the
	 *         specified user. Returns null in the event no account is found.
	 */
	public Account getAccount(String username) {
		return accountStore.get(username);
	}

}
