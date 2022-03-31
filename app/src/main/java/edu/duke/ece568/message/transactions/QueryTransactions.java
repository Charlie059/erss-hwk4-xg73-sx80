package edu.duke.ece568.message.transactions;

public class QueryTransactions extends Transactions{
    int transactionId;

    public int getTransactionId() {
        return transactionId;
    }

    public QueryTransactions(int accountId, int transactionId){
        this.AccountId = accountId;
        this.transactionId = transactionId;
    }

    public String execute() {
//        ResultSet result = PostgreSQLJDBC.getInstance().processTransactionQuery(AccountId, transactionId);
//        try{
//            while(result.next());
//        }catch(SQLException e){
//            Logger logger = Logger.getSingleton();
//            logger.write(e.getMessage());
//        }
//
//
//        return false;
        return null;
    }

    //@Override

}
