package edu.duke.ece568.tools.response;

public class createAccountErrorResponse {
    String response;
    //
    public createAccountErrorResponse(int accountId){
        this.response = "  <error id=\""+ accountId + "\">Account already exists</error>\n";
    }

    public String getResponse(){
        return response;
    }
}
