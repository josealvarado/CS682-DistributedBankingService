import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class FrontEndServerProcessor extends Processor{

	/**
	 * FrontEndServerProcessor variables: clientSocket, cache, backEndIP, backEndPort
	 */
	private FrontEndServerCache cache;
	
	private String backEndIP;
	private int backEndPort;
	
	private String discoveryIP;
	private int discoveryPort;
	
	int maxAttempts = 5;
	int countAttempts = 0;
	
	/**
	 * FrontEndServerProcessor constructor
	 * @param clientSocket
	 * @param cache
	 * @param IP
	 * @param PORT
	 */
	public FrontEndServerProcessor(Socket clientSocket, FrontEndServerCache cache, String IP, int PORT, String discoveryIP, int discoveryPort) {
		this.clientSocket = clientSocket;
		this.cache = cache;
		this.backEndIP = IP;
		this.backEndPort = PORT;
		this.discoveryIP = discoveryIP;
		this.discoveryPort = discoveryPort;
	}
	
	public void handlePost(String uri){
		System.out.println("FrontEnd POST");
		countAttempts++;

		if (uri.equals("/profile")){
			String password = (String) requestLine.getValueFromParam("password");
			String email = (String) requestLine.getValueFromParam("email");				
			String name = (String) requestLine.getValueFromParam("name");
			String delay = (String) requestLine.getValueFromParam("delay");
			if (password != null && email != null && name != null){
				HashMap<String, Object> sendingBody = new HashMap<String, Object>();
				sendingBody.put("password", password);
				sendingBody.put("email", email);
				sendingBody.put("name", name);
				sendingBody.put("delay", delay);
				
//				HashMap<String, String> response = Utilities.connectToServer(sendingBody, "Primary", backEndIP, backEndPort, "POST", "/profile");
//				json.putAll(response);
				
				HashMap<String, String> response;
				do{
					response = Utilities.connectToServer(sendingBody, "Primary", backEndIP, backEndPort, "POST", "/profile");
					if (response.containsKey("error")){
						System.out.println("Failed to connect to primary. Contacting discovery server");
						countAttempts++;
						HashMap<String, String> discoveryResponse = Utilities.connectToServer(sendingBody, "Dicovery", this.discoveryIP, this.discoveryPort, "GET", "/discovery");
						System.out.println("Discovery response " + discoveryResponse);
						
						if (discoveryResponse.containsKey("error")){
							System.out.println(discoveryResponse.get("error"));
						} else {
							System.out.println("Updaing primary ip and port");

							HTTPRequestLine requestLine = new HTTPRequestLine();
							String receivedBody = (String) discoveryResponse.get("body");

							boolean correctlyFormatted = requestLine.setBody(receivedBody);
							
							if (correctlyFormatted){
								backEndIP = (String) requestLine.getValueFromParam("ip");
								backEndPort = Integer.parseInt("" + requestLine.getValueFromParam("port"));
							} else {
								Utilities.debug("The body received was not formatted properly.\nBody Received " + receivedBody);
							}
						}

					} else {
						System.out.println("Successfully received from primary. " + response);
					}
				}
				while(countAttempts< maxAttempts && response.containsKey("error"));
				json.putAll(response);
				
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 201 Good Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			} else {
				json.put("message", "ERROR POST MISSING CORRECT KEYS");
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		} else if (uri.equals("/profile/name")){
			String password = (String) requestLine.getValueFromParam("password");
			String email = (String) requestLine.getValueFromParam("email");				
			String name = (String) requestLine.getValueFromParam("name");
			String delay = (String) requestLine.getValueFromParam("delay");
			if (password != null && email != null && name != null){
				HashMap<String, Object> sendingBody = new HashMap<String, Object>();
				sendingBody.put("password", password);
				sendingBody.put("email", email);
				sendingBody.put("name", name);
				sendingBody.put("delay", delay);
				
//				HashMap<String, String> response = Utilities.connectToServer(sendingBody, "Primary", backEndIP, backEndPort, "POST", "/profile/name");
//				json.putAll(response);
				
				HashMap<String, String> response;
				do{
					response = Utilities.connectToServer(sendingBody, "Primary", backEndIP, backEndPort, "POST", "/profile/name");
					if (response.containsKey("error")){
						System.out.println("Failed to connect to primary. Contacting discovery server");
						countAttempts++;
						HashMap<String, String> discoveryResponse = Utilities.connectToServer(sendingBody, "Dicovery", this.discoveryIP, this.discoveryPort, "GET", "/discovery");
						System.out.println("Discovery response " + discoveryResponse);
						
						if (discoveryResponse.containsKey("error")){
							System.out.println(discoveryResponse.get("error"));
						} else {
							System.out.println("Updaing primary ip and port");

							HTTPRequestLine requestLine = new HTTPRequestLine();
							String receivedBody = (String) discoveryResponse.get("body");

							boolean correctlyFormatted = requestLine.setBody(receivedBody);
							
							if (correctlyFormatted){
								backEndIP = (String) requestLine.getValueFromParam("ip");
								backEndPort = Integer.parseInt("" + requestLine.getValueFromParam("port"));
							} else {
								Utilities.debug("The body received was not formatted properly.\nBody Received " + receivedBody);
							}
						}

					} else {
						System.out.println("Successfully received from primary. " + response);
					}
				}
				while(countAttempts< maxAttempts && response.containsKey("error"));
				json.putAll(response);
				
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 201 Good Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			} else {
				json.put("message", "ERROR POST MISSING CORRECT KEYS");
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		}  else if (uri.equals("/profile/account")){
			String password = (String) requestLine.getValueFromParam("password");
			String email = (String) requestLine.getValueFromParam("email");				
			String delay = (String) requestLine.getValueFromParam("delay");				
			if (password != null && email != null && name != null){
				HashMap<String, Object> sendingBody = new HashMap<String, Object>();
				sendingBody.put("password", password);
				sendingBody.put("email", email);
				sendingBody.put("delay", delay);
				
//				HashMap<String, String> response = Utilities.connectToServer(sendingBody, "Primary", backEndIP, backEndPort, "POST", "/profile/account");
//				json.putAll(response);
				
				HashMap<String, String> response;
				do{
					response = Utilities.connectToServer(sendingBody, "Primary", backEndIP, backEndPort, "POST", "/profile/account");
					if (response.containsKey("error")){
						System.out.println("Failed to connect to primary. Contacting discovery server");
						countAttempts++;
						HashMap<String, String> discoveryResponse = Utilities.connectToServer(sendingBody, "Dicovery", this.discoveryIP, this.discoveryPort, "GET", "/discovery");
						System.out.println("Discovery response " + discoveryResponse);
						
						if (discoveryResponse.containsKey("error")){
							System.out.println(discoveryResponse.get("error"));
						} else {
							System.out.println("Updaing primary ip and port");

							HTTPRequestLine requestLine = new HTTPRequestLine();
							String receivedBody = (String) discoveryResponse.get("body");

							boolean correctlyFormatted = requestLine.setBody(receivedBody);
							
							if (correctlyFormatted){
								backEndIP = (String) requestLine.getValueFromParam("ip");
								backEndPort = Integer.parseInt("" + requestLine.getValueFromParam("port"));
							} else {
								Utilities.debug("The body received was not formatted properly.\nBody Received " + receivedBody);
							}
						}

					} else {
						System.out.println("Successfully received from primary. " + response);
					}
				}
				while(countAttempts< maxAttempts && response.containsKey("error"));
				json.putAll(response);
				
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 201 Good Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			} else {
				json.put("message", "ERROR POST MISSING CORRECT KEYS");
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		} else if (uri.equals("/profile/account/transaction")){
			String email = (String) requestLine.getValueFromParam("email");
			String password = (String) requestLine.getValueFromParam("password");
			String accountId = (String) requestLine.getValueFromParam("account_id");
			String type = (String) requestLine.getValueFromParam("type");
			String delay = (String) requestLine.getValueFromParam("delay");
			Double amount = Double.parseDouble((String) requestLine.getValueFromParam("amount"));
			
			if (password != null && email != null && name != null){
				HashMap<String, Object> sendingBody = new HashMap<String, Object>();
				sendingBody.put("email", email);
				sendingBody.put("password", password);
				sendingBody.put("account_id", accountId);
				sendingBody.put("type", type);
				sendingBody.put("amount", amount);
				sendingBody.put("delay", delay);
				
//				HashMap<String, String> response = Utilities.connectToServer(sendingBody, "Primary", backEndIP, backEndPort, "POST", "/profile/account/transaction");
//				json.putAll(response);
				
				HashMap<String, String> response;
				do{
					response = Utilities.connectToServer(sendingBody, "Primary", backEndIP, backEndPort, "POST", "/profile/account/transaction");
					if (response.containsKey("error")){
						System.out.println("Failed to connect to primary. Contacting discovery server");
						countAttempts++;
						HashMap<String, String> discoveryResponse = Utilities.connectToServer(sendingBody, "Dicovery", this.discoveryIP, this.discoveryPort, "GET", "/discovery");
						System.out.println("Discovery response " + discoveryResponse);
						
						if (discoveryResponse.containsKey("error")){
							System.out.println(discoveryResponse.get("error"));
						} else {
							System.out.println("Updaing primary ip and port");

							HTTPRequestLine requestLine = new HTTPRequestLine();
							String receivedBody = (String) discoveryResponse.get("body");

							boolean correctlyFormatted = requestLine.setBody(receivedBody);
							
							if (correctlyFormatted){
								backEndIP = (String) requestLine.getValueFromParam("ip");
								backEndPort = Integer.parseInt("" + requestLine.getValueFromParam("port"));
							} else {
								Utilities.debug("The body received was not formatted properly.\nBody Received " + receivedBody);
							}
						}

					} else {
						System.out.println("Successfully received from primary. " + response);
					}
				}
				while(countAttempts< maxAttempts && response.containsKey("error"));
				json.putAll(response);
				
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 201 Good Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			} else {
				json.put("message", "POST ERROR MISSING CORRECT KEYS");
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		} 
		
		else {
			json.put("statusCode", "404");
			json.put("message", "ERROR POST MISSING URI /tweets");
			responseBody = json.toJSONString();
			responseHeader = "HTTP/1.1 404 Not Found\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
		}
	}
	
	public void handleGet(String uri){
		countAttempts = 1;
		if (uri.equals("/profile")){
			String email = (String) requestLine.getValueFromParam("email");
			String password = (String) requestLine.getValueFromParam("password");
			
			if (email != null && password != null){		
				
				boolean needLatestVersion = true;
				String error = null;
				
				// Verify if I have the latest version. If so return that. 
				HashMap<String, Object> sendingBody2 = new HashMap<String, Object>();
				sendingBody2.put("password", password);
				sendingBody2.put("email", email);
				
				HashMap<String, String> response2;
				do{
					Utilities.debug("Attempt #" + countAttempts);
					response2 = Utilities.connectToServer(sendingBody2, "Primary", backEndIP, backEndPort, "GET", "/profile/version");
					if (response2.containsKey("error")){
						System.out.println("Failed to connect to primary. Error: " + response2.get("error") + " Contacting discovery server");
						countAttempts++;
						HashMap<String, String> discoveryResponse = Utilities.connectToServer(sendingBody2, "Dicovery", this.discoveryIP, this.discoveryPort, "GET", "/discovery");
						System.out.println("Discovery response " + discoveryResponse);
						
						if (discoveryResponse.containsKey("error")){
							System.out.println(discoveryResponse.get("error"));
						} else {
							error = null;
							System.out.println("Updaing primary ip and port");

							HTTPRequestLine requestLine = new HTTPRequestLine();
							String receivedBody = (String) discoveryResponse.get("body");

							boolean correctlyFormatted = requestLine.setBody(receivedBody);
							
							if (correctlyFormatted){
								backEndIP = (String) requestLine.getValueFromParam("ip");
								backEndPort = Integer.parseInt("" + requestLine.getValueFromParam("port"));
							} else {
								Utilities.debug("The body received was not formatted properly.\nBody Received " + receivedBody);
							}
						}

					} else {
						System.out.println("Successfully received from primary. " + response2);
						
						HTTPRequestLine requestLine = new HTTPRequestLine();
						String receivedBody = (String) response2.get("body");

						boolean correctlyFormatted = requestLine.setBody(receivedBody);
						
						if (correctlyFormatted){
							error =  (String) requestLine.getValueFromParam("error");
							if (error != null){
								Utilities.debug(name + "ERROR " + error);
							} else {
								error = null;
								int dataVersion = Integer.parseInt("" + requestLine.getValueFromParam("version"));
								int previousVersion = cache.getVersion(email);
								
								if (dataVersion == previousVersion){
									needLatestVersion = false;
								}
							}
						} else {
							Utilities.debug("The body received was not formatted properly.\nBody Received " + receivedBody);
						}
					}
				}
				while(countAttempts< maxAttempts && response2.containsKey("error"));
				
				if (countAttempts == maxAttempts){
					json.put("error", "Max number of attempts allowed was reached");
				}
				else if (error != null){
					json.put("error", error);
				} else {
					if (needLatestVersion){
						// I didn't have the latest version. Get new version
						HashMap<String, Object> sendingBody = new HashMap<String, Object>();
						sendingBody.put("password", password);
						sendingBody.put("email", email);
						
						HashMap<String, String> response;
						do{
							response = Utilities.connectToServer(sendingBody, "Primary", backEndIP, backEndPort, "GET", "/profile");
							if (response.containsKey("error")){
								System.out.println("Failed to connect to primary. Error: " + response.get("error") + " Contacting discovery server");
								countAttempts++;
								HashMap<String, String> discoveryResponse = Utilities.connectToServer(sendingBody, "Dicovery", this.discoveryIP, this.discoveryPort, "GET", "/discovery");
								System.out.println("Discovery response " + discoveryResponse);
								
								if (discoveryResponse.containsKey("error")){
									System.out.println(discoveryResponse.get("error"));
								} else {
									System.out.println("Updaing primary ip and port");

									HTTPRequestLine requestLine = new HTTPRequestLine();
									String receivedBody = (String) discoveryResponse.get("body");

									boolean correctlyFormatted = requestLine.setBody(receivedBody);
									
									if (correctlyFormatted){
										backEndIP = (String) requestLine.getValueFromParam("ip");
										backEndPort = Integer.parseInt("" + requestLine.getValueFromParam("port"));
									} else {
										Utilities.debug("The body received was not formatted properly.\nBody Received " + receivedBody);
									}
								}

							} else {
								System.out.println("Successfully received from primary. " + response);
								
								HTTPRequestLine requestLine = new HTTPRequestLine();
								String receivedBody = (String) response.get("body");

								boolean correctlyFormatted = requestLine.setBody(receivedBody);
								
								if (correctlyFormatted){
									int dataVersion = Integer.parseInt("" + requestLine.getValueFromParam("version"));
									this.cache.saveVersion(email, dataVersion, response);

								} else {
									Utilities.debug("The body received was not formatted properly.\nBody Received " + receivedBody);
								}
							}
						}
						while(countAttempts< maxAttempts && response.containsKey("error"));
						
						json.putAll(response);
						
					} else {
						System.out.println("This was returned from my cache!!!!");
						HashMap<String, String> data = cache.getData(email);
						json.putAll(data);
					}
				}
				
				
		
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 201 Good Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
				
			} else {	
				json.put("message", "ERROR GET MISSING CORRECT KEYS");
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		} else {
			json.put("message", "ERROR GET MISSING URI /tweets ");
			responseBody = json.toJSONString();
			responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
		}
	}
		
}
