package edu.duke.ece568;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;
import edu.duke.ece568.tools.tcp.TCP;

import java.io.IOException;
import java.net.Socket;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server {
    private Logger logger;
    private FileHandler fh;
    private TCP tcp;
    private PostgreSQLJDBC postgreSQLJDBC;

    /**
     * init logger
     */
    private void initLogger(){
        this.logger = Logger.getLogger("ServerLog");
        try {
            this.fh = new FileHandler("server.log");
            this.logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            this.fh.setFormatter(formatter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor of Server
     * 1. Build TCP for connection
     * 2. Connect DB and create tables
     * @param portNum
     */
    public Server(int portNum){
        // init logger
        initLogger();
        try {
            this.tcp = new TCP(portNum);
            this.postgreSQLJDBC = PostgreSQLJDBC.getInstance();
        } catch (IOException e) {
            this.logger.info("Cannot build TCP connection.");
        }
    }


    public static void main(String[] args) {
        // init server
        int portNum = 12345;
        Server server = new Server(portNum);

        try {
            // infinite loop to let thread recv message
            while (true) {
                // accept tcp connection
                Socket client = server.tcp.acceptClient();
                // log the message
                server.logger.info("Connect: " + client.getInetAddress().getHostAddress());
                // create a new thread object
                ClientHandler clientSock = new ClientHandler(client);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
