package edu.duke.ece568.tools.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class Logger {
    private java.util.logging.Logger logger;
    private FileHandler fh;

    private static Logger instance;

    private Logger(){
        this.logger = java.util.logging.Logger.getLogger("ServerLog");
        try {
            this.fh = new FileHandler("../server.log");
            this.logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            this.fh.setFormatter(formatter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getSingleton() {
        if (instance == null) {
            synchronized (Logger.class) {
                if (instance == null) {
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    public void write(String log){
        this.logger.info(log);
    }
}
