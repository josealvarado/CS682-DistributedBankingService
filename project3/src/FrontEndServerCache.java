import java.util.ArrayList;
import java.util.HashMap;

public class FrontEndServerCache {
	
	/**
	 * FrontEndServerCache variables
	 */
	private HashMap<String, HashMap<String, String>> profileMap;
	private HashMap<String, Integer> versionMap;
	
	private MultiReaderLock lock = new MultiReaderLock();
	
	/**
	 * Default constructor
	 */
	public FrontEndServerCache(){
		profileMap = new HashMap<String, HashMap<String, String>>();
		versionMap = new HashMap<String, Integer>();
	}
	
	public void saveVersion(String email, int newVersion, HashMap<String, String> response){
		lock.lockWrite();
		this.versionMap.put(email, newVersion);
		this.profileMap.put(email, response);
		lock.unlockWrite();
	}
	
	public int getVersion(String email){
		int version = 0;
		lock.lockWrite();
		if (this.versionMap.containsKey(email)){
			version = this.versionMap.get(email);
		} else {
			version = -1;
		}
		lock.unlockWrite();
		return version;
	}
	
	public HashMap<String, String> getData(String email){
		HashMap<String, String> data;
		 lock.lockRead();
		 data = profileMap.get(email);
		 lock.unlockRead();
		 return data;
	}
}
