/**
 * Author: Yuxuan Yang AND Xuhui Gong
 */
package edu.duke.ece568;

import edu.duke.ece568.tools.log.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MockClient {

  private final String host;
  private final int portNum;
  private final InputStream in;
  private final OutputStream out;
  private final OutputStreamWriter writer;
  private final BufferedReader reader;

  public Socket getSocket() {
    return socket;
  }

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
      request.append(temp + "\n");
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

    // Create a time array
    List times = Collections.synchronizedList(new ArrayList<Long>());

    int NUMTHREAD = 100;

    int threadNumCounter = 0;
    try {
      while (threadNumCounter <= NUMTHREAD) {
        // accept tcp connection
        MockClient mockClient = new MockClient(12345,"vcm-24574.vm.duke.edu");


        // create a new thread object
        ClientThread clientSock = new ClientThread(mockClient, times);
        new Thread(clientSock).start();
        threadNumCounter++;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    while (times.size() != NUMTHREAD){}

    for (int i = 0; i < NUMTHREAD; i++){
      System.out.println(times.get(i));
    }

  }

}
