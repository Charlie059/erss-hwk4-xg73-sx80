package edu.duke.ece568;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;
import edu.duke.ece568.tools.log.Logger;
import edu.duke.ece568.tools.tcp.TCP;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try {
            // logger to log for testing
            Logger logger = Logger.getSingleton();

            // Recv from Client
            String str = TCP.recvMsg(this.clientSocket);
            logger.write("RECV: "+str);

            if (str.equals("a")){
               System.out.println(PostgreSQLJDBC.getInstance().createAccount(1,2000));
                System.out.println(PostgreSQLJDBC.getInstance().createAccount(2,2000));
               System.out.println(PostgreSQLJDBC.getInstance().createPosition("BTC", 100, 1));
                //PostgreSQLJDBC.getInstance().insertOrder(1,1,"BTC",1000,100);
                PostgreSQLJDBC.getInstance().processTransactionOrder(1,"BTC",-10,1);
                PostgreSQLJDBC.getInstance().processTransactionOrder(1,"BTC",-20,1);
                PostgreSQLJDBC.getInstance().processTransactionOrder(2,"BTC",40,2);

            }

            // Send to Client
            TCP.sendMsg(this.clientSocket, str);
            logger.write("SEND: "+str);

            // Close the TCP Connection
            this.clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
