import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

final class HttpRequest implements Runnable{
	Socket socket;
	final static String CRLF = "\r\n";
	boolean isProxy;
	public HttpRequest(Socket socket,boolean isProxy){
		this.socket = socket;
		this.isProxy = isProxy;
	}

	@Override
	public void run() {
		try {
			if(isProxy){
			proxyRequest();
			}else {
			processRequest();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private void proxyRequest() throws Exception {
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
				
				URL url = new URL("http://localhost:6790" + fileName);
				URLConnection conn = url.openConnection();
				conn.setDoInput(true);
				conn.setDoOutput(true);
				//input stream from webserver
				BufferedReader webbr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String inputLine = null;
				while((inputLine = webbr.readLine()) != null)
					os.writeBytes(inputLine);
				
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
		URL url = new URL("http://" + fileName.substring(2));
		URLConnection conn = url.openConnection();
		conn.setDoInput(true);
		BufferedReader webbr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line = null;
		
		if(fileName.endsWith(".com"))
			fileName = fileName.substring(6,fileName.length()-4);
		else 
			fileName = fileName.substring(6,fileName.length()-6);
		
		File file = new File(fileName + ".html");
		FileOutputStream fos = new FileOutputStream(file);
		while((line = webbr.readLine())!=null)
			fos.write(line.getBytes());
		
		
		//Open the requested file.
		FileInputStream fis = null;
		boolean fileExists = true;
		
		try{
			fis = new FileInputStream(fileName + ".html");
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
							 "<BODY>Not Found</BODY>" + "</HTML>";
		}
		// Send the status line
		//os.writeBytes(statusLine);
		
		//send the content type line.
		os.writeBytes(contentTypeLine);
		
		//send a blank line to indicate the end of the header lines.
		os.writeBytes(CRLF);
		
		if(fileExists) { 
			sendBytes(fis, os);
			//os.writeBytes(statusLine);
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
	
	private void processRequest2() throws Exception{
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
							 "<BODY>Not Found</BODY>" + "</HTML>";
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
			
}