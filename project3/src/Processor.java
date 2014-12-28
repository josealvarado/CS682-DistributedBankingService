import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public abstract class Processor implements Runnable{

	Socket clientSocket;
	String name = "";
	
	String responseBody = "";
	String responseHeader = "";
	JSONObject json = new JSONObject();
	HTTPRequestLine requestLine;

	private BufferedReader in;
	private OutputStream out;
	
	/**
	 * Run the thread
	 */
	@Override
	public void run() {
		Utilities.debug(name + " started");
		
		/**
		 * Get InputStream from Socket
		 */
		try {
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {			
			Utilities.debug(name + " IOException failed to create BufferedReader\nError Message: " + e.getLocalizedMessage());
			return;
		} 
		
		/**
		 * Get OutputStream from Socket
		 */
		try {
			out = clientSocket.getOutputStream();
		} catch (IOException e) {
			Utilities.debug(name + " IOException failed to create OutputStream\nError Message: " + e.getLocalizedMessage());
			return;
		}

		String request = "";
		String body = "";
		String line = "";
		int length = 0;
		boolean savedFirstLine = false;
		
		/**
		 * Read incoming request line by line
		 */
		try {
			while ((line = in.readLine()) != null) {
				
				Utilities.debug(name + " RECEIVED" + line);
				
				/*
				 * Last line of request header is a blank line
				 * Followed by the request body
				 */
				if (line.equals("") ) {
					Utilities.debug(name + " STOP READING");
					break; 
				}
				
				/*
				 * Check if line has information about Content-Length
				 * This determines if it has a message body or not
				 */
				if (line.startsWith("Content-Length: ")) { 
					int index = line.indexOf(':') + 1;
					String len = line.substring(index).trim();
					length = Integer.parseInt(len);
				}
				
				/**
				 * Saves the METHOD URI VERSION
				 */
				if(!savedFirstLine){
					request = line;
					savedFirstLine = true;
				}				
			}
		} catch (NumberFormatException | IOException e) {
			Utilities.debug(name + " IOException failed to read request.\nError Message: " + e.getLocalizedMessage());
			return;
		} 		
		
		/*
		 * If a message body was found, read it
		 */
		if (length > 0) {
			int read;
			try {
				while ((read = in.read()) != -1) {
					body+= (char) read;
					if (body.length() == length)
					break;
				}
			} catch (IOException e) {
				Utilities.debug(name + " IOException failed to read message body.\nError Message: "+ e.getLocalizedMessage());
				return;
			}
		}
		
		Utilities.debug("Request: " + request + ". Body: " + body);

		requestLine = HTTPRequestLineParser.parse(request);

		if (requestLine != null){
			
			String version = requestLine.getHttpVerstion();
			HTTPConstants.HTTPMethod method = requestLine.getMethod();
			String uri = requestLine.getURIPathWithOutParams();
			Utilities.debug("Method: " + method);
			Utilities.debug("URI: " + uri);
			Utilities.debug("Version: " + version);
			
			/**
			 * Handle incoming POST requests
			 */
			if (method.toString().equals("POST")){
				
				/**
				 * No request body found
				 */
				if (body.length() <= 0){
					json.put("message", "ERROR BackEndServer MISSTING BODY");
					responseBody = json.toJSONString();
					responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
				} else {
					
					/**
					 * Verify request body was formatted properly
					 */
					boolean correctlyFormatted = requestLine.setBody(body);
					
					if (correctlyFormatted){
						
						handlePost(uri);
						
					} else {
						json.put("message", "ERROR IMPROPERLY FORMATTED BODY");
						responseBody = json.toJSONString();
						responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
					}
				}
			} 
			/**
			 * Handle incoming GET requests
			 */
			else if (method.toString().equals("GET")){
				
				boolean correctlyFormatted = true;
				
				if (body.length() > 0){
					correctlyFormatted = requestLine.setBody(body);
				}
				
				handleGet(uri);
				
			} else {
				json.put("message", "ERROR MISSING CORRECT METHOD");
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		} else {
			json.put("message", "ERROR IMPROPERLY FORMATTED REQUEST");
			responseBody = json.toJSONString();
			responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
		}
		
		/**
		 * Respond back to original request
		 */
		try {
			out.write(responseHeader.getBytes());
		} catch (IOException e) {
			System.out.println(name + " IOException failed to write response header.\nError Message: "+ e.getLocalizedMessage());
			StartServer.log.debug(name + "BackEndServerProcessor IOException failed to write response header.\nError Message: "+ e.getLocalizedMessage());
			return;
		}
		try {
			out.write(responseBody.getBytes());
		} catch (IOException e) {
			System.out.println(name + "BackEndServerProcessor IOException failed to write message body.\nError Message: "+ e.getLocalizedMessage());
			StartServer.log.debug(name + "BackEndServerProcessor IOException failed to write message body.\nError Message: "+ e.getLocalizedMessage());
			return;
		}		
		
		System.out.println(name + "BackEndServerProcessor finished");
		StartServer.log.debug(name + "BackEndServerProcessor finished");
	}
	
	public abstract void handlePost(String uri);
	
	public abstract void handleGet(String uri);
}
