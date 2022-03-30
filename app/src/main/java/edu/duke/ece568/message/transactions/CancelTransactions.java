package edu.duke.ece568.message.transactions;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;

public class CancelTransactions extends Transactions{
    int transactionId;

    void changeOrder(){

    }

    @Override
    public boolean execute() {
        String result = PostgreSQLJDBC.getInstance().processTransactionCancel(AccountId, transactionId);
        if (result == null){
            return true;
        }
        else{
            return false;
        }

    }
}
