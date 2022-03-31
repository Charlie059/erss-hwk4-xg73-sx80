package edu.duke.ece568.message.create;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;

public class SymbolCreateAction extends CreateAction{
    String symbolName;
    double symbolAmount;

    public SymbolCreateAction(int accountId, String symbolName, double symbolAmount){
        this.accountId = accountId;
        this.symbolName = symbolName;
        this.symbolAmount = symbolAmount;
    }
    //TODO: add new element to DATABASE Symbol
    @Override
    public String execute(){
        String result = PostgreSQLJDBC.getInstance().createPosition(symbolName, symbolAmount, accountId);
        return result;
    }
}
