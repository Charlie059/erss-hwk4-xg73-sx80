package edu.duke.ece568.tools.response;

public class createAccountErrorResponse {
    String response;
    //
    public createAccountErrorResponse(int accountId, String errorMessage){
        this.response = "  <error id=\""+ accountId + "\">"+errorMessage+"</error>\n";
    }

    public String getResponse(){
        return response;
    }
}
