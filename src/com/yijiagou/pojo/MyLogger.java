package com.yijiagou.pojo;


import com.yijiagou.config.Log4JConfig;

import java.util.Scanner;

/**
 * Created by zgl on 17-9-8.
 */
public class MyLogger {

    public MyLogger(){

        new Thread(new logListener()).start();
    }
}

class logListener implements Runnable {
    private Scanner scanner;
    @Override
    public void run() {
        while (true) {
            scanner = new Scanner(System.in);
            String str = scanner.next();
            if (str.equals("logchange")) {
                Log4JConfig.load();
                System.out.println("logchange success");
            }else {
                System.out.println("输入参数有误");
            }
        }
    }
}


