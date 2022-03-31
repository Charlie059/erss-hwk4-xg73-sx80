package edu.duke.ece568.message.transactions;

import edu.duke.ece568.message.Message;

public abstract class Transactions implements Message {
    public int getAccountId() {
        return AccountId;
    }

    int AccountId;
    //TODO: add field DATABASE
    public abstract String execute();
}
