package edu.duke.ece568.tools.parser;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import edu.duke.ece568.tools.log.Logger;
import org.w3c.dom.Document;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Parser {
    String xml;
    DocumentBuilder dBuilder;
    Document document;

    public Parser(String xml) throws ParserConfigurationException, IOException, SAXException {
        this.xml = xml;
        this.dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    /**
     * Parse the xml
     * @return Document object
     * @throws IOException
     * @throws SAXException
     */
    public Document parse() throws IOException, SAXException {
        return this.dBuilder.parse(new InputSource((new StringReader(this.xml))));
    }

    /**
     * Check the XML type and create the specific parser
     * @return null for error, or return parser
     */
    public Parser createParser() {
        String type = null;
        try {
            // Get the type of XML name
            type = parse().getDocumentElement().getNodeName();
            if (type.equals("create")) {
                return new CreateParser(this.xml);
            } else if (type.equals("transactions")) {
                return new TransactionParser(this.xml);
            } else return null;
        } catch (IOException | SAXException | ParserConfigurationException e) {
            Logger.getSingleton().write("Unexpected error in parsing");
            return null;
        }
    }

    public void run(){};
}
