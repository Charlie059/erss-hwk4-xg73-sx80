package edu.duke.ece568.message.transactions;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;

public class CancelTransactions extends Transactions{
    public int getTransactionId() {
        return transactionId;
    }

    int transactionId;

    public CancelTransactions(int accountId, int transactionId) {
        this.AccountId = accountId;
        this.transactionId = transactionId;
    }

    @Override
    public String execute() {
        String result = PostgreSQLJDBC.getInstance().processTransactionCancel(AccountId, transactionId);
        return result;
    }
}
