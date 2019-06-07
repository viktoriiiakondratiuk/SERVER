package lpi.server.rmi;

import java.io.IOException;

public class Main {

     public static void main(String[] args) throws IOException {
		System.out.println("Welcome to RST test TCP Server");

		try (Server server = new Server(args)) {
			server.run();
			System.in.read();
		}

    }
	}

