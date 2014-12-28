import java.util.HashMap;


public class ProfileLog {

	/*
	 * Private variables
	 */
	private String id;
	private String newName;
	private boolean modifiedName;
	private boolean didTransaction;
	private String transactionId;
	private String type;
	private double amount;
	private String accountId;
	private boolean createdNewAccount;
	private boolean createdProfile;
	
	private String timeStamp;
	private String prepareTimeStamp;
	
	private String email;
	private String password;
	private String name;
	
	/**
	 * Constructor
	 * @param name
	 */
	public ProfileLog(){
		modifiedName = false;
		createdNewAccount = false;
		didTransaction = false;
		createdProfile = false;
	}
	
	/*
	 * Mutator methods
	 */
	public void setId(String id){
		this.id = id;
	}
	
	public void setNewName(String email, String password, String profileId, String newName, String timeStamp){
		modifiedName = true;
		this.id = profileId;
		this.email = email;
		this.password = password;
		this.timeStamp = timeStamp;
		this.newName = newName;
	}
	
	public void createdNewAccount(String email, String password, String id, String accountId, String timeStamp){
		createdNewAccount = true;
		this.id = id;
		this.email = email;
		this.password = password;
		this.timeStamp = timeStamp;
		this.accountId = accountId;
	}
	
	public void didTransaction(String email, String password, String id, String accountId, String transactionId, String type, double amount, String timeStamp){
		didTransaction = true;
		this.id = id;
		this.email = email;
		this.password = password;
		this.accountId = accountId;
		this.transactionId = transactionId;
		this.type = type;
		this.amount = amount;
		this.timeStamp = timeStamp;
	}
	
	public void createdProfile(String id, String email, String password, String name, String timeStamp){
		this.id = id;
		this.email = email;
		this.password = password;
		this.name = name;
		this.timeStamp = timeStamp;
	}
	
	public void prepareProfile(String email, String password, String name, String prepareTimeStamp){
		this.email = email;
		this.password = password;
		this.name = name;
		this.prepareTimeStamp = prepareTimeStamp;
	}
	
	public void prepareNewName(String email, String password, String newName, String prepareTimeStamp){
		this.email = email;
		this.password = password;
		this.prepareTimeStamp = prepareTimeStamp;
		this.newName = newName;
	}
	
	public void prepareNewAccount(String email, String password, String prepareTimeStamp){
		createdNewAccount = true;
		this.email = email;
		this.password = password;
		this.prepareTimeStamp = prepareTimeStamp;
	}
	
	public void prepareNewTransactoin(String email, String password, String accountId, String type, double amount, String prepareTimeStamp){
		didTransaction = true;
		this.email = email;
		this.password = password;
		this.accountId = accountId;
		this.type = type;
		this.amount = amount;
		this.prepareTimeStamp = prepareTimeStamp;
	}
	/*
	 * Accessor methods
	 */
	
	public boolean didModifyName(){
		return modifiedName;
	}
	
	public String getModifiedName(){
		return newName;
	}
	
	public boolean didCreateNewAccount(){
		return createdNewAccount;
	}

	public boolean getDidTransaction(){
		return didTransaction;
	}
	
	public String getAccountId(){
		return accountId;
	}
	
	public String getProfileId(){
		return id;
	}
	
	public String getEmail(){
		return email;
	}
	
	public String getPassword(){
		return password;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean didCreateProfile(){
		return createdProfile;
	}
	
	public HashMap<String, String> getEmailPasswordMap(){
		HashMap<String, String> emailPasswordMap = new HashMap<String, String>();
		emailPasswordMap.put("id", id);
		emailPasswordMap.put("email", email);
		emailPasswordMap.put("password", password);
		emailPasswordMap.put("time_stamp", timeStamp);
		return emailPasswordMap;
	}
	
	public HashMap<String, String> getPrepareProfileMap(){
		HashMap<String, String> prepareProfileMap = new HashMap<String, String>();
		prepareProfileMap.put("email", email);
		prepareProfileMap.put("password", password);
		prepareProfileMap.put("name", name);
		prepareProfileMap.put("prepare_time_stamp", prepareTimeStamp);
		return prepareProfileMap;
	}
	
	public HashMap<String, String> getUpdatedNameMap(){
		HashMap<String, String> updateNameMap = new HashMap<String, String>();
		updateNameMap.put("new_name", newName);
		updateNameMap.put("updated_name", "True");
		updateNameMap.put("id", id);
		updateNameMap.put("time_stamp", timeStamp);
		updateNameMap.put("email", email);
		updateNameMap.put("password", password);
		return updateNameMap;
	}
	
	public HashMap<String, String> getPrepareUpdateNameProfileMap(){
		HashMap<String, String> prepareProfileMap = new HashMap<String, String>();
		prepareProfileMap.put("new_name", newName);
		prepareProfileMap.put("updated_name", "True");
		prepareProfileMap.put("prepare_time_stamp", prepareTimeStamp);
		prepareProfileMap.put("email", email);
		prepareProfileMap.put("password", password);
		return prepareProfileMap;
	}
	
	
	public HashMap<String, String> getCreatedNewAccountMap(){
		HashMap<String, String> newAccountMap = new HashMap<String, String>();
		newAccountMap.put("created_account", "True");
		newAccountMap.put("account_id", accountId);
		newAccountMap.put("id", id);
		newAccountMap.put("time_stamp", timeStamp);
		newAccountMap.put("email", email);
		newAccountMap.put("password", password);
		return newAccountMap;
	}
	
	public HashMap<String, String> getPrepareNewAccountMap(){
		HashMap<String, String> newAccountMap = new HashMap<String, String>();
		newAccountMap.put("created_account", "True");
		newAccountMap.put("account_id", accountId);
		newAccountMap.put("prepare_time_stamp", prepareTimeStamp);
		newAccountMap.put("email", email);
		newAccountMap.put("password", password);
		return newAccountMap;
	}
	
	public HashMap<String, String> getDidTransactionMap(){
		HashMap<String, String> didTransactionMap = new HashMap<String, String>();
		didTransactionMap.put("did_transaction", "True");
		didTransactionMap.put("id", id);
		didTransactionMap.put("account_id", accountId);
		didTransactionMap.put("transaction_id", transactionId);
		didTransactionMap.put("type", type);
		didTransactionMap.put("amount", "" + amount);
		didTransactionMap.put("time_stamp", timeStamp);
		didTransactionMap.put("email", email);
		didTransactionMap.put("password", password);
		return didTransactionMap;
	}
	
	public HashMap<String, String> getPrepareTransactionMap(){
		HashMap<String, String> didTransactionMap = new HashMap<String, String>();
		didTransactionMap.put("did_transaction", "True");
		didTransactionMap.put("account_id", accountId);
		didTransactionMap.put("type", type);
		didTransactionMap.put("amount", "" + amount);
		didTransactionMap.put("prepare_time_stamp", prepareTimeStamp);
		didTransactionMap.put("email", email);
		didTransactionMap.put("password", password);
		return didTransactionMap;
	}
}
