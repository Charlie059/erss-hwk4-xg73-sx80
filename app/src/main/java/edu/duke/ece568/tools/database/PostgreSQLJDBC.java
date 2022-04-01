package edu.duke.ece568.tools.database;

import edu.duke.ece568.counter.TransactionCounter;
import edu.duke.ece568.tools.log.Logger;
import org.checkerframework.checker.units.qual.A;

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
            c = DriverManager.getConnection("jdbc:postgresql://database:5432/postgres",
                            "postgres", "postgres");
            return c;
        } catch (SQLException | ClassNotFoundException e) {
            Logger logger = Logger.getSingleton();
            logger.write("Cannot connect database.");
            return null;
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
        this.runSQLUpdate(clearSQL);
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
                "                        Time bigint NOT NULL,\n" +
                "                        FOREIGN KEY(Account_id) REFERENCES Accounts(Account_id) ON DELETE CASCADE\n" +
                ");";
        this.runSQLUpdate(buildSQL);
    }

    /**
     * Run SQL statement
     * @param sql String
     * @return true for success exe
     */
    private boolean runSQLUpdate(String sql){
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
     * Run SQL query and return a ResultSet
     * @param sql String
     * @return ResultSet
     */
    private ResultSet runSQLQuery(String sql){
        Statement statement;
        try {
            this.c = connectDB();
            ResultSet rs;
            statement = c.createStatement();
            rs = statement.executeQuery(sql);
            this.c.close();
            return rs;
        } catch (SQLException e) {
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
            return null;
        }
    }


    /**
     * if already have an account, return true, else, return false
     * @param accountId of int
     * @return return true if account exist
     */
    private boolean checkAccountExist(int accountId){
        String queryInsertAccount = "SELECT * FROM accounts WHERE Account_id="+ accountId+ ";";
        ResultSet result = runSQLQuery(queryInsertAccount);
        try {
            return result != null && result.next();
        } catch (SQLException e) {
            Logger.getSingleton().write(e.getMessage());
            return false;
        }
    }


    /**
     * Insert Account to DB (Check permission and insert)
     * @param accountID int
     * @param balance double
     * @return true for success
     */
    public String createAccount(int accountID, double balance){
        boolean accountExist = false;//if true, create; else, not create
        // try to grant permission of creating account
        accountExist = checkAccountExist(accountID);
        if(balance<0){return "You can not create a account with negative balance";}
        // If createPermission indicates true that means the account exist, return error
        if (accountExist) return "You can not create account "+accountID+" because it exists!";
        else{  // If createPermission is grant, execute insert
            String insertSQL = "INSERT INTO accounts (account_id, balance) VALUES" +  "(" + accountID +"," + balance + ");";
            if (runSQLUpdate(insertSQL)) return null;//"You have successfully create the account "+ accountID;
            else return "The execution of runSQL has error!";
        }
    }



    /**
     * Check if create a new position
     * @param insertingSymbol
     * @param insertingAccountId
     * @return true for position exist
     * @throws SQLException
     */
    private boolean checkPositionExist(String insertingSymbol, int insertingAccountId) throws SQLException {
        String queryInsertPosition = "SELECT * FROM positions WHERE symbol=" + "'" + insertingSymbol +"'" + " AND account_id=" +insertingAccountId + ";";
        ResultSet result = runSQLQuery(queryInsertPosition);
        return result != null && result.next();
    }

    /**
     * Insert Position to account with amount
     * @param symbol
     * @param amount
     * @param account_id
     * @return true for success
     */
    public String createPosition(String symbol, double amount, int account_id){
        if(amount<0){return "You can not create position with negative amount of symbol";}
        String insertSQL = null;
        try{
            // Try to check if symbol exist in account
            if (checkPositionExist(symbol, account_id)){ // If symbol exists with account

                // Symbol in position exists with account, move to the next line and update
                ResultSet result = runSQLQuery("SELECT * FROM positions WHERE symbol="+ "'"+ symbol + "'" + " AND account_id=" +account_id + ";");
                result.next();

                // Update amount
                double updatedAmount = result.getDouble("Amount") + amount;
                insertSQL = "UPDATE positions SET amount=" + updatedAmount + " WHERE Account_id=" + account_id +" AND Symbol=" + "'"+symbol+"'" + ";";

            } // symbol in account does not exist, need create
            else insertSQL = "INSERT INTO positions (symbol, amount, account_id) VALUES (" + "'" + symbol + "'" + "," + amount + "," + account_id + ");";

        }catch (SQLException e){
            Logger.getSingleton().write(e.getMessage());
            return "The accountId is invalid";
        }
        // Run update or insert in DB
        if (runSQLUpdate(insertSQL)) return null;//"You have successfully create the symbol "+ symbol + " and it is in account" + account_id;
        else return "The execution of Create SQL has error!";
    }


    /**
     * Insert Order to DB (Basic Method)
     * @param trans_id
     * @param account_id
     * @param symbol
     * @param amount
     * @param limit_price
     * @return true for success
     */
    public boolean insertOrder(int trans_id, int account_id, String symbol, double amount, double limit_price, String status, long currtime){
        String insertSQL = "INSERT INTO orders (trans_id, account_id, symbol, amount, limit_price, status, time) VALUES ("+ trans_id +","+ account_id +"," + "'" + symbol + "'" + "," +  amount + "," + limit_price +"," + "'" + status + "'"+","+currtime+");";
        return runSQLUpdate(insertSQL);
    }


//    /**
//     * check the account is existing, true is existing, false otherwise
//     * @param account_id
//     * @return
//     */
//    private boolean checkAccountAvailable(int account_id){
//        try {
//            boolean createPermission = checkAccountExist(account_id);
//            if (createPermission == false){
//                return true;
//            }
//
//        }catch (SQLException e){
//            Logger logger = Logger.getSingleton();
//            logger.write(e.getMessage());
//        }
//        return false;
//    }



    //TODO TO BE CHECKED may be not throw is suitable
    /**
     * Buyer buy checker and execute
     * @param account_id
     * @param amount
     * @param limit_price
     * @return null for success or error for string
     * @throws SQLException
     */
    private String getMoneyOutOfBalanceForBuyer(int account_id, double amount, double limit_price) throws SQLException {
        String getCurrentBalanceSQL = "SELECT * FROM accounts WHERE account_id=" + account_id + ";";
        ResultSet result = runSQLQuery(getCurrentBalanceSQL);
        result.next();
        double originalBalance = result.getDouble("Balance");
        double newBalance = originalBalance - amount*limit_price;
        if (newBalance < 0){
            return "The buyer can not make this order because not enough balance";
        }
        String changeAccountBalanceSQL = "UPDATE accounts SET balance="+ newBalance + " WHERE account_id=" + account_id +";";
        boolean runSuccess = runSQLUpdate(changeAccountBalanceSQL);
        if (runSuccess == true){
            return null;
        }
        else{
            return "The execution of runSQL has error!";
        }

    }


    //TODO TO BE CHECKED
    /**
     * if amount == 0, this position no longer belongs to this account;
     * @param account_id
     * @param symbol
     * @param amount
     * @return
     * @throws SQLException
     */
    private String getPositionOutForSeller(int account_id, String symbol, double amount)  {
        String getCurrentPositionSQL = "SELECT * FROM positions WHERE symbol=" + "'" + symbol + "'" + " AND account_id=" +account_id + ";";
        ResultSet result = runSQLQuery(getCurrentPositionSQL);
        try {
            result.next();

            double originalAmount = result.getDouble("Amount");
            double newAmount = originalAmount + amount;
            if (amount > originalAmount){
                return "The seller can not make this order because not enough symbol";
            }
            String changePositionAmountSQL = "UPDATE positions SET amount=" + newAmount + " WHERE symbol="+ "'" + symbol + "'" + " AND account_id=" +account_id + ";";
            boolean runSuccess = runSQLUpdate(changePositionAmountSQL);
            if (runSuccess == true){
                return null;
            }
            else{
                return "The execution of runSQL has error!";
            }
        }catch (SQLException e){
            return "This account can not sell this symbol because it does not have this symbol";
        }
    }


    /**
     * Search Database if we have potential order is matching
     * @param symbol String
     * @param amount > 0 for buy, < 0 for selling
     * @param limit_price
     * @return ResultSet
     * @throws SQLException
     */
    private ResultSet searchMatchingOrder(String symbol, double amount, double limit_price) throws SQLException {
        String theOpposite = null;
        String theExtremeSQL = null;

        // If buying, search the lowest selling OPEN order price
        if (amount > 0){
            //TODO why we write twice SQL?
            theOpposite = "SELECT limit_price FROM orders WHERE status='OPEN' AND amount<0 AND limit_price<=" + limit_price + " AND symbol="+"'"+symbol+"'";
            theExtremeSQL = "SELECT * FROM orders WHERE status='OPEN' AND amount<0 AND limit_price<=" + limit_price + " AND symbol="+"'"+symbol+"'"+" AND limit_price<=ALL(" + theOpposite +")"+";";
        }
        else{ // If selling, search the highest buying OPEN order price
            theOpposite = "SELECT limit_price FROM orders WHERE status='OPEN' AND amount>0 AND limit_price>=" + limit_price + " AND symbol="+"'"+symbol+"'";
            theExtremeSQL = "SELECT * FROM orders WHERE status='OPEN' AND amount>0 AND limit_price>=" + limit_price + " AND symbol="+"'"+symbol+"'"+" AND limit_price>=ALL(" + theOpposite +")"+";";
        }
        ResultSet result = runSQLQuery(theExtremeSQL);

        if (result.next()) return result; // If we found result then return
        else return null;
    }


    /**
     * Check account's balance
     * @param account_id
     * @return null for failure or Double
     */
    private Double getCurrentBalance(int account_id){
        Double currBalance = null;
        try {
            ResultSet result = runSQLQuery("SELECT * FROM accounts WHERE account_id="+account_id+";");
            result.next();
            currBalance = result.getDouble("balance");
        }catch (SQLException e){
            Logger.getSingleton().write(e.getMessage());
        }
        return currBalance;
    }


    //TODO TO BE CHECKED
    /**
     * Buyer get return balance and position
     * seller get balance and lose position
     * @param account_id
     * @param returnBalance
     * @return something goes wrong, return false; else, true
     */
    private boolean paybackBalance(int account_id, double returnBalance){
        Double currBalance = getCurrentBalance(account_id);
        if (currBalance == null) return false;
        else{
            double newBalance = currBalance+returnBalance;
            String returnBalanceSQL = "UPDATE accounts SET balance="+newBalance+" WHERE account_id="+account_id+";";
            return runSQLUpdate(returnBalanceSQL);
        }
    }

    //TODO TO BE CHECKED
    private boolean getPositionForBuyer(String symbol, double amount, int account_id){
        String message = createPosition(symbol, amount, account_id);
        if(message == null){
            return true;
        }
        return false;
    }
    //TODO TO BE CHECKED
    private boolean getBalanceForSeller(int account_id, double addBalance){
        boolean executed = paybackBalance(account_id, addBalance);
        return executed;
    }

    //TODO TO BE CHECKED
    private void executeReturnBalanceAndPosition(double buyerLimit, double executedLimit, double executedAmount, int buyerAccountId, int sellerAccountId, String symbol){
        double returnBalance = (buyerLimit- executedLimit)*executedAmount;
        paybackBalance(buyerAccountId, returnBalance);
        getPositionForBuyer(symbol, executedAmount, buyerAccountId);
        double getBalance = executedLimit * executedAmount;
        getBalanceForSeller(sellerAccountId, getBalance);
    }


    //TODO Checking now
    /**
     * return the updated amount
     * @param result
     * @param amount
     * @return
     */
    private double executeMatchingOrder(ResultSet result, double amount, int AmountTran_Id, int Amountaccount_id, String Amountsymbol, double AmountLimit_price) {
        Integer AmountorderId = getOrderID(AmountTran_Id);
        if(AmountorderId == null) return 0;

        double executed_amount= 0;

        // Get potential matched order_id, tran_id, account_id, amount, limit_price
        int order_id = 0, tran_id = 0, account_id = 0; double limit_price = 0, matchingAmount = 0;
        try {
            order_id = result.getInt("order_id");
            tran_id = result.getInt("trans_id");
            account_id = result.getInt("account_id");
            matchingAmount = result.getDouble("amount");
            limit_price = result.getDouble("limit_price");
        }catch (SQLException e){Logger.getSingleton().write(e.getMessage()); return 0;}

        // Execute order

        // If potential matched amount is perfectly match to current order's amount
        if (matchingAmount + amount == 0){
            // Change both status from 'open' to 'executed'
            long currtime = java.time.Instant.now().getEpochSecond();//java.time.Instant.now().getEpochSecond()
            String updateStatusForMatchingSQL = "UPDATE orders SET status='EXECUTED', time="+currtime+" WHERE order_id="+order_id+";";
            runSQLUpdate(updateStatusForMatchingSQL); // change potential matched order to EXECUTED

            String updateStatusForAmountSQL = "UPDATE orders SET status='EXECUTED', time="+currtime+" limit_price="+ limit_price +" WHERE order_id="+AmountorderId+";";
            runSQLUpdate(updateStatusForAmountSQL); // change current order to EXECUTED

            // Trade settlement
            executed_amount = Math.abs(amount);
            // If is a buy order
            if (amount > 0) executeReturnBalanceAndPosition(AmountLimit_price, limit_price, executed_amount, Amountaccount_id, account_id, Amountsymbol);
            // If is a selling order
            else executeReturnBalanceAndPosition(limit_price, limit_price, executed_amount, account_id, Amountaccount_id, Amountsymbol);
            return 0;
        }
        // After execute the new inserted order, there is still amount left and cannot be executed
        else if ((matchingAmount + amount > 0 && amount > 0) || (matchingAmount + amount < 0 && amount < 0)){
            //update amount and go next check and change matching status from 'open' to 'executed' and
            //split amount(check if there has been splitted)
            long currtime = java.time.Instant.now().getEpochSecond();
            String updateStatusForMatchingSQL = "UPDATE orders SET status='EXECUTED', time="+currtime+" WHERE order_id="+order_id+";";

            insertOrder(AmountTran_Id, Amountaccount_id, Amountsymbol, -matchingAmount, limit_price, "EXECUTED", currtime);
            double newAmount = matchingAmount + amount;
            String updateAmountForAmountSQL = "UPDATE orders SET amount="+newAmount+", time="+currtime+" WHERE order_id="+AmountorderId+";";
            runSQLUpdate(updateStatusForMatchingSQL);
            runSQLUpdate(updateAmountForAmountSQL);
            executed_amount = Math.abs(matchingAmount);
            if (amount > 0){
                executeReturnBalanceAndPosition(AmountLimit_price, limit_price, executed_amount, Amountaccount_id, account_id, Amountsymbol);
            }
            else{
                executeReturnBalanceAndPosition(limit_price, limit_price, executed_amount, account_id, Amountaccount_id, Amountsymbol);
            }
            return newAmount;
        }
        else if((matchingAmount + amount < 0 && amount > 0) || (matchingAmount + amount > 0 && amount < 0)){
            //change amount's status from 'open' to 'executed' and split matching(check if there has been splitted)
            long currtime = java.time.Instant.now().getEpochSecond();
            String updateStatusForAmountSQL = "UPDATE orders SET status='EXECUTED', limit_price="+ limit_price +" WHERE order_id="+AmountorderId+";";

            insertOrder(tran_id, account_id, Amountsymbol, -amount, limit_price, "EXECUTED", currtime);
            double newMatchingAmount = matchingAmount + amount;
            String updateAmountForMatchingSQL = "UPDATE orders SET amount="+newMatchingAmount+", time="+currtime+" WHERE order_id="+order_id+";";
            runSQLUpdate(updateAmountForMatchingSQL);
            runSQLUpdate(updateStatusForAmountSQL);
            executed_amount = Math.abs(amount);
            if (amount > 0){
                executeReturnBalanceAndPosition(AmountLimit_price, limit_price, executed_amount, Amountaccount_id, account_id, Amountsymbol);
            }
            else{
                executeReturnBalanceAndPosition(limit_price, limit_price, executed_amount, account_id, Amountaccount_id, Amountsymbol);
            }
            return 0;
        }
        return 0;
    }

    //TODO TO BE CHECKED
    private Integer getOrderID(int tranID) {
        String getOrderIDSQL = "SELECT order_ID FROM orders WHERE trans_id="+tranID+" AND status=" + "'OPEN'" + ";";
        ResultSet result = runSQLQuery(getOrderIDSQL);
        Integer order_id = -1;
        try{
            result.next();
            order_id = result.getInt("order_id");
        }catch (SQLException e){
            Logger.getSingleton().write(e.getMessage());
            return null;
        }
        return order_id;
    }

    /**
     * processTransactionOrder XML
     * @param account_id
     * @param symbol
     * @param amount
     * @param limit_price
     * @return null for no error
     */
    public String processTransactionOrder(int account_id, String symbol, double amount, double limit_price, int tran_id){
        // If account ID is not exist
        if (!checkAccountExist(account_id)) return "This account "+ account_id + " does not exist";
        else if(amount == 0) return "Cannot create order which amount is zero";
        else{  // if the account is available, add this order to Orders Table
            //int tran_id = TransactionCounter.getInstance().getCurrent_id();
            // Insert Order to the DB


            // Reduce buyer's balance or Reduce seller's positions amount
            try {
                String reduceResult = null;
                if(amount > 0) reduceResult = getMoneyOutOfBalanceForBuyer(account_id, amount, limit_price);
                else reduceResult = getPositionOutForSeller(account_id, symbol, amount);

                // if we cannot apply reducing then return error
                if (reduceResult != null) return reduceResult;
            }catch (SQLException e){
                Logger.getSingleton().write(e.getMessage());
            }
            long currtime = java.time.Instant.now().getEpochSecond();
            if(!insertOrder(tran_id, account_id, symbol, amount, limit_price, "OPEN", currtime)) return "The execution of runSQL has error!";

            // Match one or more orders
            double amount_temp = amount; // temp amount
            while (true){
                ResultSet result = null;
                try {
                    result = searchMatchingOrder(symbol, amount, limit_price);
                }catch (SQLException e){
                    Logger.getSingleton().write(e.getMessage());
                    return "Error to execute SQL";
                }

                if (result == null) break; // if not found, break the loop
                else if(amount == 0) break; // if amount is 0, break the loop
                else if((int)Math.signum(amount) * (int)Math.signum(amount_temp) == -1) break; // break if the amount is reverted from temp
                else{
                    // Execute matching orders
                    amount = executeMatchingOrder(result, amount, tran_id, account_id, symbol, limit_price);
                }
            }

        }
        return null;
    }

    //TODO TO BE CHECKED
    private ResultSet getCancelledOrder(int trans_id){
        String getCancelledSQL = "SELECT * FROM orders WHERE trans_id="+trans_id+ " AND status=" + "'OPEN'"+ ";";
        ResultSet result = runSQLQuery(getCancelledSQL);
        try{
            result.next();
        }catch (SQLException e){
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
            return null;
        }
        return result;
    }



    private String checkCancelAccount(int account_id, int trans_id){
        ResultSet resultoftransId = checkTransactionIdExist(trans_id);
        if (resultoftransId == null){
            return "the trans_id does not exist";
        }
        ResultSet result = getCancelledOrder(trans_id);
        if (result == null){
            return "no such transactionID is open to be canceled";
        }
        int AccountIdOfOrder = 0;
        try{
            AccountIdOfOrder = result.getInt("Account_id");
        }catch(SQLException e){
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
        }
        if (AccountIdOfOrder != account_id){
            return "This account can not cancel a transaction order that does not belong to this account";
        }
        return null;
    }



    //TODO TO BE CHECKED
    public String processTransactionCancel(int account_id, int trans_id){
        //change status from OPEN to CANCELLED
        Logger logger = Logger.getSingleton();
        String cancelPermission = checkCancelAccount(account_id, trans_id);
        if(cancelPermission != null){
            return cancelPermission;
        }
        ResultSet result = getCancelledOrder(trans_id);
        long currtime = java.time.Instant.now().getEpochSecond();
        String changeStatusSQL = "UPDATE orders SET status='CANCELLED', time="+currtime+" WHERE trans_id="+trans_id+ " AND status=" + "'OPEN'"+ ";";
        runSQLUpdate(changeStatusSQL);
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


    private ResultSet checkTransactionIdExist(int trans_id){
        String getQuerySQL = "SELECT * FROM orders WHERE trans_id="+trans_id+ ";";
        ResultSet result = runSQLQuery(getQuerySQL);
        try{
            result.next();
        }catch (SQLException e){
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
            return null;
        }

        return result;
    }
    //TODO: must be add to main() before processTransactionQuery
    public String checkTransactionQuery(int account_id, int trans_id){
        ResultSet result = checkTransactionIdExist(trans_id);
        if (result == null){
            return "the trans_id does not exist";
        }
        int AccountIdOfOrder = 0;
        try{
            AccountIdOfOrder = result.getInt("Account_id");
        }catch(SQLException e){
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
        }
        if (AccountIdOfOrder != account_id){
            return "This account can not query a transaction order that does not belong to this account";
        }
        return null;

    }

    //TODO TO BE CHECKED
    public ResultSet processTransactionQuery(int account_id, int trans_id){

        String querySQL = "SELECT * FROM orders WHERE trans_id="+trans_id;
        ResultSet result = runSQLQuery(querySQL);
        return result;
    }

    public void printQueryResult(ResultSet result){
        try {
            while (result.next()) {
                System.out.println("Order_id=" + result.getInt("Order_id") + "   " + "Trans_id=" + result.getInt("Trans_id") + "   " + "Account_id" + result.getInt("Account_id") + "   " + "Symbol=" + result.getString("Symbol") + "   " + "Amount=" + result.getDouble("Amount") + "   " + "Limit_price=" + result.getDouble("Limit_price") + "   " + "Status=" + result.getString("Status"));
            }
        }catch (SQLException e){
            Logger logger = Logger.getSingleton();
            logger.write(e.getMessage());
        }
    }




}

