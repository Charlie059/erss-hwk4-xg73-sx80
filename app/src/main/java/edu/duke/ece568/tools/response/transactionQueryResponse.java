package edu.duke.ece568.tools.response;

import edu.duke.ece568.tools.log.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class transactionQueryResponse {
    String response;

    public transactionQueryResponse(int trans_id, ResultSet result){
        response = "  <status id=\""+trans_id+"\">\n";
        try{
            while(result.next()){
                String status = result.getString("status");
                double amount = result.getDouble("amount");
                double price = result.getDouble("limit_price");
                long currtime = result.getLong("Time");
                if (status.equals("OPEN")){
                    response += "    <open shares="+amount+"/>\n";
                }
                if (status == "CANCELLED"){
                    response += "    <canceled shares="+amount+" time=  />\n";
                }
                if (status == "EXECUTED"){
                    response += "    <executed shares="+amount+" price="+price+" time=  />\n";
                }
            }
            response += "  </status>\n";
        }catch (SQLException e){
            Logger.getSingleton().write(e.getMessage());
        }
    }

    public String getResponse(){
        return response;
    }
}
