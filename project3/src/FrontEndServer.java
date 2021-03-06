import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FrontEndServer {
	
	/*
	 * FrontEndServer variables: IP, PORT, cache, backEndServerIP, backEndServerPORT
	 */
	private String IP;
	private int PORT;
	private FrontEndServerCache cache;
	
	private String backEndServerIP;
	private int backEndServerPORT;
	
	private String discoveryIP;
	private int discoveryPort;
	
	/**
	 * FrontEndServer constructor
	 * @param ip
	 * @param port
	 * @param backEndServerIP
	 * @param backEndServerPORT
	 */
	public FrontEndServer(String ip, int port, String backEndServerIP, int backEndServerPORT, String discoveryIP, int discoveryPort){
		IP = ip;
		PORT = port;
		cache = new FrontEndServerCache();
		
		this.backEndServerIP = backEndServerIP;
		this.backEndServerPORT = backEndServerPORT;
		
		this.discoveryIP = discoveryIP;
		this.discoveryPort = discoveryPort;
	}
	
	/**
	 * Start FrontEndServer
	 */
	public void startServer(){
		Utilities.debug("FronEndServer Started");
		
		ExecutorService executor = Executors.newFixedThreadPool(100);		
		ServerSocket serverSocket = null;
		InetAddress bindAddr = null;
		try {
			bindAddr = InetAddress.getByName(IP);
		} catch (UnknownHostException e1) {
			Utilities.debug("FronEndServer UnknownHostException unable to get IP " + IP + ".\nError Message: " + e1.getLocalizedMessage());
			return;
		}

		/**
		 * Bind Server with IP and PORT 
		 */
		try {
			serverSocket = new ServerSocket(PORT, 5, bindAddr);
		} catch (IOException e) {
			Utilities.debug("FronEndServer IOException unable to create ServerSocket with port " + PORT + " and IP " + IP + ". Error Message: " + e.getLocalizedMessage());
			return;
		}
		 
		/**
		 * FronEndServer waits for requests
		 * creates a new FronEndServerProcessor thread for each request
		 */
		while (true) {
			Utilities.debug("FronEndServer Waiting for request");
		   	Socket clientSocket = null;
		   	try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				Utilities.debug("FronEndServer unable to accept requests.\nError Message: " + e.getLocalizedMessage());
			}
		    
			executor.execute(new FrontEndServerProcessor(clientSocket, cache, backEndServerIP, backEndServerPORT, discoveryIP, discoveryPort));	
		}
	}
}
