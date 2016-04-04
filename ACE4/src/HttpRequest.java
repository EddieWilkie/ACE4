import java.io.*;
import java.net.*;
import java.util.*;

public final class HttpRequest implements Runnable{
	Socket socket;
	final static String CRLF = "\r\n";
	boolean isProxy;
	final static HashMap<String,HTMLFile> cache = new HashMap<String,HTMLFile>();
	public HttpRequest(Socket socket,boolean isProxy){
		this.socket = socket;
		this.isProxy = isProxy;
	}

	@Override
	public void run() {
		try {
			fillCache();
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
		String statusLine = null;
		String contentTypeLine = null;
		String requestLine = br.readLine();
		
		System.out.println();
		System.out.println(requestLine);
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); //skip over the GET request
		//extract the url Name
		String urlName = tokens.nextToken();
		//extract the fileName
		String fileName = trimFileName(urlName);//make a fileName from the url such as google.com
		
		
		
		//check if file exists in cache...
		if(cache.containsKey(fileName)){
			FileInputStream fis = new FileInputStream("./cache/" + cache.get(fileName).getFileName());
			statusLine = "HTTP/1.0 200 OK" + CRLF;
			contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
			sendBytes(fis, os);
			
			// Send the status line
			os.writeBytes(statusLine);
			//send the content type line.
			os.writeBytes(contentTypeLine);
			//send a blank line to indicate the end of the header lines.
			os.writeBytes(CRLF);
			
			System.out.println("*********************");
			System.out.println(fileName + " Cached");
			System.out.println("*********************");
			
			String headerLine = null;
			while((headerLine = br.readLine()).length() != 0)
				System.out.println(headerLine);
			//close streams and socket.
			fis.close();
			os.close();
			br.close();
			socket.close();
			
		}else {//query the web server
			System.out.println(urlName);
			URL url = new URL(urlName);
			URLConnection conn = url.openConnection();
			conn.setDoInput(true);
			BufferedReader webbr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = null;
			//new HTMLFile
			HTMLFile htmlFile = new HTMLFile(new File(fileName));
			//Save file to cache folder
			FileOutputStream fos = new FileOutputStream("./cache/" + htmlFile.getFile());
			cache.put(fileName, htmlFile);
			while((line = webbr.readLine())!=null){
				fos.write(line.getBytes());
				fos.flush();
			}
			
			statusLine = "HTTP/1.0 200 OK" + CRLF;
			contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
			//get File from cache
			FileInputStream fis = new FileInputStream("./cache/" + cache.get(fileName).getFileName());
			statusLine = "HTTP/1.0 200 OK" + CRLF;
			contentTypeLine = "Content-type: " + contentType(fileName) + CRLF;
			sendBytes(fis, os);
			
			// Send the status line
			os.writeBytes(statusLine);
			//send the content type line.
			os.writeBytes(contentTypeLine);
			//send a blank line to indicate the end of the header lines.
			os.writeBytes(CRLF);
			
			System.out.println("*********************");
			System.out.println(fileName + "NON CACHED");
			System.out.println("*********************");
			
			String headerLine = null;
			while((headerLine = br.readLine()).length() != 0)
				System.out.println(headerLine);
			
			//close streams and socket.
			fos.close();
			fis.close();
			os.close();
			br.close();
			socket.close();
		}
	}
	
	private static void fillCache(){
		
		File folder = new File("./cache/");
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
		    if (file.isFile()) {
		        cache.put(file.getName(), new HTMLFile(file));
		    }
		}
		
	}
		
	private static String trimFileName(String fileName){
		
		if(fileName.endsWith(".com"))
			fileName = fileName.substring(11,fileName.length()-5) + ".html";
		else if(fileName.endsWith(".co.uk"))
			fileName = fileName.substring(11,fileName.length()-7) + ".html";
		else if(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
			fileName = fileName.substring(7,fileName.length()-4).replace('/', '_').replace('.', 'd').replace(':', 'd') + ".jpg";
		else if(fileName.endsWith(".html"))
			fileName = fileName.substring(11,fileName.length()-5).replace('/', '_').replace('.', 'd').replace(':', 'd') + ".min.css";
		else if(fileName.endsWith(".css"))
			fileName = fileName.substring(7,fileName.length()-4).replace('/', '_').replace('.', 'd').replace(':', 'd') + ".min.css";
		else if(fileName.endsWith(".js"))
			fileName = fileName.substring(7,fileName.length()-3).replace('/', '_').replace('.', 'd').replace(':', 'd') + ".app.js";
		else 
			fileName = fileName.substring(7,fileName.length()-3).replace('/', '_').replace('.', 'd').replace(':', 'd') + ".html";
		System.out.println("fileName: " + fileName);
		return fileName;
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