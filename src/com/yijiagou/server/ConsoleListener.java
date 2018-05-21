package com.yijiagou.server;

import org.apache.log4j.PropertyConfigurator;

import java.util.Scanner;

/**
 * Created by wangwei on 17-9-8.
 */
public class ConsoleListener implements Runnable {
    Scanner in;

    public ConsoleListener() {
        in = new Scanner(System.in);
    }

    @Override
    public void run() {
        while (true) {
            String comment = in.nextLine();
            if ("log change".equals(comment)) {
                PropertyConfigurator.configure("./log.properties");
            } else {
                System.out.println("comment error!");
            }
        }
    }
}
