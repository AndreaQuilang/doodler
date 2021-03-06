import java.net.*;
import java.util.*;
import java.io.*;

public class ServerThread extends Thread {
	private ChatServer server = null;
	private Socket socket = null;
	private int id = -1;
	private DataInputStream in = null;
	private DataOutputStream out = null;
	private String un = null;

	public ServerThread(ChatServer server, Socket socket, String un) { //initialize values
		super();
		this.server = server;
		this.socket = socket;
		this.un = un;
		id = socket.getPort();
		System.out.println("ServerThread id: " + id);
	}

	public void send(String msg) { //sender
		try {
			out.writeUTF(msg);
			out.flush();
		}catch(Exception e) {}
	}

	public int getID() {
		return id;
	}

	public void run() {
		System.out.println(id + " running");
		while(true) {
			try {
				ChatServer.handle(un, in.readUTF()); //pass the received message to server
			}catch(Exception e) {}
		}
	}

	public void open() throws IOException { //open for reading and writing
		in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	}

	public void close() throws IOException {
		if (socket != null) socket.close();
		if (in != null) in.close();
		if (out != null) out.close();
	}
}
