package org.atum.jvcp.atum_jvcp.account;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AccountStore {

	private Logger logger = Logger.getLogger(AccountStore.class);
	private HashMap<String,Account> accountStore = new HashMap<String,Account>();
	private static AccountStore singleton = null;
	
	public static AccountStore getSingleton(){
		if( singleton == null){
			singleton = new AccountStore();
		}
		return singleton;
	}

	public AccountStore() {
		Gson gson = new GsonBuilder().create();
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("accounts.json");
		Account[] accounts = gson.fromJson(new InputStreamReader(is), Account[].class);
		for (Account account : accounts) {
			if (account != null){
				logger.info("Creating account: " + account);
				accountStore.put(account.getUsername(),account);
			}
		}
	}
	
	public Account getAccount(String username){
		return accountStore.get(username);
	}

}
