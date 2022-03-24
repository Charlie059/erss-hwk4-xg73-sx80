package edu.duke.ece568;

import edu.duke.ece568.tools.log.Logger;
import org.junit.jupiter.api.Test;

class LoggerTest {

    @Test
    void write() {
        Logger logger = Logger.getSingleton();
        logger.write("AAAA");
    }
}