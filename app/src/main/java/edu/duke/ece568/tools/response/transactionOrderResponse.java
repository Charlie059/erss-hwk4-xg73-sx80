package edu.duke.ece568.tools.response;

public class transactionOrderResponse {
    String response;
    //
    public transactionOrderResponse(String symbol, double amount, double price_limit, int trans_id){
        this.response = "  <opened sym=\""+ symbol + "\" amount=\""+amount+"\" limit=\""+price_limit+"\" id=\"" + trans_id+"\"/>\n";
    }

    public String getResponse(){
        return response;
    }
}
