package edu.duke.ece568;

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
            // Recv from Client
            String str = TCP.recvMsg(this.clientSocket);

            // Send to Client
            TCP.sendMsg(this.clientSocket, str);

            // Close the TCP Connection
            this.clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
