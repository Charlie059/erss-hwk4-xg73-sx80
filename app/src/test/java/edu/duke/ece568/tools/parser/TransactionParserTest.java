package edu.duke.ece568.tools.parser;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TransactionParserTest {

    @Test
    void parse() throws IOException, SAXException, ParserConfigurationException {
        String xml = Files.readString(Path.of("../XMLSamples/transaction.xml"), StandardCharsets.UTF_8);
        System.out.println(xml);
        Parser parser = new Parser(xml).createParser();
        parser.parse();
        parser.run();
    }

    @Test
    void run() {
    }
}