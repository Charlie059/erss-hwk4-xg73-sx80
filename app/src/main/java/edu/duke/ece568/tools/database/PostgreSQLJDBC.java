package edu.duke.ece568.tools.database;

import edu.duke.ece568.tools.log.Logger;

import java.sql.*;


public class PostgreSQLJDBC {

    private static PostgreSQLJDBC postgreSQLJDBC;
    private Connection c = null;

    /**
     * Get the instance of PostgreSQLJDBC
     * @return the instance
     */
    public static PostgreSQLJDBC getInstance() {
        if (postgreSQLJDBC == null) postgreSQLJDBC = new PostgreSQLJDBC();
        return postgreSQLJDBC;
    }

    /**
     * Construct of PostgreSQLJDBC which clear the tables and build the tables
     */
    private PostgreSQLJDBC(){
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
            // TODO use environment var: change to database when in use
            c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres",
                            "postgres", "postgres");
            return c;
        } catch (SQLException | ClassNotFoundException e) {
            Logger logger = Logger.getSingleton();
            logger.write("Cannot connect database.");
            return null;
        }
    }

    private ResultSet runQuerySQL(String sql){
        Statement statement;
        try {
            this.c = connectDB();
            ResultSet rs = null;
            statement = c.createStatement();
            rs = statement.executeQuery(sql);
            statement.close();
            this.c.close();
            return rs;
        } catch (SQLException e) {
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
            return null;
        }
    }

    private boolean checkInsertAccount(int insertingAccountId) throws SQLException {
        String queryInsertAccount = "Select * From accounts Where Account_id="+ insertingAccountId+ ";";
        ResultSet result = runQuerySQL(queryInsertAccount);
        return !result.next();
    }


    /**
     * Insert Account to DB
     * @param accountID
     * @param balance
     * @return true for success
     */
    public String insertAccount(int accountID, double balance){
        boolean insertPermission = false;
        try{
            insertPermission = checkInsertAccount(accountID);

        }catch (SQLException e){
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
        }
        if (insertPermission == false){
            return "You can not create account "+accountID+" because it exists!";
        }
        String insertSQL = "INSERT INTO accounts (account_id, balance) VALUES" +  "(" + accountID +"," + balance + ");";
        boolean createSuccess = runSQL(insertSQL);
        if (createSuccess == true){
            return "You have successfully create the account "+ accountID;
        }
        return "The execution of Create SQL has error!";
    }


    /**
     * Insert Order to DB
     * @param trans_id
     * @param account_id
     * @param symbol
     * @param amount
     * @param limit_price
     * @return true for success
     */
    public boolean insertOrder(int trans_id, int account_id, String symbol, double amount, double limit_price){
        String insertSQL = "INSERT INTO orders (trans_id, account_id, symbol, amount, limit_price) VALUES ("+ trans_id +","+ account_id +"," + "'" + symbol + "'" + "," +  amount + "," + limit_price + ");";
        return runSQL(insertSQL);
    }

    /**
     * Insert Position
     * @param symbol
     * @param amount
     * @param account_id
     * @return true for success
     */
    public boolean insertPosition(String symbol, double amount, int account_id){
        String insertSQL = "INSERT INTO positions (symbol, amount, account_id) VALUES (" + "'" + symbol + "'" + "," + amount + ","+ account_id + ");";
        return runSQL(insertSQL);
    }


    /**
     * Run SQL statement
     * @param sql indicated string
     */
    private boolean runSQL(String sql){
        Statement statement;
        try {
            this.c = connectDB();
            statement = c.createStatement();
            statement.executeUpdate(sql);
            statement.close();
            this.c.close();
            return true;
        } catch (SQLException e) {
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
            return false;
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

