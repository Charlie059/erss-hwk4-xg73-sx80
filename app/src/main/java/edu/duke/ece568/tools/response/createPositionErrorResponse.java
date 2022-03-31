package edu.duke.ece568.tools.response;

public class createPositionErrorResponse {
    String response;
    //
    public createPositionErrorResponse(int accountId, String symbol, String errorMessage){
        this.response = "  <created sym=\""+symbol+"\" id=\""+ accountId + "\">"+errorMessage+"</error>\n";
    }

    public String getResponse(){
        return response;
    }
}
