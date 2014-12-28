import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackEndToBackEnd implements Runnable{
	
	private Socket clientSocket;
	private BackEndDatabase database;
		
	private String ip;
	private int backEndDiscoveryPort;
	/**
	 * BackEndServerProcessor constructor
	 * @param clientSocket
	 * @param database
	 */
	public BackEndToBackEnd(BackEndDatabase database) {
		this.ip = database.getPrimaryIP();
		this.backEndDiscoveryPort = database.getDiscoveryReceivePort();
		this.database = database;
	}
	
	@Override
	public void run() {
		System.out.println("BackEndToBackEnd Discovery Service Started");
		
		
		
		ExecutorService executor = Executors.newFixedThreadPool(100);		
		ServerSocket serverSocket = null;
		InetAddress bindAddr = null;
		try {
			bindAddr = InetAddress.getByName(ip);
		} catch (UnknownHostException e1) {
			Utilities.debug("BackEndToBackEnd UnknownHostException unable to get IP " + ip + ". Error Message: " + e1.getLocalizedMessage());
			return;
		}

		/**
		 * Bind Server with IP and PORT 
		 */
		try {
			serverSocket = new ServerSocket(backEndDiscoveryPort, 10000, bindAddr);			
		} catch (IOException e) {
			Utilities.debug("BackEndToBackEnd IOException unable to create ServerSocket with port " + backEndDiscoveryPort + " and IP " + ip + ". Error Message: " + e.getLocalizedMessage());
			return;
		}
		
		/**
		 * BackEndServer waits for requests
		 * creates a new BackEndServerProcessor thread for each request
		 */
		while (true) {
		   	Utilities.debug("BackEndToBackEnd Discovery Service Waiting for request");
		   	Socket clientSocket = null;
		   	try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				Utilities.debug("BackEndToBackEnd IOException unable to accept requests\nError Message: " + e.getLocalizedMessage());
			}
		    
			executor.execute(new BackEndToBackEndProcessor(clientSocket, database));	
		}
	}
}
