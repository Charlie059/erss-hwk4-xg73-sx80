package edu.duke.ece568.tools.database;

import edu.duke.ece568.tools.log.Logger;

import java.sql.*;


public class PostgreSQLJDBC {

    private static PostgreSQLJDBC postgreSQLJDBC;
    private Connection c = null;

    private int tran_id = 1;
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

    /**
     * if no such account, return true, else, return false
     * @param insertingAccountId
     * @return
     * @throws SQLException
     */
    private boolean checkCreateAccount(int insertingAccountId) throws SQLException {
        String queryInsertAccount = "SELECT * FROM accounts WHERE Account_id="+ insertingAccountId+ ";";
        ResultSet result = runQuerySQL(queryInsertAccount);
        return !result.next();
    }


    /**
     * Insert Account to DB
     * @param accountID
     * @param balance
     * @return true for success
     */
    public String createAccount(int accountID, double balance){
        boolean createPermission = false;//if true, create; else, not create
        try{
            createPermission = checkCreateAccount(accountID);

        }catch (SQLException e){
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
        }
        if (createPermission == false){
            return "You can not create account "+accountID+" because it exists!";
        }
        String insertSQL = "INSERT INTO accounts (account_id, balance) VALUES" +  "(" + accountID +"," + balance + ");";
        boolean createSuccess = runSQL(insertSQL);
        if (createSuccess == true){
            return "You have successfully create the account "+ accountID;
        }
        return "The execution of runSQL has error!";
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
    public boolean insertOrder(int trans_id, int account_id, String symbol, double amount, double limit_price, String status){
        String insertSQL = "INSERT INTO orders (trans_id, account_id, symbol, amount, limit_price, status) VALUES ("+ trans_id +","+ account_id +"," + "'" + symbol + "'" + "," +  amount + "," + limit_price +"," + "'" + status + "'"+");";
        return runSQL(insertSQL);
    }


    /**
     * check the account is existing, true is existing, false otherwise
     * @param account_id
     * @return
     */
    private boolean checkAccountAvailable(int account_id){
        try {
            boolean createPermission = checkCreateAccount(account_id);
            if (createPermission == false){
                return true;
            }

        }catch (SQLException e){
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
        }
        return false;
    }



    private String getMoneyOutOfBalanceForBuyer(int account_id, double amount, double limit_price) throws SQLException {
        String getCurrentBalanceSQL = "SELECT * FROM accounts WHERE account_id="+account_id+";";
        ResultSet result = runQuerySQL(getCurrentBalanceSQL);
        result.next();
        double originalBalance = result.getDouble("Balance");
        double newBalance = originalBalance - amount*limit_price;
        if (newBalance < 0){
            return "The buyer can not make this order because not enough balance";
        }
        String changeAccountBalanceSQL = "UPDATE accounts SET balance="+ newBalance + " WHERE account_id=" + account_id +";";
        boolean runSuccess = runSQL(changeAccountBalanceSQL);
        if (runSuccess == true){
            return null;
        }
        else{
            return "The execution of runSQL has error!";
        }

    }

    /**
     * if amount == 0, this position no longer belongs to this account;
     * @param account_id
     * @param symbol
     * @param amount
     * @return
     * @throws SQLException
     */
    private String getPositionOutForSeller(int account_id, String symbol, double amount) throws SQLException {
        String getCurrentPositionSQL = "SELECT * FROM positions WHERE symbol="+ symbol + " AND account_id=" +account_id + ";";
        ResultSet result = runQuerySQL(getCurrentPositionSQL);
        result.next();
        double originalAmount = result.getDouble("Amount");
        double newAmount = originalAmount - amount;
        if (amount > originalAmount){
            return "The seller can not make this order because not enough symbol";
        }
        String changePositionAmountSQL = "UPDATE positions SET amount=" + newAmount + "WHERE symbol="+ symbol + " AND account_id=" +account_id + ";";
        boolean runSuccess = runSQL(changePositionAmountSQL);
        if (runSuccess == true){
            return null;
        }
        else{
            return "The execution of runSQL has error!";
        }
    }


    private ResultSet searchMatchingOrder(String symbol, double amount, double limit_price) throws SQLException {
        String theOpposite = null;
        String theExtremeSQL = null;
        if (amount>0){
            theOpposite = "SELECT limit_price FROM orders WHERE status='OPEN' AND amount<0 AND limit_price<=" + limit_price + " AND symbol="+"'"+symbol+"'";
            theExtremeSQL = "SELECT * FROM orders WHERE status='OPEN' AND amount<0 AND limit_price<=" + limit_price + " AND symbol="+"'"+symbol+"'"+" AND limit_price<=ALL(" + theOpposite +")"+";";
        }
        else{
            theOpposite = "SELECT limit_price FROM orders WHERE status='OPEN' AND amount>0 AND limit_price>=" + limit_price + " AND symbol="+"'"+symbol+"'";
            theExtremeSQL = "SELECT * FROM orders WHERE status='OPEN' AND amount>0 AND limit_price>=" + limit_price + " AND symbol="+"'"+symbol+"'"+" AND limit_price>=ALL(" + theOpposite +")"+";";
        }
        ResultSet result = runQuerySQL(theExtremeSQL);

        if (result.next()){
            return result;
        }
        return null;
    }


    private double getCurrentBalance(int account_id){
        String getCurrentBalance = "SELECT * FROM accounts WHERE account_id="+account_id+";";
        ResultSet result = runQuerySQL(getCurrentBalance);
        double currBalance = -1;
        try {
            result.next();
            currBalance = result.getDouble("balance");
        }catch (SQLException e){
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
        }
        return currBalance;
    }

    /**
     * something goes wrong, return false; else, true
     * buyer get return balance and position
     * seller get balance and lose position
     * @param account_id
     * @param returnBalance
     * @return
     */
    private boolean paybackBalance(int account_id, double returnBalance){
        double currBalance = getCurrentBalance(account_id);
        if (currBalance == -1){
            return false;
        }
        double newBalance = currBalance+returnBalance;
        String returnBalanceSQL = "UPDATE accounts SET balance="+newBalance+" WHERE account_id="+account_id+";";
        runSQL(returnBalanceSQL);
        return true;
    }

    private boolean getPositionForBuyer(String symbol, double amount, int account_id){
        String message = createPosition(symbol, amount, account_id);
        String expected = "You have successfully create the symbol "+ symbol + "and it is in account" + account_id;
        if(message == expected){
            return true;
        }
        return false;
    }

    private boolean getBalanceForSeller(int account_id, double addBalance){
        boolean executed = paybackBalance(account_id, addBalance);
        return executed;
    }


    private void executeReturnBalanceAndPosition(double buyerLimit, double executedLimit, double executedAmount, int buyerAccountId, int sellerAccountId, String symbol){
        double returnBalance = (buyerLimit- executedLimit)*executedAmount;
        paybackBalance(buyerAccountId, returnBalance);
        getPositionForBuyer(symbol, executedAmount, buyerAccountId);
        double getBalance = executedLimit * executedAmount;
        getBalanceForSeller(sellerAccountId, getBalance);
    }

    /**
     * return the updated amount
     * @param result
     * @param amount
     * @return
     */
    private double executeMatchingOrder(ResultSet result, double amount, int AmountTran_Id, int Amountaccount_id, String Amountsymbol, double AmountLimit_price) {
        Logger logger = Logger.getSingleton();
        int AmountorderId = getOrderID(AmountTran_Id);
        double executed_price = 0;
        double executed_amount= 0;
        int order_id = 0;
        int tran_id = 0;
        int account_id = 0;
        double limit_price = 0;
        try {
            order_id = result.getInt("Order_id");
            tran_id = result.getInt("Trans_id");
            account_id = result.getInt("Account_id");
            limit_price = result.getDouble("Limit_price");
        }catch (SQLException e){
            logger.write(e.getMessage());
        }
        try {
            double matchingAmount = result.getDouble("Amount");
            executed_price = result.getDouble("Limit_price");
            if (matchingAmount + amount == 0){
                //change status from 'open' to 'executed'
                String updateStatusForMatchingSQL = "UPDATE orders SET status='EXECUTED' WHERE order_id="+order_id+";";
                String updateStatusForAmountSQL = "UPDATE orders SET status='EXECUTED' WHERE order_id="+AmountorderId+";";
                runSQL(updateStatusForMatchingSQL);
                runSQL(updateStatusForAmountSQL);
                executed_amount = Math.abs(amount);
                if (amount > 0){
                    executeReturnBalanceAndPosition(AmountLimit_price, limit_price, executed_amount, Amountaccount_id, account_id, Amountsymbol);
                }
                else{
                    executeReturnBalanceAndPosition(limit_price, AmountLimit_price, executed_amount, account_id, Amountaccount_id, Amountsymbol);
                }
                return 0;
            }
            else if ((matchingAmount + amount > 0 && amount > 0) || (matchingAmount + amount < 0 && amount < 0)){
                //update amount and go next check and change matching's status from 'open' to 'executed' and
                //split amount(check if there has been splitted)

                String updateStatusForMatchingSQL = "UPDATE orders SET status='EXECUTED' WHERE order_id="+order_id+";";
                insertOrder(AmountTran_Id, Amountaccount_id, Amountsymbol, -matchingAmount, AmountLimit_price, "EXECUTED");
                double newAmount = matchingAmount + amount;
                String updateAmountForAmountSQL = "UPDATE orders SET amount="+newAmount+" WHERE order_id="+AmountorderId+";";
                runSQL(updateStatusForMatchingSQL);
                runSQL(updateAmountForAmountSQL);
                executed_amount = Math.abs(matchingAmount);
                if (amount > 0){
                    executeReturnBalanceAndPosition(AmountLimit_price, limit_price, executed_amount, Amountaccount_id, account_id, Amountsymbol);
                }
                else{
                    executeReturnBalanceAndPosition(limit_price, AmountLimit_price, executed_amount, account_id, Amountaccount_id, Amountsymbol);
                }
                return newAmount;
            }
            else if((matchingAmount + amount < 0 && amount > 0) || (matchingAmount + amount > 0 && amount < 0)){
                //change amount's status from 'open' to 'executed' and split matching(check if there has been splitted)
                String updateStatusForAmountSQL = "UPDATE orders SET status='EXECUTED' WHERE order_id="+AmountorderId+";";

                insertOrder(tran_id, account_id, Amountsymbol, -amount, limit_price, "EXECUTED");
                double newMatchingAmount = matchingAmount + amount;
                String updateAmountForMatchingSQL = "UPDATE orders SET amount="+newMatchingAmount+" WHERE order_id="+order_id+";";
                runSQL(updateAmountForMatchingSQL);
                runSQL(updateStatusForAmountSQL);
                executed_amount = Math.abs(amount);
                if (amount > 0){
                    executeReturnBalanceAndPosition(AmountLimit_price, limit_price, executed_amount, Amountaccount_id, account_id, Amountsymbol);
                }
                else{
                    executeReturnBalanceAndPosition(limit_price, AmountLimit_price, executed_amount, account_id, Amountaccount_id, Amountsymbol);
                }
                return 0;
            }

        }catch (SQLException e){
            logger.write(e.getMessage());
        }
        //TODO: return balance and add position

        //TODO: check this
        return 0;
    }

    private int getOrderID(int tranID) {
        String getOrderIDSQL = "SELECT order_ID FROM orders WHERE trans_id="+tranID+" AND status=" + "'OPEN'" + ";";
        ResultSet result = runQuerySQL(getOrderIDSQL);
        int order_id = -1;
        try{
            result.next();
            order_id = result.getInt("order_id");
        }catch (SQLException e){
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
        }

        return order_id;
    }

    public String processTransactionOrder(int account_id, String symbol, double amount, double limit_price){
        //check account_id is available
        Logger logger = Logger.getSingleton();
        if (checkAccountAvailable(account_id) == false){
            return "This account "+ account_id + " does not exist, so this order is an error";
        }
        //the account is available, add this order to Orders
        if(insertOrder(tran_id, account_id, symbol, amount, limit_price, "OPEN")){
            tran_id++;
        }
        else{
            return "The execution of runSQL has error!";
        }
        //TODO: what if amount == 0
        //amount > 0, it is a buy order
        if (amount > 0){
            //get the money out of buyer's balance(modify Table Accounts)
            try {
                String getMoneyOutOfBalanceForBuyerResult = getMoneyOutOfBalanceForBuyer(account_id, amount, limit_price);
                if (getMoneyOutOfBalanceForBuyerResult != null) {
                    return getMoneyOutOfBalanceForBuyerResult;
                }
            }catch (SQLException e){
                logger.write(e.getMessage());
            }
            while(amount > 0){
                //search for the matching sell order
                ResultSet result = null;
                try {
                    result = searchMatchingOrder(symbol, amount, limit_price);
                }catch (SQLException e){
                    logger.write(e.getMessage());
                }
                if (result == null){
                    break;
                }

                double updatedAmount = executeMatchingOrder(result, amount,  tran_id-1, account_id, symbol, limit_price);
                amount = updatedAmount;
            }

            //double newBalance =
            //String getMoneySQL = "UPDATE accounts SET balance=" + updatedAmount + "WHERE Account_id=" + account_id +" AND Symbol=" + "'"+symbol+"'" + ";";
        }
        else {
            //get the position out of seller's positions(modify Table Positions)
            try {
                String getPositionOutForSellerResult = getPositionOutForSeller(account_id, symbol, amount);
                if (getPositionOutForSellerResult != null){
                    return getPositionOutForSellerResult;
                }
            }catch (SQLException e){
                logger.write(e.getMessage());
            }
            while(amount < 0){
                //search for the matching sell order
                ResultSet result = null;
                try {
                    result = searchMatchingOrder(symbol, amount, limit_price);
                }catch (SQLException e){
                    logger.write(e.getMessage());
                }
                if (result == null){
                    break;
                }

                double updatedAmount = executeMatchingOrder(result, amount,  tran_id-1, account_id, symbol, limit_price);
                amount = updatedAmount;
            }
        }
        return null;
    }

    private ResultSet getCancelledOrder(int trans_id){
        String getCancelledSQL = "SELECT * FROM orders WHERE trans_id="+trans_id+ " AND status=" + "'OPEN'"+ ";";
        ResultSet result = runQuerySQL(getCancelledSQL);
        try{
            result.next();
        }catch (SQLException e){
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
        }
        return result;
    }

    public String processTransactionCancel(int account_id, int trans_id){
        //change status from OPEN to CANCELLED
        Logger logger = Logger.getSingleton();
        ResultSet result = getCancelledOrder(trans_id);
        String changeStatusSQL = "UPDATE orders SET status='CANCELLED' WHERE trans_id="+trans_id+ " AND status=" + "'OPEN'"+ ";";
        runSQL(changeStatusSQL);
        //give back the Position or Balance
        double amount = 0;
        String symbol = null;
        double limit_price = 0;
        try {
            amount = result.getDouble("Amount");
            symbol = result.getString("Symbol");
            limit_price = result.getDouble("Limit_price");
        }catch (SQLException e){

            logger.write(e.getMessage());
        }
        if(amount > 0){
            try {
                getMoneyOutOfBalanceForBuyer(account_id, amount, -limit_price);
            }catch (SQLException e){
                logger.write(e.getMessage());
            }

        }
        else if (amount < 0){
            createPosition(symbol, -amount, account_id);
        }
        return null;
    }



    public ResultSet processTransactionQuery(int account_id, int trans_id){
        String querySQL = "SELECT * FROM orders WHERE trans_id="+trans_id;
        ResultSet result = runQuerySQL(querySQL);
        return result;
    }


    private boolean checkCreatePosition(String insertingSymbol, double insertingAmount, int insertingAccountId) throws SQLException {
        String queryInsertAccount = "SELECT * FROM positions WHERE symbol="+ insertingSymbol + " AND account_id=" +insertingAccountId + ";";
        ResultSet result = runQuerySQL(queryInsertAccount);
        return !result.next();
    }
    /**
     * Insert Position
     * @param symbol
     * @param amount
     * @param account_id
     * @return true for success
     */
    public String createPosition(String symbol, double amount, int account_id){
        boolean createPermission = false;//if true, create; else, modify
        Logger logger = Logger.getSingleton();
        ResultSet result = null;
        String insertSQL = null;
        try{
            createPermission = checkCreatePosition(symbol, amount, account_id);
            String queryInsertAccount = "SELECT * FROM positions WHERE symbol="+ "'"+ symbol + "'" + " AND account_id=" +account_id + ";";
            result = runQuerySQL(queryInsertAccount);
            if (createPermission == false){
                //symbol in account exists with account, need update
                result.next();
                logger.write("symbol exists, we should modify the num in it");
                double updatedAmount = result.getDouble("Amount") + amount;
                insertSQL = "UPDATE positions SET amount=" + updatedAmount + " WHERE Account_id=" + account_id +" AND Symbol=" + "'"+symbol+"'" + ";";
            }
        }catch (SQLException e){
            logger.write(e.getMessage());
        }
        //symbol in account does not exist, need create
        if (createPermission == true) {
            insertSQL = "INSERT INTO positions (symbol, amount, account_id) VALUES (" + "'" + symbol + "'" + "," + amount + "," + account_id + ");";
        }
        boolean createSuccess = runSQL(insertSQL);
        if (createSuccess == true){
            return "You have successfully create the symbol "+ symbol + "and it is in account" + account_id;
        }
        return "The execution of Create SQL has error!";
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

