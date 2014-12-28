import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackEndToFrontEnd implements Runnable{

	public String ip;
	public int port;
	public BackEndDatabase database;
	
	public BackEndToFrontEnd(BackEndDatabase database){
		this.ip = database.getBackEndIP();
		this.port = database.getBackEndPort();
		this.database = database;
	}
	
	public void run(){
		System.out.println("BackEndToFrontEnd Service Started");

		ExecutorService executor = Executors.newFixedThreadPool(100);		
		ServerSocket serverSocket = null;
		InetAddress bindAddr = null;
		
		/**
		 * Bind socket with ip and port 
		 */
		try {
			bindAddr = InetAddress.getByName(this.ip);
			serverSocket = new ServerSocket(this.port, 10000, bindAddr);			

		} catch (UnknownHostException e1) {
			Utilities.debug("BackEndToFrontEnd UnknownHostException unable to get IP " + this.ip + ". Error Message " + e1.getLocalizedMessage());
			return;
		} catch (IOException e) {
			Utilities.debug("BackEndToFrontEnd IOException unable to create ServerSocket with port " + this.port + " and IP " + this.port + ". Error Message: " + e.getLocalizedMessage());
			return;
		}

		/**
		 * BackEndToFrontEnd waits for requests
		 * creates a new BackEndServerProcessor thread for each request
		 */
		while (true) {	
		   	Utilities.debug("BackEndToFrontEnd Waiting for request");
		   	
		   	Socket clientSocket = null;
		   	try {
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				Utilities.debug("BackEndToFrontEnd IOException unable to accept requests\nError Message: " + e.getLocalizedMessage());
			}
		    
			executor.execute(new BackEndToFrontEndProcessor(clientSocket, database));	
		}
	}
}
