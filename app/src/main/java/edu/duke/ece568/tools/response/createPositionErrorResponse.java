package edu.duke.ece568.tools.response;

public class createPositionErrorResponse {
    String response;
    //
    public createPositionErrorResponse(int accountId, String symbol){
        this.response = "  <created sym=\""+symbol+"\" id=\""+ accountId + "\">AccountId is invalid</error>\n";
    }

    public String getResponse(){
        return response;
    }
}