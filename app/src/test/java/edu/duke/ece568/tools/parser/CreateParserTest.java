package edu.duke.ece568.tools.parser;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CreateParserTest {

    @Test
    void parse() throws ParserConfigurationException, IOException, SAXException {
        String xml = Files.readString(Path.of("../XMLSamples/testCount.xml"), StandardCharsets.UTF_8);
        System.out.println(xml.length());
    }
}