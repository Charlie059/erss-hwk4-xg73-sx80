package edu.duke.ece568.message.transactions;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;

public class CancelTransactions extends Transactions{
    int transactionId;

    public CancelTransactions(int accountId, int transactionId) {
        this.AccountId = accountId;
        this.transactionId = transactionId;
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
