package edu.duke.ece568.message.create;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;

import java.util.Objects;

public class AccountCreateAction extends CreateAction{
    double balance;

    public AccountCreateAction(int accountId, double balance){
        this.accountId = accountId;
        this.balance = balance;

    }
    //TODO: add new element to DATABASE Account
    @Override
    public boolean execute(){
        String result = PostgreSQLJDBC.getInstance().createAccount(accountId, balance);
        if (result == null){
            return true;
        }
        else{
            return false;
        }
    }

}
