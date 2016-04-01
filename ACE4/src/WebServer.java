import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public final class WebServer {
	
	public static void main(String[] args) throws IOException{
		//set port number
		final int port = 2108;
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
	Socket socket;
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
		DataOutputStream os =  new DataOutputStream(socket.getOutputStream());
		//Input filters
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
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
			statusLine = "HTTP/1.0 200 OK" + CRLF;
			contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
		}else {
				statusLine = "HTTP/1.0 404 Not Found" + CRLF;
				contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
				entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" + 
							 "<BODY>Not Found</BODY></HTML>";
		}
		// Send the status line
		os.writeBytes(statusLine);
		
		//send the content type line.
		os.writeBytes(contentTypeLine);
		
		//send a blank line to indicate the end of the header lines.
		os.writeBytes(CRLF);
		
		if(fileExists) { 
			sendBytes(fis, os);
			os.writeBytes(statusLine);
			fis.close();
		}else {
			os.writeBytes(statusLine);
			os.writeBytes(entityBody);
			os.writeBytes(contentTypeLine);
		}
		
		System.out.println("*********************");
		System.out.println(fileName);
		System.out.println("*********************");
		
		String headerLine = null;
		while((headerLine = br.readLine()).length() != 0)
			System.out.println(headerLine);
		
		//close streams and socket.
		os.close();
		br.close();
		socket.close();
		
	}
	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception{
		//Construct a 1K buffer to hold bytes on their way to the socket.
		byte[] buffer = new byte[1024];
		int bytes = 0;
		
		//Copy requested file into the socket's output stream.
		while((bytes = fis.read(buffer)) != -1)
			os.write(buffer, 0, bytes);
	}
	
	private static String contentType(String fileName){
		
		if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		if(fileName.endsWith(".gif")){
			return "image/gif";
		}
		if(fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")){
			return "image/jpeg";
		}
		return "application.octet-stream";
	}
			
}