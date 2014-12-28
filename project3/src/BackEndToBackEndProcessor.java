
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class BackEndToBackEndProcessor extends Processor {

	/**
	 * BackEndServerProcessor variables: clientSocket, database
	 */
	private BackEndDatabase database;
	
	/**
	 * BackEndServerProcessor constructor
	 * @param clientSocket
	 * @param database
	 */
	public BackEndToBackEndProcessor(Socket clientSocket, BackEndDatabase database) {
		this.clientSocket = clientSocket;
		this.database = database;
		this.name = "BackEndToBackEndProcessor";
	}
	
	/**
	 * Servers responding to POST requests
	 */
	public void handlePost(String uri){
		System.out.println("BackEndToBackEnd POST");
		
		/**
		 * Secondary severs responding to prepare message from Primary server
		 */
		if (uri.equals("/forward/prepare")){							
			String prepare = (String) requestLine.getValueFromParam("prepare");
			
			if (prepare != null ){
				ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) requestLine.getValueFromParam("data");
				HashMap<String, String> prepareMap = list.get(0);
				database.putPrepareMapInPrepareQueue(prepareMap);
				
				json.put("commit", "true");
				json.put("status_code", "200");
												
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 200 Created\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			} else {
				json.put("statusCode", "400");
				json.put("message", "GET ERROR MISSING CORRECT KEYS");
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		} else if (uri.equals("/forward/commit")){
			String commit = (String) requestLine.getValueFromParam("commit");
			String prepareTimeStamp = (String) requestLine.getValueFromParam("prepare_time_stamp");
			
			if (commit != null && prepareTimeStamp != null){
				
				database.removeFromPrepareQueue(prepareTimeStamp);
				database.processQueue();
				
				json.put("status_code", "200");
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 200 Created\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			} else {
				json.put("statusCode", "400");
				json.put("message", "GET ERROR MISSING CORRECT KEYS");
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		} else {
			json.put("message", "POST ERROR MISSING URI");
			responseBody = json.toJSONString();
			responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
		}
	}
	
	/**
	 * Servers responding to GET requests
	 */
	public void handleGet(String uri){
		System.out.println("BackEndToBackEnd GET");
		//From Secondary to Primary
		/**
		 * Either Secondary Server or Primary Server requesting a status update
		 */
		if (uri.equals("/status")){
			Integer receivedEmailPasswordVersion =  Integer.parseInt("" +  requestLine.getValueFromParam("email_password_version"));								
			Integer receivedProfileVersion =  Integer.parseInt("" +  requestLine.getValueFromParam("profile_version"));								
			String receivedIp =  (String) requestLine.getValueFromParam("ip");								
			String receivedPort = (String) requestLine.getValueFromParam("port");
			
			if (receivedEmailPasswordVersion != null && receivedProfileVersion != null && receivedIp != null && receivedPort != null){
				
				database.addSecondary(receivedIp, receivedPort);
				
				HashMap<String, Object> result = database.getStatus(receivedEmailPasswordVersion, receivedProfileVersion);
				json.putAll(result);
				
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 201 Created\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			} else {
				json.put("statusCode", "400");
				json.put("message", "GET ERROR MISSING CORRECT KEYS");
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		} 
		/**
		 * Secondary Servers requesting an election of a new Primary Server
		 */
		else if (uri.equals("/election")){
			json.put("ip", this.database.getBackEndIP());
			json.put("port", "" + this.database.getDiscoveryReceivePort());
			
			json.put("email_password_version", this.database.getEmailPasswordDatabaseVersion());
			json.put("profile_version", this.database.getProfileDatabaseVersion());

			ArrayList<HashMap<String, String>> prep = database.getPrepareQueue();
			json.put("prep_size", database.getPrepareQueueSize());
			json.put("prep", prep);
			
			json.put("statusCode", "200");
			
			json.put("message", "ELECTION TIME!!");
			responseBody = json.toJSONString();
			responseHeader = "HTTP/1.1 200 Good Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
		}
		else {
			json.put("message", "GET ERROR MISSING URI");
			responseBody = json.toJSONString();
			responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
		}
	}
}

