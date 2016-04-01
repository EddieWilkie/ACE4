import java.util.*;
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
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); //skip over the GET request
		String fileName = tokens.nextToken();
		
		//Prepend a "." so that the file request is within the current working directory.
		fileName = "." + fileName;
		
		//Open the requested file.
		FileInputStream fis = null;
		boolean fileExists = true;
		try{
			
			fis = new FileInputStream(fileName);
		}catch(FileNotFoundException e){
			fileExists = false;
		}
		
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		//check to see whether the file exists or not.
		if(fileExists){
			statusLine = tokens.nextToken();
			contentTypeLine = "Content-type: " + contentType( fileName) + CRLF;
		}else {
				statusLine = tokens.nextToken();
				contentTypeLine = tokens.nextToken();
				entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" + 
							 "<BODY>404 Not Found</BODY></HTML>";
		}
		
		String headerLine = null;
		while((headerLine = br.readLine()).length() != 0)
			System.out.println(headerLine);
		
		//close streams and socket.
		os.close();
		br.close();
		socket.close();
		
	}
	
	private static String contentType(String fileName){
		return null;
	}
			
}