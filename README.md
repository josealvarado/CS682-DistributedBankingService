josealvarado-project3
=====================

project3 created for josealvarado

Necessary Commands

export CLASSPATH=bin

export CLASSPATH=lib/commons-codec-1.9.jar:lib/json-simple-1.1.1.jar:lib/log4j-1.2.17.jar:bin

Project 3 Proposal

I want to make a strongly connected banking system which is basically an extension of my project 2. I will be able to launch multiple banking systems such as Chase, Bank of America, and Wells Fargo. Every banking system will allow users to create an account, login to account, and change password. Users would be able to deposit and withdraw cash. They would also be able to deposit and cash checks. Users would also be able to view their statement which would show all their transactions. Users would also be allowed to transfer money between their accounts. Users would also be able to register for a credit card, make purchases on that credit card, and make payments to that credit card.

I would also like to expand it to allow users to make a wire transfer if they own accounts in different banks. The users would also be able to check their inbox which would contian notifications from the bank. I also want to be able to charge the users a fee if they don't make payments on time. I would also like the users to receive a credit report from a central system that would contact all the banks and ask for their credit information. I would also like to add a feature where the bank would freeze an account if it suspects fradulent activity.

The general idea of this project is fine. What is unclear from your proposal, however, is which features will be required in your backend architecture.

Ordering of the transactions
Implement strict 2 phase locking to enable transactions (e.g., a transfer consisting of two operations, deposit and withdrawal).


Please sketch out your implementation.

What kinds of data structures will be required?

Bank

  String
    Name

  HashMap
    email address
    ID
  
  HashMap
    ID
    password
    
  HashMap
    ID
    Profile
      Name
      Last Updated
      Date Created
      Status -> if the user is logged in or not
      HashMap -> Id to Account
        Account
          Account #
          Balance
          ArrayList
            Transaction
              ID #
              Type -> withdrawl, deposit
              Amount
      // Later
      ArrayList
        Credit Card
          CC ID #
          Name
          Digits
          Expiration Date 
          Balance
          ArrayList
            Transaction
              ID
              Type -> withdrawl, deposit
              Amount
      
  HashMap
    ID
    ArrayList
      Mail
        Message
        Status -> whether or not the email has been read
        
What will be required to ensure consistency in your system (transactions, for example)?
  The primary will timestamp each transaction. The secondaries will queue the transaction, order them, and then process them to make sure the order of the transactions are preserved. 

How will you mimic different banks?
  I would use the name to differentiate the banks, where the front end will map the bank name to the IP address of the corresponding bank

What is your user interface going to look like?
  I was thinking of just using curls to test the functionality of the project instead of focusing on the UI. If I finish the project in time, I'd develop a basic UI. 
