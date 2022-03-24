package edu.duke.ece568;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;
import org.junit.jupiter.api.Test;

class PostgreSQLJDBCTest {

    @Test
    void getInstance() {
        PostgreSQLJDBC postgreSQLJDBC = PostgreSQLJDBC.getInstance();
    }
}