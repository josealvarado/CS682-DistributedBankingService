import java.net.Socket;

public class DiscoveryServerProcessor extends Processor{
	/**
	 * DiscoveryServerProcessor variables: clientSocket, database
	 */
//	private Socket clientSocket;
	private BackEndDatabase database;
	
	/**
	 * BackEndServerProcessor constructor
	 * @param clientSocket
	 * @param database
	 */
	public DiscoveryServerProcessor(Socket clientSocket, BackEndDatabase database) {
		this.clientSocket = clientSocket;
		this.database = database;
		this.name = "DiscoveryServer";
	}
	
	@Override
	public void handlePost(String uri) {
		if (uri.equals("/discovery")){
			String ip = (String) requestLine.getValueFromParam("ip");
			Integer port = Integer.parseInt("" + requestLine.getValueFromParam("port"));
			if (ip != null && port != null){
				
				System.out.println("Primary Server Updated. New IP: " + ip + ". New Port: " + port);
				
				database.setPrimaryInfo(ip, port);
				
				json.put("statusCode", "201");
				json.put("message", "POST Correctly formattted");
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 201 Created\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";								
			} else {
				json.put("message", "POST ERROR MISSING CORRECT KEYS");
				responseBody = json.toJSONString();
				responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
			}
		} else {
			json.put("message", "POST ERROR  MISSING URI");
			responseBody = json.toJSONString();
			responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
		}
		
	}

	@Override
	public void handleGet(String uri) {
		if (uri.equals("/discovery")){	
			json.put("statusCode", "200");
			json.put("message", "GET Correctly Formatted");
			json.put("ip", database.getPrimaryIP());
			json.put("port", database.getPrimaryPort());
			responseBody = json.toJSONString();
			System.out.println(responseBody);
			responseHeader = "HTTP/1.1 200 OK\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
		} else {
			json.put("message", "GET ERROR MISSING URI");
			responseBody = json.toJSONString();
			responseHeader = "HTTP/1.1 400 Bad Request\n" + "Content-Length: " + responseBody.getBytes().length + "\n\n";
		}
	}
}
