package edu.duke.ece568.tools.database;

import edu.duke.ece568.tools.log.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class PostgreSQLJDBC {

    private static PostgreSQLJDBC postgreSQLJDBC;
    private Connection c = null;

    /**
     * Get the instance of PostgreSQLJDBC
     * @return the instance
     */
    public static PostgreSQLJDBC getInstance() {
        System.out.println("DB getInstance");
        if (postgreSQLJDBC == null) postgreSQLJDBC = new PostgreSQLJDBC();
        return postgreSQLJDBC;
    }

    /**
     * Construct of PostgreSQLJDBC which clear the tables and build the tables
     */
    private PostgreSQLJDBC(){
        System.out.println("Construct DB");
        // clear table if exist
        this.clearTables();
        // build table
        this.buildTables();
    }

    /**
     * Connect Database
     */
    private static Connection connectDB() {
        Connection c;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager.getConnection("jdbc:postgresql://database:5432/postgres",
                            "postgres", "postgres");
            Logger logger = Logger.getSingleton();
            System.out.println("Connect to DB");
            return c;
        } catch (SQLException | ClassNotFoundException e) {
            Logger logger = Logger.getSingleton();
            logger.write("Cannot connect database.");
            return null;
        }
    }

    /**
     * Run SQL statement
     * @param sql indicated string
     */
    private void runSQL(String sql){
        Statement statement;
        try {
            this.c = connectDB();
            statement = c.createStatement();
            statement.executeUpdate(sql);
            statement.close();
            this.c.close();
        } catch (SQLException e) {
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
        }
    }

    /**
     * Clear all tables if exist
     */
    private void clearTables(){
        String clearSQL = "DROP TABLE IF EXISTS Orders CASCADE;\n" +
                "DROP TYPE IF EXISTS Status_enum CASCADE;\n" +
                "DROP TABLE IF EXISTS Positions CASCADE;\n" +
                "DROP TABLE IF EXISTS Accounts CASCADE;\n";
        this.runSQL(clearSQL);
    }

    /**
     * Build all tables
     */
    private void buildTables(){
        String buildSQL = "CREATE TABLE Accounts (\n" +
                "                          Account_id SERIAL PRIMARY KEY,\n" +
                "                          Balance real NOT NULL CONSTRAINT positive_balance CHECK (Balance >= 0)\n" +
                ");\n" +
                "\n" +
                "CREATE TABLE Positions (\n" +
                "                           Position_id SERIAL PRIMARY KEY,\n" +
                "                           Symbol varchar(10) NOT NULL,\n" +
                "                           Amount real NOT NULL CONSTRAINT positive_amount CHECK (Amount >= 0),\n" +
                "                           Account_id int NOT NULL,\n" +
                "                           FOREIGN KEY(Account_id) REFERENCES Accounts(Account_id) ON DELETE CASCADE\n" +
                ");\n" +
                "\n" +
                "CREATE TYPE Status_enum AS ENUM ('OPEN', 'CANCELLED','EXECUTED');\n" +
                "CREATE TABLE Orders (\n" +
                "                        Order_id SERIAL PRIMARY KEY,\n" +
                "                        Trans_id int NOT NULL,\n" +
                "                        Account_id int NOT NULL,\n" +
                "                        Symbol varchar(10) NOT NULL,\n" +
                "                        Amount real NOT NULL,\n" +
                "                        Limit_price real NOT NULL CONSTRAINT positive_price CHECK (Limit_price >= 0),\n" +
                "                        Status Status_enum NOT NULL DEFAULT 'OPEN',\n" +
                "                        FOREIGN KEY(Account_id) REFERENCES Accounts(Account_id) ON DELETE CASCADE\n" +
                ");";
       this.runSQL(buildSQL);
    }

}

