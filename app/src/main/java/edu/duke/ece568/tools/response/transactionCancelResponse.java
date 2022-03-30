package edu.duke.ece568.tools.response;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;
import edu.duke.ece568.tools.log.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public class transactionCancelResponse {

    String response;

    public transactionCancelResponse(int trans_id, int accountId){

        ResultSet result = PostgreSQLJDBC.getInstance().processTransactionQuery(accountId, trans_id);
        response = "  <canceled id=\""+trans_id+"\">\n";
        try{
            while(result.next()){
                String status = result.getString("Status");
                double amount = result.getDouble("Amount");
                double price = result.getDouble("Limit_price");

                if (status == "CANCELLED"){
                    response += "    <canceled shares="+amount+" time=  />\n";
                }
                if (status == "EXECUTED"){
                    response += "    <executed shares="+amount+" price="+price+" time=  />\n";
                }
            }
            response += "  </canceled>\n";
        }catch (SQLException e){
            Logger.getSingleton().write(e.getMessage());
        }
    }

    public String getResponse(){
        return response;
    }
}
