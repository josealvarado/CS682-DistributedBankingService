import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

public class Profile {
	
	/*
	 * Private variables
	 */
	private String name;
	private long lastUpdated;
	private Date dateCreated;
	private boolean status;
	private HashMap<String, Account> idToAccountMap;
	
	/**
	 * Constructor
	 * @param name
	 */
	public Profile(String name){
		this.name = name;
		lastUpdated = System.currentTimeMillis();
		dateCreated = new Date();
		status = false;
		idToAccountMap = new HashMap<String, Account>();
	}
		
	/*
	 * Mutator methods
	 */
	public void setName(String name){
		lastUpdated = System.currentTimeMillis();
		this.name = name;
	}
	
	public void setStatus(boolean status){
		lastUpdated = System.currentTimeMillis();
		this.status = status;
	}
	
	public String createAccount(){
		lastUpdated = System.currentTimeMillis();
		String accountId = "" + idToAccountMap.size();
		Account account = new Account(accountId);
		idToAccountMap.put(accountId, account);
		return accountId;
	}
		
	/*
	 * Accessor methods
	 */
	public String getName(){
		return name;
	}
	
	public long getLastUpdated(){
		return lastUpdated;
	}
	
	public Date getDateCreated(){
		return dateCreated;
	}
	
	public boolean getStatus(){
		return status;
	}
	
	public Account getAccount(String id){
		if (idToAccountMap.containsKey(id)){
			return idToAccountMap.get(id);
		} else {
			return null;
		}
	}
	
	public HashMap<String, Object> getProfileMap(){
		HashMap<String, Object> profileMap = new HashMap<String, Object>();
		profileMap.put("name", name);
		profileMap.put("date_created", "" + dateCreated);
		profileMap.put("date_modified", "" + lastUpdated);
		ArrayList<HashMap<String, String>> accountList = new ArrayList<HashMap<String, String>>();
		for (Entry<String, Account> entry : idToAccountMap.entrySet()) {
//			String accountId = entry.getKey();
		    Account account = entry.getValue();
		    HashMap<String, String> accountMap = account.getAccountMap();
//		    HashMap<String, Object> accountIdToAccount = new HashMap<String, Object>();
//		    accountIdToAccount.put(accountId, accountMap);
		    accountList.add(accountMap);
		}
		profileMap.put("accounts", accountList);
		return profileMap;
	}
	
}