import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackEndServer extends HTTPServer{

	/**
	 * BackEndServer variables: IP, PORT, database
	 */
	private BackEndDatabase database;

	/**
	 * BackEndServer constructor
	 * @param ip
	 * @param port
	 */
	public BackEndServer(String ip, int port, String primaryIP, int primaryPort, int backEndDiscoveryPort, String discoveryIP, int discoveryPort){
		database = new BackEndDatabase();
		database.setBackendInfo(ip, port);
		database.setPrimaryInfo(primaryIP, primaryPort);
		database.setDiscoveryInfo(discoveryIP, discoveryPort);
		database.setBackEndDiscoveryReceivePort(backEndDiscoveryPort);
	}
	
	/**
	 * Start BackEndServer 
	 */
	public void startServer(){
		System.out.println("BackEndServer Started");
		
		BackEndToFrontEnd frontEndCommunication = new BackEndToFrontEnd(database);
		BackEndToBackEnd backEndCommunication = new BackEndToBackEnd(this.database);
		BackEndServerDiscoverySend discoveryRequest = new BackEndServerDiscoverySend(database);

		ExecutorService executor = Executors.newFixedThreadPool(3);	
		executor.execute(frontEndCommunication);
		executor.execute(backEndCommunication);
		executor.execute(discoveryRequest);	
	}
}
