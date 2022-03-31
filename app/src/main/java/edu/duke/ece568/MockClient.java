/**
 * Author: Yuxuan Yang AND Xuhui Gong
 */
package edu.duke.ece568;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MockClient {

  private final String host;
  private final int portNum;
  private final InputStream in;
  private final OutputStream out;
  private final OutputStreamWriter writer;
  private final BufferedReader reader;
  private final Socket socket;

  /**
   * Constructor of Client Class by given portNum and host
   * 
   * @param portNum
   * @param host
   * @throws IOException
   */
  public MockClient(int portNum, String host) throws IOException {
    this.portNum = portNum;
    this.host = host;
    this.socket = new Socket(this.host, this.portNum);
    this.in = socket.getInputStream();
    this.out = socket.getOutputStream();
    this.writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    this.reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
  }

  /**
   * Send a JSON to the server, and it adds a \n automatically
   * 
   * @param msg to be send
   * @throws IOException
   */
  public void sendMsg(String msg) throws IOException {
    this.writer.write(msg + "\n");
    this.writer.flush();
  }

  /**
   * Receive messgae from the server
   * 
   * @return String
   * @throws IOException
   */
  public String recvMsg() throws IOException {

    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    StringBuilder request= new StringBuilder();
    String temp=in.readLine();
    while(temp!=null) {
      request.append(temp);
      temp=in.readLine();
      if(temp.contains("</results>")){
        request.append(temp);
        temp=in.readLine();
        break;
      }
    }
    return request.toString();
  }

  public static void main(String[] args) {
    try {
      MockClient mockClient = new MockClient(12345,"127.0.0.1");
      mockClient.sendMsg("173\n" +
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
              "<create>\n" +
              " <account id=\"123456\" balance=\"1000\"/>\n" +
              " <symbol sym=\"SPY\">\n" +
              " <account id=\"123456\">100000</account>\n" +
              " </symbol>\n" +
              "</create> ");

      // Shut down the output
      mockClient.socket.shutdownOutput();

      String XMLResponse =  mockClient.recvMsg();
      System.out.println(XMLResponse);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
