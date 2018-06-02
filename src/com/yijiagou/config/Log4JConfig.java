package com.yijiagou.config;

import org.apache.log4j.PropertyConfigurator;

import java.io.File;

/**
 * Created by zgl on 17-9-8.
 */
public class Log4JConfig {
    private final static String filename = "../config/log4j.properties";
    private static String path;
    static {
        File file = new File("index.txt");
        String indexpath=file.getAbsolutePath();
        System.out.println(indexpath);
        int a=indexpath.lastIndexOf("/",indexpath.lastIndexOf("/")-1);
        String filepath=indexpath.substring(0,a);
        path=filepath+"/config/"+filename;
//        path="./log4j.properties";
    }

    public static void load(){
        PropertyConfigurator.configure(path);
    }
}

