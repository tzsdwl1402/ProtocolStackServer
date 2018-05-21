package com.yijiagou.config;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangwei on 17-9-6.
 */
public class Configurator {
    private static Map<String, String> configs;
    private static final String INDEXPATH = "index.txt";
//    private static String JPPATH = "/conf/jedis.properties";
//    private static String SERVERPATH = "/conf/ProtocolStackServer.properties";
    private static final String JPPATH = "jedis.properties";
    private static final String SERVERPATH = "ProtocolStackServer.properties";
    private static final String BSERVERPATH= "DownLoadServer.properties";
    static {
        configs = new ConcurrentHashMap();
    }

    public static void init() throws Exception {
        BufferedReader in = null;
        BufferedReader serverIn = null;
        BufferedReader bserverIn = null;
        File file = new File(INDEXPATH);
        String absolutePath = file.getAbsolutePath();
        String path = absolutePath.substring(0, absolutePath.lastIndexOf("/", absolutePath.lastIndexOf("/") - 1));
        try {
//            in = new BufferedReader(new FileReader(path + JPPATH));
//            serverIn = new BufferedReader(new FileReader(path + SERVERPATH));
//            System.out.println(JPPATH);
            in = new BufferedReader(new FileReader(JPPATH));
            serverIn = new BufferedReader(new FileReader(SERVERPATH));
            bserverIn = new BufferedReader(new FileReader(BSERVERPATH));
            String conf = "";
            while ((conf = in.readLine()) != null) {
                String[] pair = conf.split("=");
                if (pair.length != 2) {
                    throw new Exception("config error");
                } else {
                    configs.put(pair[0], pair[1]);
                }
            }
            while ((conf = serverIn.readLine()) != null) {
                String[] pair = conf.split("=");
                if (pair.length != 2) {
                    throw new Exception("config error");
                } else {
                    configs.put(pair[0], pair[1]);
                }
            }

            while ((conf = bserverIn.readLine())!=null){
                String[] pair = conf.split("=");
                if (pair.length!=2){
                    throw new Exception("config error");
                }else {
                    configs.put(pair[0],pair[1]);
                }
            }

            Configurator.getCDNUrl();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public static int getJPWorkMinNum() throws Exception {
        String str = configs.get(ConfigKeyword.JP_WORKMINNUM);
        int workMinNum = 0;
        try {
            if (str != null) {
                workMinNum = Integer.parseInt(str);
            } else {
                throw new Exception("WorkMinNum Not Found");
            }
        } catch (NumberFormatException e) {
            throw new Exception("WorkMinNum format error!");
        }
        return workMinNum;
    }

    public static int getJPWorkMaxNum() throws Exception {
        String str = configs.get(ConfigKeyword.JP_WORKMAXNUM);
        int workMaxNum = 0;
        try {
            if (str != null) {
                workMaxNum = Integer.parseInt(str);
            } else {
                throw new Exception("WorkMaxNum Not Found");
            }
        } catch (NumberFormatException e) {
            throw new Exception("WorkMaxNum format error!");
        }
        return workMaxNum;
    }

    public static String getJPHost() throws Exception {
        String host = configs.get(ConfigKeyword.JP_HOST);
        if (host == null) {
            throw new Exception("Jedis pool host Not Found");
        }
        return host;
    }

    public static int getJPPort() throws Exception {
        String str = configs.get(ConfigKeyword.JP_PORT);
        int port = 0;
        try {
            if (str != null) {
                port = Integer.parseInt(str);
            } else {
                throw new Exception("Jedis pool port Not Found");
            }
        } catch (NumberFormatException e) {
            throw new Exception("Jedis pool port format error!");
        }
        return port;
    }

    public static String getServerHost() throws Exception {
        String host = configs.get(ConfigKeyword.SERVER_HOST);
        if (host == null) {
            throw new Exception("Server host Not Found");
        }
        return host;
    }

    public static int getServerPort() throws Exception {
        String str = configs.get(ConfigKeyword.SERVER_PORT);
        int port = 0;
        try {
            if (str != null) {
                port = Integer.parseInt(str);
            } else {
                throw new Exception("Server port Not Found");
            }
        } catch (NumberFormatException e) {
            throw new Exception("Server port format error!");
        }
        return port;
    }

    public static String getCDNUrl() throws Exception {
        String url = configs.get(ConfigKeyword.SERVER_CDN_URL);
        if (url == null) {
            throw new Exception("CDN url Not Found");
        }
        return url;
    }

    public static int getBserverPort() throws Exception{
        String string =configs.get(ConfigKeyword.BSERVER_PORT);
        int port=0;
        try {
            if (string != null) {
                port = Integer.parseInt(string);
            } else {
                throw new Exception("Bserver Port Not Found");
            }
        }catch (NumberFormatException e){
            throw new Exception("Bserver Port Format Error");
        }
        return port;
    }

    public static String getBserverHost() throws Exception{
        String string = configs.get(ConfigKeyword.BSERVER_HOST);
        if(string==null){
            throw new Exception("Bserver Port Not Found");
        }else {
            return string;
        }
    }
}
