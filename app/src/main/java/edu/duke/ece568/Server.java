package edu.duke.ece568;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;
import edu.duke.ece568.tools.log.Logger;
import edu.duke.ece568.tools.tcp.TCP;

import java.io.IOException;
import java.net.Socket;

public class Server {
    private TCP tcp;

    /**
     * Constructor of Server
     * 1. Build TCP for connection
     * 2. Connect DB and create tables
     * @param portNum
     */
    public Server(int portNum){
        try {
            this.tcp = new TCP(portNum);
            PostgreSQLJDBC.getInstance();
        } catch (IOException e) {
            Logger.getSingleton().write("Cannot build TCP connection.");
        }
    }


    public static void main(String[] args) {
        // init server
        int portNum = 12345;
        Server server = new Server(portNum);
        PostgreSQLJDBC.getInstance(); // init Database
        try {
            // infinite loop to let thread recv message
            while (true) {
                // accept tcp connection
                Socket client = server.tcp.acceptClient();

                // log the message
                Logger.getSingleton().write("Connect: " + client.getInetAddress().getHostAddress());

                // create a new thread object
                ClientHandler clientSock = new ClientHandler(client);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
