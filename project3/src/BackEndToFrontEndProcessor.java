
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class BackEndToFrontEndProcessor extends Processor{

	/**
	 * BackEndToFrontEndProcessor variables: clientSocket, database
	 */
	private BackEndDatabase database;
	
	/**
	 * BackEndToFrontEndProcessor constructor
	 * @param clientSocket
	 * @param database
	 */
	public BackEndToFrontEndProcessor(Socket clientSocket, BackEndDatabase database) {
		this.clientSocket = clientSocket;
		this.database = database;
		this.name = "BackEndToFrontEndProcessor";
	}
	
	@Override
	public void handlePost(String uri){
		System.out.println("BackEndToFrontEnd POST");
		if (uri.equals("/profile")){
			
			String password = (String) requestLine.getValueFromParam("password");
			String email = (String) requestLine.getValueFromParam("email");				
			String name = (String) requestLine.getValueFromParam("name");

			String delay = (String) requestLine.getValueFromParam("delay");
			
			if (email != null && password != null){
				// Without 2 phase commmit
//				database.createNewProfile(email, password, name);
				
				// With 2 phase commit
				HashMap<String, String> prepareProfileMap = prepareProfileMap = database.prepareNewProfile(email, password, name);
				System.out.println(prepareProfileMap);
				forwardToSecondaries(prepareProfileMap, delay);
				
				json.put("statusCode", "201");
				json.put("message", "POST Correctly formattted");
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 201 Created\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			} else {
				json.put("message", "POST ERROR MISSING CORRECT KEYS FOR URI " + uri);
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		} else if (uri.equals("/profile/name")){
			String email = (String) requestLine.getValueFromParam("email");
			String password = (String) requestLine.getValueFromParam("password");
			String newName = (String) requestLine.getValueFromParam("name");
			String delay = (String) requestLine.getValueFromParam("delay");

			if (email != null && password != null && name != null){
//				HashMap<String, String> result = database.updateProfileName(email, password, newName);
//				json.putAll(result);
				
				HashMap<String, String> prepareProfileMap = database.prepareUpdateNameProfileMap(email, password, newName);
				forwardToSecondaries(prepareProfileMap, delay);
				
				json.put("statusCode", "201");
				json.put("message", "POST Correctly formattted");
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 201 Created\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			} else {
				json.put("message", "POST ERROR MISSING CORRECT KEYS FOR URI " + uri);
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		} else if (uri.equals("/profile/account")){
			String email = (String) requestLine.getValueFromParam("email");
			String password = (String) requestLine.getValueFromParam("password");
			String delay = (String) requestLine.getValueFromParam("delay");

			if (email != null && password != null){
//				HashMap<String, String> result = database.createNewAccount(email, password);
//				json.putAll(result);

				HashMap<String, String> prepareProfileMap = database.prepareNewAccountProfile(email, password);
				forwardToSecondaries(prepareProfileMap, delay);
				json.put("statusCode", "201");
				json.put("message", "POST Correctly formattted");
				
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 201 Created\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			} else {
				json.put("message", "POST ERROR MISSING CORRECT KEYS FOR URI " + uri);
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		} else if (uri.equals("/profile/account/transaction")){
			String email = (String) requestLine.getValueFromParam("email");
			String password = (String) requestLine.getValueFromParam("password");
			String accountId = (String) requestLine.getValueFromParam("account_id");
			String type = (String) requestLine.getValueFromParam("type");
			Double amount = Double.parseDouble("" + requestLine.getValueFromParam("amount"));
			String delay = (String) requestLine.getValueFromParam("delay");

			if (email != null && password != null && accountId != null && type != null && amount != null){
//				HashMap<String, String> result = database.createNewTransaction(email, password, accountId, type, amount);
//				json.putAll(result);

				HashMap<String, String> prepareProfileMap = database.prepareNewTransaction(email, password, accountId, type, amount);
				forwardToSecondaries(prepareProfileMap, delay);
				json.put("statusCode", "201");
				json.put("message", "POST Correctly formattted");
				
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 201 Created\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			} else {
				json.put("message", "POST ERROR MISSING CORRECT KEYS FOR URI " + uri);
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		}
		else {
			json.put("message", "POST ERROR MISSING URI");
			responseBody = json.toJSONString();
			responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
		}
	}
	
	@Override
	public void handleGet(String uri){
		System.out.println("BackEndToFrontEnd GET");
		if (uri.equals("/profile")){
			String email = (String) requestLine.getValueFromParam("email");
			String password = (String) requestLine.getValueFromParam("password");
			
			if (email != null && password != null){
				HashMap<String, Object> result = database.getProfile(email, password);
				json.putAll(result);
				
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 201 Created\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			} else {
				json.put("statusCode", "400");
				json.put("message", "GET ERROR MISSING CORRECT KEYS FOR URI " + uri);
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		} else if (uri.equals("/profile/version")){
			String email = (String) requestLine.getValueFromParam("email");
			String password = (String) requestLine.getValueFromParam("password");
			
			if (email != null && password != null){
				HashMap<String, Object> result = database.getProfileVersion(email, password);
				json.putAll(result);
				
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 201 Created\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			} else {
				json.put("statusCode", "400");
				json.put("message", "GET ERROR MISSING CORRECT KEYS FOR URI " + uri);
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		} 
		else {
			json.put("message", "ERROR GET MISSING URI /tweets");
			responseBody = json.toJSONString();
			responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
		}
	}
	
	public void forwardToSecondaries(HashMap<String, String> data, String delay){		
		ArrayList<ArrayList<String>> secondaries = this.database.getSecondaries();
		ArrayList<HashMap<String, String>> responseList = new ArrayList<HashMap<String, String>>();
		
		for(ArrayList<String> secondary: secondaries){
			String secondaryIP = secondary.get(0);
			Integer secondaryPORT = Integer.parseInt(secondary.get(1));
			
			HashMap<String, Object> sendingBody = new HashMap<String, Object>();
			sendingBody.put("prepare", "true");
			ArrayList<HashMap<String, String>> test = new ArrayList<HashMap<String, String>>();
			test.add(data);
			sendingBody.put("data", test);
//			sendingBody.putAll(data);
			HashMap<String, String> response = Utilities.connectToServer(sendingBody, "Secondary", secondaryIP, secondaryPORT, "POST", "/forward/prepare");
			responseList.add(response);
		
			if (delay != null){
				System.out.println("JUST FINISHED PROPAGATING POST TO A SEONDARY");
				System.out.println("JUST FINISHED PROPAGATING POST TO A SEONDARY");
				System.out.println("BREAK IT NOW BEFORE THE INFORMATION IN PROPATATED AGAIN");
				System.out.println("BREAK IT NOW BEFORE THE INFORMATION IN PROPATATED AGAIN");
				
				// Sleep for 10 second
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e1) {
					Utilities.debug(this.name + " UnknownHostException\nError Message: " + e1.getLocalizedMessage() + "\n");
				}
			}
		}
		
		boolean forwardCommit = true;
		for(HashMap<String, String> response: responseList){
			if (response.containsKey("error")){
				System.out.println(response.get("error"));
			} else {
				HTTPRequestLine requestLine = new HTTPRequestLine();
				String receivedBody = (String) response.get("body");
		
				boolean correctlyFormatted = requestLine.setBody(receivedBody);
				
				if (correctlyFormatted){
					String commit = (String) requestLine.getValueFromParam("commit");
					if (commit != null){
						if (commit.equals("false")){
							Utilities.debug("Server failed to prepare. Aboring commit ...");
							forwardCommit = false;
						}
					}
				} else {
					Utilities.debug("The body received was not formatted properly.\nBody Received " + receivedBody);
				}
			}	
		}
		
		if (forwardCommit){
			Utilities.debug("Proceeding to update local database");
			database.removeFromPrepareQueue(data.get("prepare_time_stamp"));
			database.processQueue();
			
			if (secondaries.size() > 0){
				Utilities.debug("Proceeding to forward request to secondary servers");
				for(ArrayList<String> secondary: secondaries){
					String secondaryIP = secondary.get(0);
					Integer secondaryPORT = Integer.parseInt(secondary.get(1));
					
					HashMap<String, Object> sendingBody = new HashMap<String, Object>();
					sendingBody.put("commit", "true");
					sendingBody.put("prepare_time_stamp", data.get("prepare_time_stamp"));
					
//					sendingBody.putAll(data);
					HashMap<String, String> response = Utilities.connectToServer(sendingBody, "Secondary", secondaryIP, secondaryPORT, "POST", "/forward/commit");
					if (response.containsKey("error")){
						System.out.println(response.get("error"));
					}
				}
			} 
		}
	}
}
