package edu.duke.ece568;

import edu.duke.ece568.tools.database.PostgreSQLJDBC;
import edu.duke.ece568.tools.log.Logger;
import edu.duke.ece568.tools.parser.CreateParser;
import edu.duke.ece568.tools.parser.Parser;
import edu.duke.ece568.tools.tcp.TCP;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

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

            // Parse the input xml
            Parser parser =  new Parser(str).createParser();
            parser.parse();

            String XMLResult = "";
            synchronized(ClientHandler.class){
                XMLResult = parser.run();
            }


            // Add the content length filed to xml
            int contentLength = XMLResult.getBytes().length;
            String ans = contentLength + "\n";
            ans = ans + XMLResult;
            // Send to Client
            TCP.sendMsg(this.clientSocket, ans);
            logger.write("SEND: "+ans);

            // Close the TCP Connection

        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
    }
}
