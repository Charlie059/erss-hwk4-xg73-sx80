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
}
