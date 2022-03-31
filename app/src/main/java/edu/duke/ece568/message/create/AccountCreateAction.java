package edu.duke.ece568.message.create;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;

public class AccountCreateAction extends CreateAction{
    double balance;

    public AccountCreateAction(int accountId, double balance){
        this.accountId = accountId;
        this.balance = balance;

    }
    //TODO: add new element to DATABASE Account
    @Override
    public String execute(){
        String result = PostgreSQLJDBC.getInstance().createAccount(accountId, balance);
        return result;
    }

}
