package edu.duke.ece568.tools.tcp;

import edu.duke.ece568.tools.log.Logger;

import javax.net.ServerSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


/**
 * Response for TCP connection
 */
public class TCP {
    private final int portNum;
    private final ServerSocket serverSocket;

    /**
     * Constructor of TCP
     * @param portNum
     * @throws IOException
     */
    public TCP(int portNum) throws IOException {
        this.portNum = portNum;
        this.serverSocket = new ServerSocket(portNum);
//        this.serverSocket = ServerSocketFactory.getDefault().createServerSocket();
//        this.serverSocket.bind(new InetSocketAddress("0.0.0.0", portNum));
    }

    /**
     * Accept the connection from the client
     * @return Client socket
     * @throws IOException
     */
    public Socket acceptClient() throws IOException {
        return this.serverSocket.accept();
    }

    /**
     * Send message to the client (Do NOT ADD \n)
     *
     * @param socket
     * @param msg
     * @throws IOException
     */
    public static void sendMsg(Socket socket, String msg) throws IOException {
        OutputStream out = socket.getOutputStream();
        var writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        writer.write(msg + "\n");
        writer.flush();
    }

    /**
     * Recv msg from client
     *
     * @param socket
     * @return the msg
     * @throws IOException
     */
    public static String recvMsg(Socket socket) throws IOException {
        InputStream in = socket.getInputStream();
        var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        int contentLength = 0;

        // Read the first line of content length
        String contentLenStr = reader.readLine();
        try {
            contentLength = Integer.parseInt(contentLenStr);
        } catch (NumberFormatException e) {
            Logger.getSingleton().write("Cannot find the content length");
            return null;
        }

        int r;
        StringBuilder xmlData = new StringBuilder(new String());
        while ((r = reader.read()) != -1) {
            if(contentLength <= 0) break;
            xmlData.append((char) r);
            contentLength--;
        }

        return String.valueOf(xmlData);
    }

}
