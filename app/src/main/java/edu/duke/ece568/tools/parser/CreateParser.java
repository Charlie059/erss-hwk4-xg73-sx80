package edu.duke.ece568.tools.parser;

import edu.duke.ece568.message.create.AccountCreateAction;
import edu.duke.ece568.message.create.CreateAction;
import edu.duke.ece568.message.create.SymbolCreateAction;
import edu.duke.ece568.tools.response.createAccountErrorResponse;
import edu.duke.ece568.tools.response.createAccountResponse;
import edu.duke.ece568.tools.response.createPositionErrorResponse;
import edu.duke.ece568.tools.response.createPositionResponse;
import org.w3c.dom.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

import org.dom4j.*;
import org.dom4j.io.SAXReader;

public class CreateParser extends Parser {
    ArrayList<CreateAction> createActions;

    public CreateParser(String xml) throws ParserConfigurationException, IOException, SAXException {
        super(xml);
        this.createActions = new ArrayList<>();
    }

    @Override
    public Document parse() throws IOException, SAXException {
        Document document = super.parse();
        NodeList nodeList = document.getDocumentElement().getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                // if we meet <account> tag then create a createAction
                if (nodeList.item(i).getNodeName().equals("account")) {
                    // Read Account info
                    NamedNodeMap attrs = nodeList.item(i).getAttributes();
                    String accountID = null, balance = null;
                    for (int j = 0; j < attrs.getLength(); j++) {
                        // Create an account object and add them into the createactions list
                        if(attrs.item(j).getNodeName().equals("id")) accountID = attrs.item(j).getNodeValue();
                        else balance = attrs.item(j).getNodeValue();
                    }
                    AccountCreateAction accountCreateAction = new AccountCreateAction(Integer.parseInt(accountID),Double.parseDouble(balance));
                    this.createActions.add(accountCreateAction);
                }
                // if we meet <symbol> tag then create a symbolCreateAction
                else if (nodeList.item(i).getNodeName().equals("symbol")) {
                    // Get <symbol> attribute
                    NamedNodeMap attrs = nodeList.item(i).getAttributes();
                    String sym = attrs.item(0).getNodeValue();

                    // Get Position info
                    NodeList positionList = nodeList.item(i).getChildNodes();
                    for (int j = 0; j < positionList.getLength(); j++) {
                        if (positionList.item(j).getNodeType() == Node.ELEMENT_NODE) {
                            // Get <account> in symbol attribute
                            NamedNodeMap positionAttr = positionList.item(j).getAttributes();
                            SymbolCreateAction symbolCreateAction = new SymbolCreateAction(Integer.parseInt(positionAttr.item(0).getNodeValue()), sym, Double.parseDouble(positionList.item(j).getTextContent()));
                            this.createActions.add(symbolCreateAction);
                        }
                    }
                }
            }
        }
        return document;
    }

    @Override
    public String run(){
        StringBuilder ans = new StringBuilder("<results>\n");
        for (int i = 0; i < this.createActions.size(); i++) {
            // Execute action
            boolean exeResult =  this.createActions.get(i).execute();

            // If execute success
            if(exeResult){
                // If we get AccountCreateAction
                if(this.createActions.get(i).getClass() == AccountCreateAction.class){
                    createAccountResponse createAccountResponse = new createAccountResponse(createActions.get(i).getAccountId());
                    ans.append(createAccountResponse.getResponse());
                }
                else{ // If we get SymbolCreateAction
                    // Transfer CreateAction to SymbolCreateAction
                    SymbolCreateAction symbolCreateAction = (SymbolCreateAction) this.createActions.get(i);
                    createPositionResponse  createPositionResponse = new createPositionResponse(symbolCreateAction.getAccountId(),symbolCreateAction.getSymbolName());
                    ans.append(createPositionResponse.getResponse());
                }
            }
            // Else if execute fail
            else{
                // If we get AccountCreateAction
                if(this.createActions.get(i).getClass() == AccountCreateAction.class){
                    createAccountErrorResponse createAccountErrorResponse = new createAccountErrorResponse(createActions.get(i).getAccountId());
                    ans.append(createAccountErrorResponse.getResponse());
                }
                else{ // If we get SymbolCreateAction
                    // Transfer CreateAction to SymbolCreateAction
                    SymbolCreateAction symbolCreateAction = (SymbolCreateAction) this.createActions.get(i);
                    createPositionErrorResponse createPositionErrorResponse = new createPositionErrorResponse(symbolCreateAction.getAccountId(),symbolCreateAction.getSymbolName());
                    ans.append(createPositionErrorResponse.getResponse());
                }
            }
        }
        ans.append("</results>\n");
        return ans.toString();
    }
}
