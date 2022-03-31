package edu.duke.ece568.message.transactions;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;

public class OrderTransactions extends Transactions{
    String symbolName;
    int symbolAmount;
    int priceLimit;
    int tran_id;


    public OrderTransactions(int accountId, String symbolName, int symbolAmount, int priceLimit, int tran_id) {
        this.AccountId = accountId;
        this.symbolName = symbolName;
        this.symbolAmount = symbolAmount;
        this.priceLimit = priceLimit;
        this.tran_id = tran_id;
    }



    @Override
    public boolean execute() {
        String result = PostgreSQLJDBC.getInstance().processTransactionOrder(AccountId, symbolName, symbolAmount, priceLimit, tran_id);
        if (result == null){
            return true;
        }
        else{
            return false;
        }

    }
}
