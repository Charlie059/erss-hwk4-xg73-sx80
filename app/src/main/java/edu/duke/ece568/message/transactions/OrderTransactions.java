package edu.duke.ece568.message.transactions;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;

public class OrderTransactions extends Transactions{
    public String getSymbolName() {
        return symbolName;
    }

    public double getSymbolAmount() {
        return symbolAmount;
    }

    public double getPriceLimit() {
        return priceLimit;
    }

    public int getTran_id() {
        return tran_id;
    }

    String symbolName;
    double symbolAmount;
    double priceLimit;
    int tran_id;


    public OrderTransactions(int accountId, String symbolName, double symbolAmount, double priceLimit, int tran_id) {
        this.AccountId = accountId;
        this.symbolName = symbolName;
        this.symbolAmount = symbolAmount;
        this.priceLimit = priceLimit;
        this.tran_id = tran_id;
    }



    @Override
    public String execute() {
        String result = PostgreSQLJDBC.getInstance().processTransactionOrder(AccountId, symbolName, symbolAmount, priceLimit, tran_id);
        return result;
    }
}
