package edu.duke.ece568.message.transactions;

public class OrderTransactions extends Transactions{
    String symbolName;
    int symbolAmount;
    int priceLimit;

    void changeOrder(){
        //TODO: including add, execute(change Table Order)
    }

    @Override
    public boolean execute() {
        String result = PostgreSQLJDBC.getInstance().processTransactionOrder(AccountId, symbolName, symbolAmount, priceLimit);
        if (result == null){
            return true;
        }
        else{
            return false;
        }

    }
}
