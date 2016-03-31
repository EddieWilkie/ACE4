import java.io.*;
import java.net.*;
import java.util.*;

public final class WebServer {
	
	public static void main(String[] args) throws IOException{
		//set port number
		final int port = 6789;
		@SuppressWarnings("resource")
		ServerSocket socket = new ServerSocket(port);
		while(true){
			Socket client = socket.accept();
			HttpRequest request = new HttpRequest(client);
			Thread thread = new Thread(request);
			thread.start();
		}
	}

}

final class HttpRequest implements Runnable{
	Socket client;
	public HttpRequest(Socket client){
		this.client = client;
	}

	@Override
	public void run() {
		
	}
}