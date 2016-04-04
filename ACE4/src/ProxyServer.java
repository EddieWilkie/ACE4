

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public final class ProxyServer {
	
	public static void main(String[] args) throws IOException{
		//set port number
		final int port = 8080;
		ServerSocket socket = new ServerSocket(port);
		while(true){
			Socket client = socket.accept();
			HttpRequest request = new HttpRequest(client,false);
			Thread thread = new Thread(request);
			thread.start();
		}
	}
}
