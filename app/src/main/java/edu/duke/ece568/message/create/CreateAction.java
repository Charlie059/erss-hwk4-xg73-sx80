package edu.duke.ece568.message.create;

import edu.duke.ece568.message.Message;
import edu.duke.ece568.tools.database.PostgreSQLJDBC;


public abstract class CreateAction implements Message {
    public int getAccountId() {
        return accountId;
    }

    int accountId;

    //TODO: add field DATABASE
    public abstract String execute();


}
