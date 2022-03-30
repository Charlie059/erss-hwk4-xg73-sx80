package edu.duke.ece568.tools.response;

public class createAccountResponse {
    String response;
    //
    public createAccountResponse(int accountId){
        this.response = "  <created id=\""+ accountId + "\"/>\n";
    }

    public String getResponse(){
        return response;
    }

}
