
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

public class BackEndDatabase {
	
	/*
	 * Private variables
	 */
	private String name;
	
	private int timeStamp;
	private int prepareTimeStamp;
	private int queueTimeStamp;
	private HashMap<String, HashMap<String, String>> queue;				//after primary sends commit
	private HashMap<String, HashMap<String, String>> prepareQueue;		//before primary sends commit
	private HashMap<String, String> lastProfileLogMap;
	
	// Used for login
	private HashMap<String, String> emailToIdMap;
	private HashMap<String, String> idToPasswordMap;
	
	private ArrayList<HashMap<String, String>> emailPasswordLog;
	private int emailPasswordDatabaseVersion;
	
	private HashMap<String, Profile> idToProfileMap;
	private HashMap<String, Integer> idToProfileVersionMap;
	
//	private ArrayList<HashMap<String, Object>> profileTransactionLog = new ArrayList<HashMap<String, Object>>();
	private ArrayList<HashMap<String, String>> profileTransactionLog;
	private int profileDatabaseVersion;

	private MultiReaderLock lock = new MultiReaderLock();
	
	private ArrayList<ArrayList<String>> secondaries;

	private String primaryIP;
	private int primaryPort;
	
	private String backendIP;
	private int backendPort;
	
	private int discoverySendPort;
	private int discoveryReceivePort;
	
	private String status;
	
	private String disoveryIP;
	private Integer discoveryPort;
	
	
	// Old stuff I need to delete
	private HashMap<String, ArrayList<String>> tweetMap;
	private HashMap<String, Integer> versionMap;
	private int databaseVersion;
	private ArrayList<HashMap<String, Object>> log = new ArrayList<HashMap<String, Object>>();
	
	/**
	 * Default constructor
	 */
	public BackEndDatabase(){
		tweetMap = new HashMap<String, ArrayList<String>>();
		versionMap = new HashMap<String, Integer>();
		databaseVersion = 0;

		timeStamp = 0;
		queueTimeStamp = 0;
		prepareTimeStamp = 0;
		profileDatabaseVersion = 0;
		emailPasswordDatabaseVersion = 0;
		emailToIdMap = new HashMap<String, String>();
		idToPasswordMap = new HashMap<String, String>();
		emailPasswordLog = new ArrayList<HashMap<String, String>>();
		idToProfileMap = new HashMap<String, Profile>();
		idToProfileVersionMap = new HashMap<String, Integer>();
		profileTransactionLog = new ArrayList<HashMap<String, String>>();
		queue = new HashMap<String, HashMap<String, String>>();
		prepareQueue = new HashMap<String, HashMap<String, String>>();
		lastProfileLogMap = new HashMap<String, String>();
		secondaries = new ArrayList<ArrayList<String>>();
	}
	
	public ArrayList<HashMap<String, String>> getPrepareQueue(){
		ArrayList<HashMap<String, String>> prep = new ArrayList<HashMap<String, String>>();
		lock.lockWrite();
		for (Entry<String, HashMap<String, String>> entry : this.prepareQueue.entrySet()) {
//		    String key = entry.getKey();
		    HashMap<String, String> value = entry.getValue();
		    prep.add(value);
		}
		lock.unlockWrite();
		return prep;
	}
	
	public boolean existInPrepQueue(HashMap<String, String> prep){
		if (this.prepareQueue.containsKey(prep.get("prepare_time_stamp"))){
			return true;
		}
		
		return false;
	}
	
	public int getPrepareQueueSize(){
		int size = -1;
		lock.lockRead();
		size = this.prepareQueue.size();
		lock.unlockRead();
		return size;
	}
	
	public void addToQeueu(HashMap<String, String> log){
		lock.lockWrite();
//		queue.put(log.get("time_stamp"), log);
		queue.put(log.get("prepare_time_stamp"), log);
		lock.unlockWrite();
	}
	
	public void processQueue(){
		while(queue.containsKey("" + (timeStamp + 1))){
			System.out.println("Processed timeStamp " +( timeStamp + 1));
			
			HashMap<String, String> log = queue.get("" + (timeStamp + 1));
			queue.remove("" + (timeStamp + 1));
			
			String password = (String) log.get("password");
			String id = (String) log.get("id");
			String email = (String) log.get("email");
			String name = (String) log.get("name");
						
			String updatedName = (String) log.get("updated_name");
			String newName = (String) log.get("new_name");
			
			String createdAccount = (String) log.get("created_account");
			String accountId = (String) log.get("account_id");
			
			String didTransaction = (String) log.get("did_transaction");
			String transactionId = (String) log.get("transaction_id");
			String amountString = (String) log.get("amount");
			String type = (String) log.get("type");
			
			if (updatedName != null){
				System.out.println("updated name");
				HashMap<String, String> result = updateProfileName(email, password, newName);
				if (result.containsKey("error")){
					System.out.println(result.get("error"));
				}
			} else if (createdAccount != null){
				System.out.println("created account");

				HashMap<String, String> result = createNewAccount(email, password);
				if (result.containsKey("error")){
					System.out.println(result.get("error"));
				}
			} else if (didTransaction != null){
				
				System.out.println("did transaction");

				Double amount = Double.parseDouble(amountString);
				HashMap<String, String> result = createNewTransaction(email, password, accountId, type, amount);
				if (result.containsKey("error")){
					System.out.println(result.get("error"));
				}
			} else {
				System.out.println("created profile");

//				createNewProfile(email, password, name);	
				
				HashMap<String, String> result = createNewProfile(email, password, name);
				if (result.containsKey("error")){
					System.out.println(result.get("error"));
				}
			}
		}
	}
	
	public int getEmailPasswordDatabaseVersion(){
		return this.emailPasswordDatabaseVersion;
	}
	
	public int getProfileDatabaseVersion(){
		return this.profileDatabaseVersion;
	}
	
	public HashMap<String, String> getLastProfileLogMap(){
		HashMap<String, String> profileLogMap = new HashMap<String, String>();
		lock.lockWrite();
		profileLogMap = this.lastProfileLogMap;
		lock.unlockWrite();
		return profileLogMap;
	}
	
	public HashMap<String, String> putPrepareProfileMapInPrepareQueue(String email, String password, String name, String prepareTimeStamp){
		HashMap<String, String> prepareProfileMap = new HashMap<String, String>();
		prepareProfileMap.put("email", email);
		prepareProfileMap.put("password", password);
		prepareProfileMap.put("name", name);
		prepareProfileMap.put("prepare_time_stamp", prepareTimeStamp);
		lock.lockWrite();
		this.prepareQueue.put(prepareTimeStamp, prepareProfileMap);
		lock.unlockWrite();
		return prepareProfileMap;
	}
	
	public void putPrepareMapInPrepareQueueW(HashMap<String, String> prepareMap){
		lock.lockWrite();
		// Does this really work? Might need to be moved too removeFromPrepareQueue
		this.prepareQueue.put(prepareMap.get("prepare_time_stamp"), prepareMap);
		lock.unlockWrite();
	} 
	
	public void putPrepareMapInPrepareQueue(HashMap<String, String> prepareMap){
		lock.lockWrite();
		// Does this really work? Might need to be moved too removeFromPrepareQueue
		prepareTimeStamp += 1;
		this.prepareQueue.put(prepareMap.get("prepare_time_stamp"), prepareMap);
		lock.unlockWrite();
	} 
	
	public HashMap<String, String> removeFromPrepareQueue(String prepareTimeStamp){
		HashMap<String, String> result = new HashMap<String, String>();
		lock.lockWrite();
		if (prepareQueue.containsKey(prepareTimeStamp)){
			HashMap<String, String> preparedMap = prepareQueue.get(prepareTimeStamp);
			prepareQueue.remove(prepareTimeStamp);
//			System.out.println("Send to queue");
//			System.out.println(preparedMap);
			queue.put(prepareTimeStamp, preparedMap);			
		} else {
			System.out.println("IMPOSSIBLE, THIS SHOULD BE IN THE PREPARE QUEUE");
		}
		lock.unlockWrite();
		return result;
	}

	public HashMap<String, String> prepareUpdateNameProfileMap(String email, String password, String newName){
		HashMap<String, String> prepareProfileMap = new HashMap<String, String>();
		lock.lockWrite();
		prepareTimeStamp += 1;
		ProfileLog profileLog = new ProfileLog();
		profileLog.prepareNewName(email, password, newName, "" + prepareTimeStamp);	
		prepareProfileMap = profileLog.getPrepareUpdateNameProfileMap();
		this.prepareQueue.put("" + prepareTimeStamp, prepareProfileMap);
		lock.unlockWrite();
		return prepareProfileMap;
	}
	
	public HashMap<String, String> prepareNewProfile(String email, String password, String name){
		HashMap<String, String> prepareProfileMap = new HashMap<String, String>();
		lock.lockWrite();
		prepareTimeStamp += 1;
		ProfileLog profileLog = new ProfileLog();
		profileLog.prepareProfile(email, password, name, "" + prepareTimeStamp);	
		prepareProfileMap = profileLog.getPrepareProfileMap();
		this.prepareQueue.put("" + prepareTimeStamp, prepareProfileMap);
		lock.unlockWrite();
		return prepareProfileMap;
	}
	
	public HashMap<String, String> prepareNewAccountProfile(String email, String password){
		HashMap<String, String> prepareProfileMap = new HashMap<String, String>();
		lock.lockWrite();
		prepareTimeStamp += 1;
		ProfileLog profileLog = new ProfileLog();
		profileLog.prepareNewAccount(email, password, "" + prepareTimeStamp);		
		prepareProfileMap = profileLog.getPrepareNewAccountMap();		
		this.prepareQueue.put("" + prepareTimeStamp, prepareProfileMap);
		lock.unlockWrite();
		return prepareProfileMap;
	}
	
	public HashMap<String, String> prepareNewTransaction(String email, String password, String accountId, String type, Double amount){
		HashMap<String, String> prepareProfileMap = new HashMap<String, String>();
		lock.lockWrite();
		prepareTimeStamp += 1;
		ProfileLog profileLog = new ProfileLog();
		
		profileLog.prepareNewTransactoin(email, password, accountId, type, amount, "" + prepareTimeStamp);
		prepareProfileMap = profileLog.getPrepareTransactionMap();		
		
		this.prepareQueue.put("" + prepareTimeStamp, prepareProfileMap);
		lock.unlockWrite();
		return prepareProfileMap;
	}

	public HashMap<String, String> createNewProfile(String email, String password, String name){
		HashMap<String, String> result = new HashMap<String, String>();

		if(emailToIdMap.containsKey(email)){
			result.put("error", "Profile already exist");
		} else {
			String profileId = "";
			lock.lockWrite();
			profileId = ""+idToProfileMap.size();
			Profile profile = new Profile(name);
			this.idToProfileMap.put(profileId, profile);
//			this.profileDatabaseVersion += 1;
			this.idToProfileVersionMap.put(profileId, 0);
			
			this.emailToIdMap.put(email, profileId);
			this.idToPasswordMap.put(profileId, password);
			this.emailPasswordDatabaseVersion += 1;
			
			timeStamp += 1;
			ProfileLog profileLog = new ProfileLog();
			profileLog.createdProfile(profileId, email, password, name, "" + timeStamp);
			
			emailPasswordLog.add(profileLog.getEmailPasswordMap());
			this.lastProfileLogMap = profileLog.getEmailPasswordMap();
			
			lock.unlockWrite();	
		}
			
		return result;
	}
	
	private HashMap<String, String> validCredentials(String email, String password){
		HashMap<String, String> result = new HashMap<String, String>();
		lock.lockRead();
		if (this.emailToIdMap.containsKey(email)){
			String profileId = this.emailToIdMap.get(email);
			String profilePassword = this.idToPasswordMap.get(profileId);
			if (profilePassword.equals(password)){
				result.put("id", profileId);
			} else {
				result.put("error", "Incorrect login credentials");
			}
		} else {
			result.put("error", "Account not found");
		}
		lock.unlockRead();
		return result;
	}
	
	public HashMap<String, Object> getProfile(String email, String password){
		HashMap<String, String> validCredentials = this.validCredentials(email, password);
		HashMap<String, Object> result = new HashMap<String, Object>();
		if (validCredentials.containsKey("error")){
			result.put("error", validCredentials.get("error"));
		} else {
			String id = validCredentials.get("id");
			result.put("id", id);
			lock.lockRead();
			Profile profile = this.idToProfileMap.get(id);
			HashMap<String, Object> profileMap = profile.getProfileMap();
			Integer profileVersion = this.idToProfileVersionMap.get(id);
			result.put("version", "" + profileVersion);
			result.putAll(profileMap);
			result.put("status_code", "200");
			lock.unlockRead();
		}		
		
		return result;
	}
	
	public HashMap<String, Object> getProfileVersion(String email, String password){
		HashMap<String, String> validCredentials = this.validCredentials(email, password);
		HashMap<String, Object> result = new HashMap<String, Object>();
		if (validCredentials.containsKey("error")){
			result.put("error", validCredentials.get("error"));
		} else {
			String id = validCredentials.get("id");
//			result.put("id", id);
			lock.lockRead();
//			Profile profile = this.idToProfileMap.get(id);
//			HashMap<String, Object> profileMap = profile.getProfileMap();
			Integer profileVersion = this.idToProfileVersionMap.get(id);
			result.put("version", "" + profileVersion);
//			result.putAll(profileMap);
			result.put("status_code", "200");
			lock.unlockRead();
		}		
		return result;
	}
	
	public HashMap<String, String> updateProfileName(String email, String password, String newName){
		HashMap<String, String> validCredentials = this.validCredentials(email, password);
		HashMap<String, String> result = new HashMap<String, String>();
		if (validCredentials.containsKey("error")){
			result.put("error", validCredentials.get("error"));
		} else {
			String id = validCredentials.get("id");
			result.put("id", id);
			lock.lockWrite();
			Profile profile = this.idToProfileMap.get(id);
			profile.setName(newName);
			Integer profileVersion = this.idToProfileVersionMap.get(id) + 1;
			
			System.out.println("New profile version " + profileVersion);
			
			this.idToProfileVersionMap.put(id, profileVersion);
			result.put("version", "" + profileVersion);
			this.profileDatabaseVersion += 1;
			result.put("database_version", "" + profileDatabaseVersion);
			
			timeStamp += 1;
			ProfileLog profileLog = new ProfileLog();
			profileLog.setNewName(email, password, id, newName, "" + timeStamp);
			profileTransactionLog.add(profileLog.getUpdatedNameMap());
			this.lastProfileLogMap = profileLog.getUpdatedNameMap();
			lock.unlockWrite();
			result.put("status_code", "200");
		}	
		return result;
	}
	
	public HashMap<String, String> createNewAccount(String email, String password){
		HashMap<String, String> validCredentials = this.validCredentials(email, password);
		HashMap<String, String> result = new HashMap<String, String>();
		if (validCredentials.containsKey("error")){
			result.put("error", validCredentials.get("error"));
		} else {
			String id = validCredentials.get("id");
			result.put("id", id);
			lock.lockWrite();
			Profile profile = this.idToProfileMap.get(id);
			String accountId = profile.createAccount();
			result.put("account_id", accountId);
			Integer profileVersion = this.idToProfileVersionMap.get(id) + 1;
			this.idToProfileVersionMap.put(id, profileVersion);
			result.put("version", "" + profileVersion);
			this.profileDatabaseVersion += 1;
			result.put("database_version", "" + profileDatabaseVersion);

			timeStamp += 1;
			ProfileLog profileLog = new ProfileLog();
			profileLog.createdNewAccount(email, password, id, accountId, "" + timeStamp);
			profileTransactionLog.add(profileLog.getCreatedNewAccountMap());
			this.lastProfileLogMap = profileLog.getCreatedNewAccountMap();
			lock.unlockWrite();
			result.put("status_code", "201");
		}	
		return result;
	}
	
	public HashMap<String, String> createNewTransaction(String email, String password, String accountId, String type, double amount){
		HashMap<String, String> validCredentials = this.validCredentials(email, password);
		HashMap<String, String> result = new HashMap<String, String>();
		if (validCredentials.containsKey("error")){
			result.put("error", validCredentials.get("error"));
		} else {
			String id = validCredentials.get("id");
			result.put("id", id);
			lock.lockWrite();
			Profile profile = this.idToProfileMap.get(id);
			Account account = profile.getAccount(accountId);
			String transactionId = account.addTransaction(type, amount);			
			Integer profileVersion = this.idToProfileVersionMap.get(id) + 1;
			this.idToProfileVersionMap.put(id, profileVersion);
			result.put("version", "" + profileVersion);
			this.profileDatabaseVersion += 1;
			result.put("database_version", "" + profileDatabaseVersion);

			timeStamp += 1;
			ProfileLog profileLog = new ProfileLog();
			profileLog.didTransaction(email, password, id, accountId, transactionId, type, amount, "" + timeStamp);
			profileTransactionLog.add(profileLog.getDidTransactionMap());
			this.lastProfileLogMap = profileLog.getDidTransactionMap();
			lock.unlockWrite();
			result.put("status_code", "201");
		}	
		return result;
	}
	
	public HashMap<String, Object> getStatus(int emailPasswordVersion, int profileVersion){
		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put("email_password_version", emailPasswordDatabaseVersion);
		result.put("profile_verison", this.profileDatabaseVersion);
		result.put("message", "GET Correctly Formatted");
		result.put("secondaries", secondaries);
		boolean modified = false;
		System.out.println("Mine: " + this.emailPasswordDatabaseVersion + ". Incoming: " + emailPasswordVersion);
		if (this.emailPasswordDatabaseVersion != emailPasswordVersion){
			System.out.println("Send new email logs");
			modified = true;
			lock.lockRead();
//			ArrayList<HashMap<String, String>> subsetEmailPasswordLog = (ArrayList<HashMap<String, String>>) emailPasswordLog.subList(emailPasswordVersion, emailPasswordDatabaseVersion);
			ArrayList<HashMap<String, String>> subsetEmailPasswordLog = new ArrayList<HashMap<String, String>>();
			
			for (int i = emailPasswordVersion; i < emailPasswordLog.size(); i++){
				subsetEmailPasswordLog.add(emailPasswordLog.get(i));
			}
			
			lock.unlockRead();
			result.put("email_password_log", subsetEmailPasswordLog);
			System.out.println("Size = " + subsetEmailPasswordLog.size());
		}
		System.out.println("Mine: " + this.profileDatabaseVersion + ". Incoming: " + profileVersion);
		if(this.profileDatabaseVersion != profileVersion){
			System.out.println("Send new profile log");
			modified = true;
			lock.lockRead();
			ArrayList<HashMap<String, String>> subsetProfileLog = new ArrayList<HashMap<String, String>>();

			for (int i = profileVersion; i < profileTransactionLog.size(); i++){
				subsetProfileLog.add(profileTransactionLog.get(i));
			}
			
			lock.unlockRead();
			result.put("profile_transaction_log", subsetProfileLog);
			System.out.println("Size = " + subsetProfileLog.size());
		}
		
		if (modified){
			result.put("status_code", "200");
		} else {
			result.put("status_code", "304");
		}
		return result;
	}
	
	///////////////////////////////////////OLD STUFF, DELETE SOME OF IT
	
	/**
	 * Add tweet to database for every hashtag in hashtags
	 * Increment current version of tweets for each hashtag
	 * @param hashtags
	 * @param tweet
	 */
	public void addTweet(ArrayList<String> hashtags, String tweet){
		lock.lockWrite();
		
		HashMap<String, Object> addTweet = new HashMap<String, Object>();
		addTweet.put("hashtags", hashtags);
		addTweet.put("tweet", tweet);
		this.log.add(addTweet);
		
		for (String hashtag: hashtags){
//			System.out.println("hashtag: " + hashtag + " successfully added to database");
			
			if (tweetMap.containsKey(hashtag)){
				ArrayList<String> tweets = tweetMap.get(hashtag);
				tweets.add(tweet);
				versionMap.put(hashtag, versionMap.get(hashtag) + 1);
			} else {
				ArrayList<String> tweets = new ArrayList<String>();
				tweets.add(tweet);
				tweetMap.put(hashtag, tweets);
				versionMap.put(hashtag, 1);
			}
		}
		this.databaseVersion += 1;
		lock.unlockWrite();
	}
	
	/**
	 * Return all tweets with the corresponding hashtag
	 * @param hashtag
	 * @return
	 */
	public ArrayList<String> getTweet(String hashtag){
		lock.lockRead();
		ArrayList<String> tweets = null;
		if (tweetMap.containsKey(hashtag)){
			tweets = tweetMap.get(hashtag);
		}
		lock.unlockRead();
		return tweets;
	}
	
	/**
	 * Return current version of tweets for corresponding hashtag
	 * @param hashtag
	 * @return
	 */
	public Integer getHashTagVersion(String hashtag){
		lock.lockRead();
		Integer version = 0;
		if (versionMap.containsKey(hashtag)){
			version = versionMap.get(hashtag);
		}
		lock.unlockRead();
		return version;
	}
	
	public Integer getDatabaseVersion(){
		int version = 0;
		lock.lockRead();
		version = this.databaseVersion;
		lock.unlockRead();
		return version;
	}
	
	public ArrayList<HashMap<String, Object>> getLog(){
		ArrayList<HashMap<String, Object>> temp = new ArrayList<HashMap<String, Object>>();
		lock.lockRead();		
		for (HashMap<String, Object> addTweet: this.log){
			HashMap<String, Object> temp2 = new HashMap<String, Object>();
			temp2.put("tweet", addTweet.get("tweet"));
			temp2.put("hashtags", addTweet.get("hashtags"));
			temp.add(temp2);
		}
		lock.unlockRead();
		return temp;
	}
	
	public ArrayList<HashMap<String, Object>> getLog(Integer num){
		ArrayList<HashMap<String, Object>> temp = new ArrayList<HashMap<String, Object>>();
		lock.lockRead();	
		
		System.out.println("INTEGER: " + num);
		System.out.println("Log Size: " + log.size());
		
		for (int i = this.log.size() - 1; i >= 0; i--){
			System.out.println("INDEX I: " + i);
			HashMap<String, Object> addTweet = this.log.get(i);
			HashMap<String, Object> temp2 = new HashMap<String, Object>();
			temp2.put("tweet", addTweet.get("tweet"));
			temp2.put("hashtags", addTweet.get("hashtags"));
			temp.add(temp2);
		}
		lock.unlockRead();
		return temp;
	}
	
	public void setBackendInfo(String backendIP, int backEndPort){
		lock.lockWrite();
		this.backendIP = backendIP;
		this.backendPort = backEndPort;
		lock.unlockWrite();
	}
	
	public String getBackEndIP(){
		String IP;
		lock.lockRead();
		IP = this.backendIP;
		System.out.println("IP " + IP);
		lock.unlockRead();
		return IP;
	}
	
	public Integer getBackEndPort(){
		Integer port;
		lock.lockRead();
		port = this.backendPort;
		System.out.println("PORT " + port);
		lock.unlockRead();
		return port;
	}
	
	public void setBackEndDiscoveryReceivePort(int discoveryReceivePort){
		lock.lockWrite();
		this.discoveryReceivePort = discoveryReceivePort;
		lock.unlockWrite();
	}
	
	public int getDiscoveryReceivePort(){
		int port = 0;
		lock.lockRead();
		port = this.discoveryReceivePort;
		lock.unlockRead();
		return port;
	}
	
	public void setPrimaryInfo(String primaryIP, int primaryPort){
		lock.lockWrite();
		this.primaryIP = primaryIP;
		this.primaryPort = primaryPort;
		
		System.out.println("Saved data: " + this.primaryIP + " " + this.primaryPort);
		lock.unlockWrite();
	}
	
	public String getPrimaryIP(){
		String IP;
		lock.lockRead();
		IP = this.primaryIP;
		System.out.println("IP " + IP);
		lock.unlockRead();
		return IP;
	}
	
	public Integer getPrimaryPort(){
		Integer port;
		lock.lockRead();
		port = this.primaryPort;
		System.out.println("PORT " + port);
		lock.unlockRead();
		return port;
	}
	
	public void setDiscoveryInfo(String primaryIP2, int primaryPort2){
		lock.lockWrite();
		this.disoveryIP = primaryIP2;
		this.discoveryPort = primaryPort2;
		lock.unlockWrite();
	}
	
	public String getDiscoveryIP(){
		String IP;
		lock.lockRead();
		IP = this.disoveryIP;
		lock.unlockRead();
		return IP;
	}
	
	public Integer getDiscoveryPort(){
		Integer port;
		lock.lockRead();
		port = this.discoveryPort;
		lock.unlockRead();
		return port;
	}
	
	
	
	public void setStatus(String status){
		lock.lockWrite();
		this.status = status;
		lock.unlockWrite();
	}
	
	public String getStatus(){
		String tempStatus = "";
		lock.lockRead();
		tempStatus = this.status;
		lock.unlockRead();
		return tempStatus;
	}
	
	public void addSecondary(String ip, String port){
		lock.lockWrite();
		ArrayList<String> secondary = new ArrayList<String>();
		secondary.add(ip);
		secondary.add(port);
		
		boolean exist = false;
		
		if (ip.equals(backendIP) && port.equals(""+discoveryReceivePort)){
			exist = true;
			System.out.println("DO NOT ADD THIS, IT'S MY IP AND PORT!!!!");
		} else {
			for(ArrayList temp: this.secondaries){
				System.out.println("Secondary | IP: " + temp.get(0) + ", PORT: " + temp.get(1));
				System.out.println("Backend | IP: " + backendIP + ", PORT: " + discoveryReceivePort);
				if ( temp.get(0).equals(ip) && temp.get(1).equals(port)){
					exist = true;
					break;
				}
			}
		}
		
		if (!exist){
			this.secondaries.add(secondary);
		}
		lock.unlockWrite();
	}
	
	public ArrayList<ArrayList<String>> getSecondaries(){
		ArrayList<ArrayList<String>> secondary = new ArrayList<ArrayList<String>>();
		lock.lockRead();
		for(ArrayList<String> second: this.secondaries){
			ArrayList<String> s = new ArrayList<String>();
			s.add(second.get(0));
			s.add(second.get(1));
			secondary.add(s);
		}
		lock.unlockRead();
		return secondary;
	}
}
