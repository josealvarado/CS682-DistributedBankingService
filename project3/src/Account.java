import java.util.ArrayList;
import java.util.HashMap;

public class Account {

	/*
	 * Private variables
	 */
	private String id;
	private double balance;
	private ArrayList<Transaction> transactionList;
	
	/*
	 * Constructor
	 */
	public Account(String id){
		this.id = id;
		balance = 0.0;
		transactionList = new ArrayList<Transaction>();
	}
	
	/*
	 * Mutator methods
	 */
	public void setBalance(double balance){
		this.balance = balance;
	}
	
	public String addTransaction(String type, double amount){
		String transactionId = "" + transactionList.size();
		Transaction transaction = new Transaction(transactionId, type, amount);
		transactionList.add(transaction);
		if (type.equals("deposit")){
			balance += amount;
		} else{
			balance -= amount;
		}
		return transactionId;
	}
	
	/*
	 * Accessor methods
	 */
	public String getId(){
		return id;
	}
	
	public double getBalance(){
		return balance;
	}
	
	public ArrayList<Transaction> getTransactionList(){
		return transactionList;
	}
	
	public HashMap<String, String> getAccountMap(){
		HashMap<String, String> accountMap = new HashMap<String, String>();
		accountMap.put("id", id);
		accountMap.put("balance", "" + balance);
		return accountMap;
	}
	
}