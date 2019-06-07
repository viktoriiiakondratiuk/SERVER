package lpi.server.rmi;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class Server implements  IServer, Closeable,Runnable {
	private static final long CLEANUP_DELAY_MS = 500;
	private static final long SESSION_TIME_SEC = 70;

	private int port = 4000;

	private IServer proxy;
	private Registry registry;

	private ConcurrentMap<String, Instant> sessionToLastActionMap = new ConcurrentHashMap<>();
	private ConcurrentMap<String, UserInfo> sessionToUserMap = new ConcurrentHashMap<>();
	private Timer sessionTimer = new Timer("Session Cleanup Timer", true);

	public Server(String[] args) {
		if (args.length > 0) {
			try {
				this.port = Integer.parseInt(args[0]);
			} catch (Exception ex) {
			}
		}
	}
	@Override
	public void run() {
		try {
			this.proxy = (IServer) UnicastRemoteObject.exportObject(this, this.port);
			this.registry = LocateRegistry.createRegistry(this.port);
			this.registry.bind("lpi.server.rmi", this.proxy);

			this.sessionTimer.schedule(new SessionCleanupTask(), CLEANUP_DELAY_MS, CLEANUP_DELAY_MS);

			System.out.printf("The RMI server was started on the port %s%n", this.port);

		} catch (AlreadyBoundException | RemoteException e) {
			throw new RuntimeException("Failed to start", e);
		}
	}
	@Override
	public void close() throws IOException {

		if (this.sessionTimer != null) {
			this.sessionTimer.cancel();
			this.sessionTimer = null;
		}

		if (this.registry != null) {
			try {
				this.registry.unbind("lpi.server.rmi");
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.registry = null;
		}

		if (this.proxy != null) {
			UnicastRemoteObject.unexportObject(this, true);
			this.proxy = null;
		}
	}



	@Override
	public <T> T executeTask(Task<T> t) throws RemoteException {
		return t.execute();
	}


    @Override
	public void ping() {
        return; // simplest implementation possible.
	}

	@Override
	public String echo(String text) {
		return String.format("ECHO: %s", text);
	}

	@Override
	public void exit(String sessionId) throws ServerException {
		try {
			if (sessionId == null || sessionId.length() == 0)
				return;
			this.sessionToLastActionMap.remove(sessionId);
			UserInfo user = this.sessionToUserMap.remove(sessionId);

			if (user != null) {
				System.out.printf("%s: User \"%s\" logged out. There are %s active users.%n", new Date(),
						user.getLogin(), this.sessionToUserMap.size());
			}

		} catch (Exception ex) {
			throw new ServerException("Server failed to process your command", ex);
		}
	}

	private UserInfo ensureSessionValid(String sessionId) throws ArgumentException {

		if (sessionId == null || sessionId.length() == 0)
			throw new ArgumentException("sessionId", "The provided session id is not valid");

		UserInfo user = this.sessionToUserMap.get(sessionId);
		if (user == null) {
			throw new ArgumentException("sessionId",
					String.format(
							"The session id is not valid or expired. "
									+ "Ensure you perform any operation with your session at least each %s seconds.",
							SESSION_TIME_SEC));
		}

		// in case the user was removed right in between these actions, let's
		// restore him there.
		if (this.sessionToLastActionMap.put(sessionId, Instant.now()) == null)
			this.sessionToUserMap.putIfAbsent(sessionId, user);

		return user;
	}

	private class SessionCleanupTask extends TimerTask {
		@Override
		public void run() {
			try {
				Instant erasingPoint = Instant.now().minus(SESSION_TIME_SEC, ChronoUnit.SECONDS);

				// removing all session older than erasing point, defined above.
				sessionToLastActionMap.entrySet().stream().filter(entry -> entry.getValue().isBefore(erasingPoint))
						.map(entry -> entry.getKey()).collect(Collectors.toList()).forEach((session -> {
							sessionToLastActionMap.remove(session);
							UserInfo user = sessionToUserMap.remove(session);
							if (user != null) {
								System.out.printf("%s: User's \"%s\" session expired. There are %s active users.%n",
										new Date(), user.getLogin(), sessionToUserMap.size());
							}
						}));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
