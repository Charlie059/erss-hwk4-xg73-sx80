package edu.duke.ece568.tools.response;

public class transactionErrorResponse {
    String response;
    //
    public transactionErrorResponse(String symbol, double amount, double price_limit, String errorMessage){
        this.response = "  <error sym=\""+ symbol + "\" amount=\""+amount+"\" limit=\""+price_limit+"\">"+errorMessage+"</error>\n";
    }

    public String getResponse(){
        return response;
    }
    //TODO:1. 逻辑判断：操作是否成功执行（null）
    //TODO:2. 如果null（成功），不含Error对应的response
    //TODO:3. 如果不是null（失败），含Error对应的response， 函数返回的String就是errorMessage

    //create Account
    //TODO:1.createAccount: 成功（null）， new createAccountResponse()
    //TODO:2.createAccount: 失败（String）， new createAccountErrorResponse()

    //create Position
    //TODO:1.createPosition: 成功（null）， new createPositionResponse()
    //TODO:2.createPosition: 失败（String）， new createPositionErrorResponse()


    //transactions Order
    //TODO: 需要创造 int tran_id = TransactionCounter.getInstance().getCurrent_id();/**********
    //TODO:1.Order: 成功（null）， new transactionOrderResponse()
    //TODO:2.order: 失败（String）， new transactionErrorResponse() 函数返回的String就是errorMessage

    //transactions Cancel
    //TODO:1.Cancel: 成功（null）， new transactionCancelResponse()
    //TODO:2.Cancel: 失败（String）， new transactionErrorForQueryAndCancelResponse() 函数返回的String就是errorMessage


    //transactions Query
    //TODO: important: first: DB : checkTransactionQuery():/*************
    //成功
    //TODO: null: processTransactionQuery()
    //TODO:1.Query: 成功（null）， new transactionQueryResponse()

    //失败
    //TODO: String:
    //TODO:1.Query: 失败（String）， new transactionErrorForQueryAndCancelResponse()
}
