package edu.duke.ece568.tools.parser;

import edu.duke.ece568.counter.TransactionCounter;
import edu.duke.ece568.message.create.AccountCreateAction;
import edu.duke.ece568.message.create.SymbolCreateAction;
import edu.duke.ece568.message.transactions.CancelTransactions;
import edu.duke.ece568.message.transactions.OrderTransactions;
import edu.duke.ece568.message.transactions.QueryTransactions;
import edu.duke.ece568.message.transactions.Transactions;
import edu.duke.ece568.tools.database.PostgreSQLJDBC;
import edu.duke.ece568.tools.response.*;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class TransactionParser extends Parser{
    ArrayList<Transactions> transactions;

    public TransactionParser(String xml) throws ParserConfigurationException, IOException, SAXException {
        super(xml);
        this.transactions = new ArrayList<>();
    }

    @Override
    public Document parse() throws IOException, SAXException {
        Document document = super.parse();

        NodeList nodeList = document.getDocumentElement().getChildNodes();

        // Get TransactionID
        NamedNodeMap transactionAttrs = document.getDocumentElement().getAttributes();
        String accountID = transactionAttrs.item(0).getNodeValue();

        // Get the children of transactions
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                System.out.println(nodeList.item(i).getNodeName());

                // If <order> node
                if(nodeList.item(i).getNodeName().equals("order")){
                    // get order attr
                    NamedNodeMap order_Attrs = nodeList.item(i).getAttributes();
                    String sym = "", amount = "", limit = "";

                    // get the attr of order
                    for(int j = 0; j < order_Attrs.getLength(); j++){
                        if(order_Attrs.item(j).getNodeName().equals("sym")) sym = order_Attrs.item(j).getNodeValue();
                        else if(order_Attrs.item(j).getNodeName().equals("amount")) amount = order_Attrs.item(j).getNodeValue();
                        else if(order_Attrs.item(j).getNodeName().equals("limit")) limit = order_Attrs.item(j).getNodeValue();
                    }

                    // Create new transactionID
                    int tran_id = TransactionCounter.getInstance().getCurrent_id();
                    this.transactions.add(new OrderTransactions(Integer.parseInt(accountID), sym, Double.parseDouble(amount), Double.parseDouble(limit),tran_id));
                }
                // If <query> node
                else if(nodeList.item(i).getNodeName().equals("query")){
                    // get order attr
                    NamedNodeMap order_Attrs = nodeList.item(i).getAttributes();
                    String id = "";

                    // get the attr of order
                    for(int j = 0; j < order_Attrs.getLength(); j++){
                        if(order_Attrs.item(j).getNodeName().equals("id")) id = order_Attrs.item(j).getNodeValue();
                    }
                    this.transactions.add(new QueryTransactions(Integer.parseInt(accountID),Integer.parseInt(id)));
                }
                // If <cancel> node
                else if(nodeList.item(i).getNodeName().equals("cancel")){
                    // get order attr
                    NamedNodeMap order_Attrs = nodeList.item(i).getAttributes();
                    String id = "";

                    // get the attr of order
                    for(int j = 0; j < order_Attrs.getLength(); j++){
                        if(order_Attrs.item(j).getNodeName().equals("id")) id = order_Attrs.item(j).getNodeValue();
                    }
                    this.transactions.add(new CancelTransactions(Integer.parseInt(accountID),Integer.parseInt(id)));
                }
            }
        }
        return document;
    }

    @Override
    public String run(){
        StringBuilder ans = new StringBuilder("<results>\n");

        for (int i = 0; i < this.transactions.size(); i++) {
            // Execute action
            String exeResult = this.transactions.get(i).execute();

            // If execute success
            if(exeResult == null){
                // If we get OrderTransactions
                if(this.transactions.get(i).getClass() == OrderTransactions.class){
                    // Transfer Transactions to OrderTransactions
                    OrderTransactions orderTransactions = (OrderTransactions) this.transactions.get(i);
                    transactionOrderResponse createAccountResponse = new transactionOrderResponse(orderTransactions.getSymbolName(), orderTransactions.getSymbolAmount(), orderTransactions.getPriceLimit(), orderTransactions.getTran_id());
                    ans.append(createAccountResponse.getResponse());
                }
                // If we get QueryTransactions
                else if(this.transactions.get(i).getClass() == QueryTransactions.class){
                    // Transfer Transactions to QueryTransactions
                    QueryTransactions queryTransactions = (QueryTransactions) this.transactions.get(i);

                    // pre-check
                    String checkRes =  PostgreSQLJDBC.getInstance().checkTransactionQuery(queryTransactions.getAccountId(), queryTransactions.getTransactionId());

                    // if pre-check fail
                    if(checkRes != null){
                        transactionErrorForQueryAndCancelResponse transactionErrorForQueryAndCancelResponse = new transactionErrorForQueryAndCancelResponse(checkRes);
                        ans.append(transactionErrorForQueryAndCancelResponse.getResponse());
                    }
                    // if pre-check success
                    else{
                        // Get the query result
                        ResultSet result = PostgreSQLJDBC.getInstance().processTransactionQuery(queryTransactions.getAccountId(), queryTransactions.getTransactionId());
                        transactionQueryResponse transactionQueryResponse = new transactionQueryResponse(queryTransactions.getTransactionId(), result);
                        ans.append(transactionQueryResponse.getResponse());

                    }
                }
                // If we get CancelTransactions
                else if(this.transactions.get(i).getClass() == CancelTransactions.class){
                    // Transfer Transactions to CancelTransactions
                    CancelTransactions cancelTransactions = (CancelTransactions) this.transactions.get(i);
                    transactionCancelResponse transactionCancelResponse = new transactionCancelResponse(cancelTransactions.getTransactionId(), cancelTransactions.getAccountId());
                    ans.append(transactionCancelResponse.getResponse());
                }
            }
            // Else if execute fail
            else{
                // If we get OrderTransactions
                if(this.transactions.get(i).getClass() == OrderTransactions.class){
                    // Transfer Transactions to OrderTransactions
                    OrderTransactions orderTransactions = (OrderTransactions) this.transactions.get(i);
                    transactionErrorResponse transactionErrorResponse = new transactionErrorResponse(orderTransactions.getSymbolName(), orderTransactions.getSymbolAmount(), orderTransactions.getPriceLimit(), exeResult);
                    ans.append(transactionErrorResponse.getResponse());
                }
                // If we get QueryTransactions
                else if(this.transactions.get(i).getClass() == QueryTransactions.class){
//                    // Transfer Transactions to QueryTransactions
//                    QueryTransactions queryTransactions = (QueryTransactions) this.transactions.get(i);
                    transactionErrorForQueryAndCancelResponse transactionErrorForQueryAndCancelResponse = new transactionErrorForQueryAndCancelResponse(exeResult);
                    ans.append(transactionErrorForQueryAndCancelResponse.getResponse());
                }
                // If we get CancelTransactions
                else if(this.transactions.get(i).getClass() == CancelTransactions.class){
//                    // Transfer Transactions to CancelTransactions
//                    CancelTransactions cancelTransactions = (CancelTransactions) this.transactions.get(i);
                    transactionErrorForQueryAndCancelResponse transactionErrorForQueryAndCancelResponse = new transactionErrorForQueryAndCancelResponse(exeResult);
                    ans.append(transactionErrorForQueryAndCancelResponse.getResponse());
                }
            }
        }
        ans.append("</results>\n");
        return ans.toString();
    }
}
