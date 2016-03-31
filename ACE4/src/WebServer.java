import java.io.IOException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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
	private Socket socket;
	final static String CRLF = "\r\n";
	public HttpRequest(Socket socket){
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private void processRequest() throws Exception{
		//Input and output streams
		InputStream is = socket.getInputStream();
		OutputStream os = socket.getOutputStream();
		//Input filters
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		
		String requestLine = br.readLine();
		
		System.out.println();
		System.out.println(requestLine);
	}
			
}