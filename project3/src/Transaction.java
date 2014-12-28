public class Transaction {

	/*
	 * Private variables
	 */
	private String id;
	private String type;
	private double amount;
	
	/**
	 * Constructor
	 * @param id - 
	 * @param type - 
	 * @param amount
	 */
	public Transaction(String id, String type, double amount){
		this.id = id;
		this.type = type;
		this.amount = amount;
	}
	
	/*
	 * Accessor methods
	 */
	public String getId(){
		return id;
	}
	
	public String getType(){
		return type;
	}
	
	public double getAmount(){
		return amount;
	}
	
}