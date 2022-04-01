package edu.duke.ece568;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientThread implements Runnable {
    private MockClient mockClient;
    private List times;

    public ClientThread(MockClient mockClient, List times) {
        this.mockClient = mockClient;
        this.times = times;
    }

    @Override
    public void run() {
        try {
            // Load message file
            String content = Files.readString(Path.of("XMLSamples/create.xml"), StandardCharsets.US_ASCII);

            // Send message
            mockClient.sendMsg(content);

            // Start counting time
            long start = System.nanoTime();

            // Shut down the output
            mockClient.getSocket().shutdownOutput();

            String XMLResponse =  mockClient.recvMsg();

            // End counting time and record elapsed time
            long end = System.nanoTime();
            long elapsedTime = end - start;

            // Record the time into the times
            this.times.add(elapsedTime);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
