
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONValue;


public class BackEndServerDiscoverySend  implements Runnable{
	private String primaryIP;
	private int primaryPort;
	private String status;
	
	private String localIp;
	private int localPort;
	
	private int discoveryReceivePort;

	private BackEndDatabase database;
	private String name;
		
	public BackEndServerDiscoverySend(BackEndDatabase database){
		name = "BackEndServerDiscoverySend";
		this.primaryIP = database.getPrimaryIP();
		this.primaryPort = database.getPrimaryPort();
		this.discoveryReceivePort = database.getDiscoveryReceivePort();
		this.localIp = database.getBackEndIP();
		this.localPort = database.getBackEndPort();
		
		if (this.localIp.equals(this.primaryIP) && discoveryReceivePort == this.primaryPort){
			this.status = "PRIMARY";
		} else {
			this.status = "SECONDARY";
		}

		this.database = database;			
	}
	
	@Override
	public void run(){				
		int allowedFailedAttempts = 2;
		int failedAttempts = 0;
	
		while(true){
			System.out.println("Server Status = " + status);
			
			// Sleep for 5 second
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				Utilities.debug("\n" + name + " UnknownHostException\nError Message: " + e1.getLocalizedMessage() + "\n");
			}
			
			if (status.equals("SECONDARY")){
				HashMap<String, Object> sendingBody = new HashMap<String, Object>();
				sendingBody.put("ip", this.localIp);
				sendingBody.put("port", ""+this.discoveryReceivePort);
				sendingBody.put("email_password_version", this.database.getEmailPasswordDatabaseVersion());
				sendingBody.put("profile_version", this.database.getProfileDatabaseVersion());
				HashMap<String, String> response = this.connectToServer(sendingBody, "Primary", primaryIP, primaryPort, "GET", "/status");
				
				if (response.containsKey("error")){
					System.out.println(response.get("error"));
					failedAttempts++;
				} else {
					HTTPRequestLine requestLine = new HTTPRequestLine();
					String receivedBody = (String) response.get("body");

					boolean correctlyFormatted = requestLine.setBody(receivedBody);
					
					if (correctlyFormatted){
						ArrayList<ArrayList<String>> secondaryList = (ArrayList<ArrayList<String>>)requestLine.getValueFromParam("secondaries");
						
						if (secondaryList != null){
							for (ArrayList<String> second: secondaryList){
								System.out.println("IP: " + second.get(0) + ", PORT: " + second.get(1));
								database.addSecondary(second.get(0), second.get(1));
							}
						}
					
						boolean modified = false;
						ArrayList<HashMap<String, String>> email_password_log = (ArrayList<HashMap<String, String>>) requestLine.getValueFromParam("email_password_log");
						if (email_password_log != null){
							modified = true;
							for(HashMap<String, String> log: email_password_log){
								log.put("prepare_time_stamp", log.get("time_stamp"));
								this.database.addToQeueu(log);
							}
							this.database.processQueue();
						} 
						
						ArrayList<HashMap<String, String>> profile_transaction_log = (ArrayList<HashMap<String, String>>) requestLine.getValueFromParam("profile_transaction_log");
						if (profile_transaction_log != null){
							modified = true;
							for(HashMap<String, String> log: profile_transaction_log){
								log.put("prepare_time_stamp", log.get("time_stamp"));
								this.database.addToQeueu(log);
							}
							this.database.processQueue();
						} 						
						
						if (modified){
							System.out.println("SERVER WAS NOT UP TO DATE, DATA RECEIVED");
						} else {
							System.out.println("SERVER IS UP TO DATE, NO DATA RECEIVED");
						}
					} else {
						Utilities.debug("The body received was not formatted properly.\nBody Received " + receivedBody);
					}
				}
				
				if (failedAttempts == allowedFailedAttempts){
					status = "DISCOVERY";
					failedAttempts = 0;
				}
			} 
			else if (status.equals("DISCOVERY")){      
				Utilities.debug("PRIMARY SERVER FAILED! Attempting to establish a new primary");
				forwardToSecondaries();
			} 
			else {
				Utilities.debug("Updating discovery server with ip and port");
				contactDiscoveryServer();
			}
		}
	}
	
	public void forwardToSecondaries(){		
		ArrayList<ArrayList<String>> secondaries = this.database.getSecondaries();
		Utilities.debug("Known Secondaries: " + secondaries.size());
		ArrayList<HashMap<String, Object>> secondaryResponses = new ArrayList<HashMap<String, Object>>();
		
		if (secondaries.size() == 0){
			status = "PRIMARY";
			Utilities.debug("No Secondaries. I am the new primary");
		} else {			
			for(ArrayList<String> secondary: secondaries){
				String secondaryIP = secondary.get(0);
				Integer secondaryPORT = Integer.parseInt(secondary.get(1));
				
				HashMap<String, Object> sendingBody = new HashMap<String, Object>();				
				HashMap<String, String> response = this.connectToServer(sendingBody, "Secondary", secondaryIP, secondaryPORT, "GET", "/election");
				
				if (response.containsKey("error")){
					System.out.println(response.get("error"));
				} else {
					HTTPRequestLine requestLine = new HTTPRequestLine();
					String receivedBody = (String) response.get("body");

					boolean correctlyFormatted = requestLine.setBody(receivedBody);
					
					if (correctlyFormatted){
						String receivedIp =  (String) requestLine.getValueFromParam("ip");
						String receivedPort =  "" + requestLine.getValueFromParam("port");
						String receivedEmailPasswordVersion =  "" + requestLine.getValueFromParam("email_password_version");
						String receivedProfileVersion =  "" + requestLine.getValueFromParam("profile_version");
						String prepSize =  "" + requestLine.getValueFromParam("prep_size");
						ArrayList<HashMap<String, String>> prep = (ArrayList<HashMap<String, String>>) requestLine.getValueFromParam("prep");

						HashMap<String, Object> receivedMap = new HashMap<String, Object>();
						receivedMap.put("ip", receivedIp);
						receivedMap.put("port", receivedPort);
						receivedMap.put("emailPasswordVersion", receivedEmailPasswordVersion);
						receivedMap.put("profileVersion", receivedProfileVersion);
						receivedMap.put("prepSize", prepSize);
						receivedMap.put("prep", prep);
						
						secondaryResponses.add(receivedMap);
					} else {
						Utilities.debug("The body received was not formatted properly.\nBody Received " + receivedBody);
					}
				}
			}
			
			Utilities.debug("MY PREP QUEUE SIZE IS " + database.getPrepareQueueSize());
			
			
			int greatestReceivedPort = this.database.getDiscoveryReceivePort();
			String greatestReceivedIp = this.database.getBackEndIP();
			boolean iAmPrimary = true;

			int greatestTimeStamp = this.database.getEmailPasswordDatabaseVersion() + this.database.getProfileDatabaseVersion();
			String ipWithLatestData = this.database.getBackEndIP();
			int portWithLatestData = this.database.getDiscoveryReceivePort();
			boolean iHaveTheLatestData = true;
			
			for(HashMap<String, Object> receivedMap: secondaryResponses){
				Utilities.debug("Response :" + receivedMap ) ;
				String prepSize = "" +  receivedMap.get("prepSize");
				Utilities.debug("OTHER PREP SIZES ARE :" + prepSize ) ;
				
				ArrayList<HashMap<String, String>> prepQueue = (ArrayList<HashMap<String, String>>) receivedMap.get("prep");
				
				for(HashMap<String, String> pre: prepQueue){
					
					boolean inMyPrep = database.existInPrepQueue(pre);
					
										// what about in mine, but not in yours?
					
					if (inMyPrep){		//in mine and yours
						Utilities.debug("PrepareMap found in prep queue. Process it. ");
						database.removeFromPrepareQueue(pre.get("prepare_time_stamp"));
					} else {			//not in mine, but in yours
						Utilities.debug("New PrepareMap found in external prep queue. Process it. ");
						
						database.putPrepareMapInPrepareQueueW(pre);
						HashMap<String, String> weird = database.removeFromPrepareQueue(pre.get("prepare_time_stamp"));
						database.processQueue();
						System.out.print(weird);
					}
					
					if (secondaries.size() > 0){
						Utilities.debug("Proceeding to forward request to secondary servers");
						for(ArrayList<String> secondary: secondaries){
							String secondaryIP = secondary.get(0);
							Integer secondaryPORT = Integer.parseInt(secondary.get(1));
							
							HashMap<String, Object> sendingBody = new HashMap<String, Object>();
							sendingBody.put("commit", "true");
							sendingBody.put("prepare_time_stamp", pre.get("prepare_time_stamp"));
							
//							sendingBody.putAll(data);
							HashMap<String, String> response = Utilities.connectToServer(sendingBody, "Secondary", secondaryIP, secondaryPORT, "POST", "/forward/commit");
							if (response.containsKey("error")){
								System.out.println(response.get("error"));
							}
						}
					} 
					
				}
				
				
				// Selects next primary
				String receivedIp = (String) receivedMap.get("ip");
				int receivedPort = Integer.parseInt((String) receivedMap.get("port"));
				String receivedEmailPasswordVersion = (String) receivedMap.get("emailPasswordVersion");
				String receivedProfileVersion = (String) receivedMap.get("profileVersion");
				int timeStamp = Integer.parseInt(receivedEmailPasswordVersion) + Integer.parseInt(receivedProfileVersion);
				
				if (receivedPort > greatestReceivedPort){
					iAmPrimary = false;
					greatestReceivedPort = receivedPort;
					greatestReceivedIp = receivedIp;
				}
				
				if (timeStamp > greatestTimeStamp){
					iHaveTheLatestData = false;
					ipWithLatestData = receivedIp;
					portWithLatestData = receivedPort;
				}
			}
			
			
			if (iAmPrimary){
				status = "PRIMARY";
				System.out.println("I am the new primary");
				if (iHaveTheLatestData){
					System.out.println("I have the latest database");
				} else {
					System.out.println("I need to update my database. Call status on IP " + ipWithLatestData + " port " + portWithLatestData);
					
					
//					HashMap<String, Object> sendingBody = new HashMap<String, Object>();
//					sendingBody.put("ip", this.localIp);
//					sendingBody.put("port", ""+this.discoveryReceivePort);
//					sendingBody.put("email_password_version", this.database.getEmailPasswordDatabaseVersion());
//					sendingBody.put("profile_version", this.database.getProfileDatabaseVersion());
//					HashMap<String, String> response = this.connectToServer(sendingBody, "Latest Server", ipWithLatestData, portWithLatestData, "GET", "/status");
//					
//					if (response.containsKey("error")){
//						System.out.println(response.get("error"));
//					} else {
//						HTTPRequestLine requestLine = new HTTPRequestLine();
//						String receivedBody = (String) response.get("body");
//
//						boolean correctlyFormatted = requestLine.setBody(receivedBody);
//						
//						if (correctlyFormatted){
//							ArrayList<ArrayList<String>> secondaryList = (ArrayList<ArrayList<String>>)requestLine.getValueFromParam("secondaries");
//							
//							if (secondaryList != null){
//								for (ArrayList<String> second: secondaryList){
//									System.out.println("IP: " + second.get(0) + ", PORT: " + second.get(1));
//									database.addSecondary(second.get(0), second.get(1));
//								}
//							}
//						
//							boolean modified = false;
//							ArrayList<HashMap<String, String>> email_password_log = (ArrayList<HashMap<String, String>>) requestLine.getValueFromParam("email_password_log");
//							if (email_password_log != null){
//								modified = true;
//								for(HashMap<String, String> log: email_password_log){
//									log.put("prepare_time_stamp", log.get("time_stamp"));
//									this.database.addToQeueu(log);
//								}
//								this.database.processQueue();
//							} 
//							
//							ArrayList<HashMap<String, String>> profile_transaction_log = (ArrayList<HashMap<String, String>>) requestLine.getValueFromParam("profile_transaction_log");
//							if (profile_transaction_log != null){
//								modified = true;
//								for(HashMap<String, String> log: profile_transaction_log){
//									log.put("prepare_time_stamp", log.get("time_stamp"));
//									this.database.addToQeueu(log);
//								}
//								this.database.processQueue();
//							} 						
//							
//							if (modified){
//								System.out.println("SERVER WAS NOT UP TO DATE, DATA RECEIVED");
//							} else {
//								System.out.println("SERVER IS UP TO DATE, NO DATA RECEIVED");
//							}
//						} else {
//							Utilities.debug("The body received was not formatted properly.\nBody Received " + receivedBody);
//						}
//					}
					
				}
			} else {
				status = "SECONDARY";
				System.out.println("I am staying as a secondary. Updating new primary");
				this.database.setPrimaryInfo(greatestReceivedIp, greatestReceivedPort);
				this.primaryIP = greatestReceivedIp;
				this.primaryPort = greatestReceivedPort;
			}
		}
	}
	
	/**
	 * Update the discovery server with the latest primary info
	 */
	
//	public void forwardToSecondaries(){
//		
//		System.out.println("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW");
//		
//		ArrayList<ArrayList<String>> secondaries = this.database.getSecondaries();
//		
//		System.out.println("Known Secondaries: " + secondaries.size());
//		
//		ArrayList<ArrayList<Object>> secondaryResponses = new ArrayList<ArrayList<Object>>();
//		
//		if (secondaries.size() == 0){
//			status = "PRIMARY";
//			System.out.println("Notify disocvery server");
//		} else {
//			for(ArrayList<String> secondary: secondaries){
//				String secondaryIP = secondary.get(0);
//				Integer secondaryPORT = Integer.parseInt(secondary.get(1));
//				
//				Socket socket = null;
//				DataOutputStream out = null;
//				DataInputStream in = null;
//				
//				HashMap response = new HashMap();
//				
//				System.out.println("Connecting to Secondary ...");
//				System.out.println("Secondary IP: " + secondaryIP);
//				System.out.println("Secondary Port: " + secondaryPORT);
////				HTTPServer.log.debug("Connecting to Secondary ...");
//				
//				try {
//					/**
//					 * Establish connection
//					 */
//					socket = new Socket(secondaryIP, secondaryPORT);
//					out = new DataOutputStream(socket.getOutputStream());
//					in = new DataInputStream(socket.getInputStream());
//				
//					System.out.println("Connected to Secondary with IP " + secondaryIP + " PORT " + secondaryPORT);
////					HTTPServer.log.debug("Connected to Secondary with IP " + secondaryIP + " PORT " + secondaryPORT);
//
//					/**
//					 * Write to BackEndServer
//					 */
//					HashMap<String, Object> sendThis = new HashMap<String, Object>();
//					sendThis.put("ID", discoveryReceivePort);
//					sendThis.put("Version", this.database.getDatabaseVersion());
//					String jsonText = JSONValue.toJSONString(sendThis);
//					
//					String request = "POST /election HTTP/1.1";
//					String body = "" + jsonText + " ";
//					
//					out.writeBytes(request);
//					out.writeBytes("\n");
//					
//					out.writeBytes("Content-Length: " + body.getBytes().length);
//					out.writeBytes("\n");
//					out.writeBytes("\n");
//
//					out.writeBytes(body);
//					
//					out.flush();
//					
//					System.out.println("Sent request to backend");
//					
//					String line = "";
//					boolean readFirstLine = false;
//					int length = 0;
//					
//					/**
//					 * Read response from BackEndServer
//					 */
//					try {
//						while ((line = in.readLine()) != null) {
//							
//							/*
//							 * Last line of request header is a blank line
//							 * Quit while loop when last line of header is reached
//							 */
//							if (line.equals("")) {
//								System.out.println("FrontEndServerProcessor STOP READING");
////								HTTPServer.log.debug("FrontEndServerProcessor STOP READING");
//								break; 
//							}
//							
//							if (!readFirstLine){
//								response.put("response", line);
//								readFirstLine = true;
//							}
//							
//							if (line.startsWith("Content-Length: ")) { 
//								int index = line.indexOf(':') + 1;
//								String len = line.substring(index).trim();
//								length = Integer.parseInt(len);
//							}
//							
//							System.out.println("FrontEndServer RECEIVED: " + line);
////							HTTPServer.log.debug("FrontEndServer RECEIVED: " + line);
//						}
//					} catch (NumberFormatException | IOException e) {
//						response.put("error", "FrontEndServerProcessor failed to read request");
//						System.out.println("FrontEndServerProcessor failed to read request.\nError Message: " + e.getLocalizedMessage());
////						HTTPServer.log.debug("FrontEndServerProcessor failed to read request.\nError Message: " + e.getLocalizedMessage());
//					} 
//
//					String responseBody = "";
//					
//					/*
//					 * If a message body was found, read it
//					 */
//					if (length > 0) {
//						int read;
//						try {
//							while ((read = in.read()) != -1) {
//								responseBody+= (char) read;
//								if (responseBody.length() == length)
//								break;
//							}
//						} catch (IOException e) {
//							System.out.println("FrontEndServerProcessor failed to read message body.\nError Message: "+ e.getLocalizedMessage());
////							HTTPServer.log.debug("FrontEndServerProcessor failed to read message body.\nError Message: "+ e.getLocalizedMessage());
//						}
//					}
//					
//					System.out.println("FrontEndServerProcessor RECEIVED BODY " + responseBody);
////					HTTPServer.log.debug("FrontEndServerProcessor RECEIVED BODY " + responseBody);
//					response.put("body", responseBody);
//						
//					out.flush();
//
//					out.close();
//					in.close();
//					
//					HTTPRequestLine tempRequestLine = new HTTPRequestLine();
//					String body2 = (String) response.get("body");
//
//					boolean correctlyFormatted = tempRequestLine.setBody(body2);
//					
//					if (correctlyFormatted){
//						String serverType =  (String) tempRequestLine.getValueFromParam("serverType");								
//						System.out.println(serverType);
//						
//						ArrayList<Object> secondaryInfo = new ArrayList<Object>();
//						secondaryInfo.add(serverType);
//						secondaryInfo.add(secondaryIP);
//						secondaryInfo.add(""+secondaryPORT);
//						
//						ArrayList<HashMap> logs = (ArrayList<HashMap>) tempRequestLine.getValueFromParam("data");
//
//						if (logs != null){
//							secondaryInfo.add(logs);
//							secondaryInfo.add(""+logs.size());
//						}
//						
//						secondaryResponses.add(secondaryInfo);
//					}
//
//				} catch (UnknownHostException e) {
//					response.put("error", "FrontEndServerProcessor UnknownHostException");
//
//					System.out.println("\nFrontEndServerProcessor UnknownHostException\nError Message: " + e.getLocalizedMessage() + "\n");
////					HTTPServer.log.debug("\nFrontEndServerProcessor UnknownHostException\nError Message: " + e.getLocalizedMessage() + "\n");
//				} catch (IOException e) {
//					response.put("error", "nFrontEndServerProcessor IOException");
//
//					System.out.println("\nFrontEndServerProcessor IOException\nError Message: " + e.getLocalizedMessage() + "\n");
////					HTTPServer.log.debug("\nFrontEndServerProcessor IOException\nError Message: " + e.getLocalizedMessage() + "\n");
//				} finally {
//					try {
//						socket.close();
//					} catch (IOException e) {
//						response.put("error", "FrontEndServerProcessor IOException closing socket");
//
//						System.out.println("\nFrontEndServerProcessor IOException closing socket\nError Message: " + e.getLocalizedMessage() + "\n");
////						HTTPServer.log.debug("\nFrontEndServerProcessor IOException closing socket\nError Message: " + e.getLocalizedMessage() + "\n");
//					} catch (Exception e){
//						response.put("error", "FrontEndServerProcessor Exception closing socket");
//
//						System.out.println("\nFrontEndServerProcessor Exception closing socket\nError Message: " + e.getLocalizedMessage() + "\n");
////						HTTPServer.log.debug("\nFrontEndServerProcessor Exception closing socket\nError Message: " + e.getLocalizedMessage() + "\n");
//					}
//				}
//				System.out.println(response);
//			}
//			
//			boolean iAmTheNewPrimary = true;
//			String newPrimaryIP = "";
//			String newPrimaryPort = "";
//			
//			ArrayList<HashMap> addToDatabase = new ArrayList<HashMap>();
//			int maxSize = -1;
//			
//			for (ArrayList<Object> secondaryInfo: secondaryResponses){
//				String serverType = (String) secondaryInfo.get(0);
//				System.out.println("Roles of other secondaries: " + serverType);
//				
//				if (serverType.equals("PRIMARY")){
//					iAmTheNewPrimary = false;
//					newPrimaryIP = (String) secondaryInfo.get(1);
//					newPrimaryPort = (String) secondaryInfo.get(2);
//					break;
//				}
//				
//				int secondaryInfoSize = secondaryInfo.size();
//				if (secondaryInfoSize > 3){
//					int currentSize = Integer.parseInt(""+secondaryInfo.get(4));
//					if (currentSize > maxSize){
//						maxSize = currentSize;
//						addToDatabase = (ArrayList<HashMap>) secondaryInfo.get(3);
//					}
//				}
//			}
//			
//			if (maxSize != -1){
//				////////////////////////////////////////////////
//				System.out.println("INCONSISTENT DATABASES AFTER ELECTING A NEW PRIMARY. FIXING DBS");
//				int count = 1;
//				for(HashMap log: addToDatabase){
//					System.out.println("COUNT: " + count);
//					ArrayList<String> hashtags = (ArrayList<String>) log.get("hashtags");
//					String tweet = (String) log.get("tweet");
//					
//					this.database.addTweet(hashtags, tweet);					
//				}
//			}
//			
//			if (iAmTheNewPrimary){
//				status = "PRIMARY";
//			} else {
//				status = "SECONDARY";
//				primaryIP = newPrimaryIP;
//				primaryPort = Integer.parseInt(newPrimaryPort);
//				this.database.setPrimaryInfo(newPrimaryIP, Integer.parseInt(newPrimaryPort));
//			}
//			this.database.setStatus(status);
//		}
//	}
	
	public void contactDiscoveryServer(){
		HashMap<String, Object> sendingBody = new HashMap<String, Object>();
		sendingBody.put("ip", this.localIp);
		sendingBody.put("port", this.localPort);
		
		HashMap<String, String> response = this.connectToServer(sendingBody, "DiscoveryServer", database.getDiscoveryIP(), database.getDiscoveryPort(), "POST", "/discovery");
		
		if (response.containsKey("error")){
			System.out.println(response.get("error"));
		} 		
	}

	/**
	 * Generic way to contact a server
	 * @param information - body
	 * @param serverName - name of the server for debugging purposes
	 * @param targetIp - ip of the server
	 * @param targetPort - port of the = server
	 * @param targetProtocol - protocol you wish to use
	 * @param targetUri - uri you wish to use
	 * @return response from that server
	 */
	public HashMap<String, String> connectToServer(HashMap<String, Object> information, String serverName, String targetIp, int targetPort, String targetProtocol, String targetUri){
		Socket socket = null;
		DataOutputStream out = null;
		DataInputStream in = null;
		
		HashMap<String, String> response = new HashMap<String, String>();
		
		System.out.println("Connecting to " + serverName + " ...");

		try {
			/**
			 * Establish connection
			 */
			socket = new Socket(targetIp, targetPort);
			out = new DataOutputStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
		
			System.out.println("Connected to " + serverName + " with IP " + targetIp + " PORT " + targetPort);
			
			String request = targetProtocol + " " + targetUri + " HTTP/1.1";
			
			String jsonText = JSONValue.toJSONString(information);
			String body = "" + jsonText + " ";
			
			out.writeBytes(request);
			out.writeBytes("\n");
			
			out.writeBytes("Content-Length: " + body.getBytes().length);
			out.writeBytes("\n");
			out.writeBytes("\n");

			out.writeBytes(body);
			
			out.flush();
			
			System.out.println("Successfully sent request");
			
			String line = "";
			boolean readFirstLine = false;
			int length = 0;
			
			/**
			 * Read server response 
			 */
			try {
				while ((line = in.readLine()) != null) {
					
					if (line.equals("")) {
						System.out.println(serverName + " STOP READING");
						break; 
					}
					
					if (!readFirstLine){
						response.put("response", line);
						readFirstLine = true;
					}
					
					if (line.startsWith("Content-Length: ")) { 
						int index = line.indexOf(':') + 1;
						String len = line.substring(index).trim();
						length = Integer.parseInt(len);
					}
					
					System.out.println(serverName + " RECEIVED: " + line);
				}
			} catch (NumberFormatException | IOException e) {
				response.put("error", serverName + " failed to read request.\nError Message: " + e.getLocalizedMessage());
				Utilities.debug(serverName + " failed to read request.\nError Message: " + e.getLocalizedMessage());
			} 

			String responseBody = "";
			
			/*
			 * If a message body was found, read it
			 */
			if (length > 0) {
				int read;
				try {
					while ((read = in.read()) != -1) {
						responseBody+= (char) read;
						if (responseBody.length() == length)
						break;
					}
				} catch (IOException e) {
					Utilities.debug(serverName + " failed to read message body.\nError Message: "+ e.getLocalizedMessage());
				}
			}
			
			Utilities.debug(serverName + " RECEIVED BODY " + responseBody);
			response.put("body", responseBody);
				
			out.flush();

			out.close();
			in.close();

		} catch (UnknownHostException e) {
			response.put("error", serverName + " UnknownHostException");
			System.out.println("\n" + serverName + " UnknownHostException\nError Message: " + e.getLocalizedMessage() + "\n");
		} catch (IOException e) {
			response.put("error", "\n" + serverName + " IOException");
			System.out.println("\n" + serverName + " IOException\nError Message: " + e.getLocalizedMessage() + "\n");
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				response.put("error", serverName + " IOException closing socket");
				System.out.println("\n" + serverName + " IOException closing socket\nError Message: " + e.getLocalizedMessage() + "\n");
			} catch (Exception e){
				response.put("error", serverName + " Exception closing socket");
				System.out.println("\n" + serverName +" Exception closing socket\nError Message: " + e.getLocalizedMessage() + "\n");
			}
		}
			
		System.out.println(response);
		return response;
	}
}
