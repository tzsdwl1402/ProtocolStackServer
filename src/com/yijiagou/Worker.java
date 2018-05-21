package com.yijiagou;

import com.yijiagou.config.Configurator;
import com.yijiagou.server.ConsoleListener;
import com.yijiagou.server.PSServer;

import java.util.Scanner;

/**
 * Created by wangwei on 17-7-28.
 */
public class Worker {
    public static void main(String[] args){
        PSServer psServer = null;
        try {
//            Thread listener = new Thread(new ConsoleListener());
//            listener.start();
            psServer = PSServer.newInstance();
            psServer.bind(Configurator.getServerHost(),Configurator.getServerPort());
            psServer.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
//class Listener implements Runnable{
//    private Scanner in;
//
//    public Listener(){
//        in = new Scanner(System.in);
//    }
//
//    @Override
//    public void run() {
//        while (true){
//            String command = in.nextLine();
//            if(command.equalsIgnoreCase("exit")){
//
//            }
//        }
//    }
//}