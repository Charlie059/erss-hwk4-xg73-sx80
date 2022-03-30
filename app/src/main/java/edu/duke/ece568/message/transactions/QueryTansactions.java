package edu.duke.ece568.message.transactions;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;
import edu.duke.ece568.tools.log.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class QueryTansactions extends Transactions{
    int transactionId;

    public QueryTansactions(int accountId, int transactionId){
        this.AccountId = accountId;
        this.transactionId = transactionId;
    }

    @Override
    public boolean execute() {
        ResultSet result = PostgreSQLJDBC.getInstance().processTransactionQuery(AccountId, transactionId);
        try{
            while(result.next());
        }catch(SQLException e){
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
        }


        return false;
    }

    //@Override

}
