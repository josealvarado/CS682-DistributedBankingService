import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.json.simple.JSONValue;


public class Utilities {

	public static void debug(String text){
		System.out.println(text);
		StartServer.log.debug(text);
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
	@SuppressWarnings("deprecation")
	public static HashMap<String, String> connectToServer(HashMap<String, Object> information, String serverName, String targetIp, int targetPort, String targetProtocol, String targetUri){
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
