package edu.duke.ece568.tools.response;

public class transactionErrorForQueryAndCancelResponse {
    String response;
    //
    public transactionErrorForQueryAndCancelResponse(String errorMessage){
        this.response = "  <error>"+errorMessage+"</error>\n";
    }

    public String getResponse(){
        return response;
    }
}
