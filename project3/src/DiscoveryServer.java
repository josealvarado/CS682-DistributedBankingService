import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiscoveryServer {
	/**
	 * DiscoveryServer variables: IP, PORT, database
	 */
	private String ip;
	private int port;
	private BackEndDatabase serverList;
	
	/**
	 * DiscoveryServer constructor
	 * @param ip
	 * @param port
	 */
	public DiscoveryServer(String ip, int port){
		this.ip = ip;
		this.port = port;
		serverList = new BackEndDatabase();		
	}
	
	/**
	 * Start DiscoveryServer 
	 */
	public void startServer(){
		System.out.println("DiscoveryServer Started");
		
		ExecutorService executor = Executors.newFixedThreadPool(100);		
		ServerSocket serverSocket = null;
		InetAddress bindAddr = null;
		try {
			bindAddr = InetAddress.getByName(ip);
		} catch (UnknownHostException e1) {
			Utilities.debug("DiscoveryServer UnknownHostException unable to get IP " + ip + ". Error Message: " + e1.getLocalizedMessage());
			return;
		}

		/**
		 * Bind Server with IP and PORT 
		 */
		try {
			serverSocket = new ServerSocket(port, 10000, bindAddr);			
		} catch (IOException e) {
			Utilities.debug("DiscoveryServer IOException unable to create ServerSocket with port " + port + " and IP " + ip + ". Error Message: " + e.getLocalizedMessage());
			return;
		}

		/**
		 * DiscoveryServer waits for requests
		 * creates a new DiscoveryServerProcessor thread for each request
		 */
		while (true) {
			Utilities.debug("DiscoveryServer Waiting for request");
		   	Socket clientSocket = null;
		   	try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				Utilities.debug("DiscoveryServer IOException unable to accept requests\nError Message: " + e.getLocalizedMessage());
			}
		    
			executor.execute(new DiscoveryServerProcessor(clientSocket, serverList));	
		}
	}
}
