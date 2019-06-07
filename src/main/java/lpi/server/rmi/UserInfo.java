package lpi.server.rmi;

import java.util.ArrayList;
import java.util.List;


public class UserInfo {
	private static final int MAX_PENDING_MESSAGES = 100;

	private final Object syncRoot = new Object();
	private final String login;
	private final String password;

	public UserInfo(String login, String password) {
		this.login = login;
		this.password = password;
	}

	public String getLogin() {
		return this.login;
	}

	public boolean canLogin(String login, String password) {
		return this.login.equals(login) && this.password.equals(password);
	}


}
