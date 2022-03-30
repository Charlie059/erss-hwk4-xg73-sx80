package edu.duke.ece568.tools.response;

public class createPositionResponse {
    String response;
    //
    public createPositionResponse(int accountId, String symbol){
        this.response = "  <created sym=\""+symbol+"\" id=\""+ accountId + "\"/>\n";
    }

    public String getResponse(){
        return response;
    }
}
