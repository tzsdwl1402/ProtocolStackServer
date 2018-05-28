package com.yijiagou.tools.jdbctools;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.yijiagou.config.C3p0Configurator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zgl on 17-8-17.
 */
public class ConnPoolUtil {
    private static ComboPooledDataSource dataSource = null;
    private static ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    static {
        map = C3p0Configurator.readConfig();
        Class clazz = null;
        try {
            clazz = Class.forName("com.mchange.v2.c3p0.ComboPooledDataSource");
            dataSource = (ComboPooledDataSource) clazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Method[] methods = clazz.getDeclaredMethods();
        System.out.println(methods.length);
        for (String str1 : map.keySet()) {
            String value = map.get(str1);
            for (Method method : methods) {
                String methodname = "set" + str1.substring(0, 1).toUpperCase() + str1.substring(1, str1.length());
                if (method.getName().equals(methodname)) {
                    Class<?>[] clas = method.getParameterTypes();
                    if (clas[0] == String.class) {
                        try {
                            method.invoke(dataSource, value);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            method.invoke(dataSource, Integer.parseInt(value));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void release(Connection con, PreparedStatement pst, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
        if (pst != null) {
            try {
                pst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (con != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void release(Connection con, PreparedStatement pst) {
        if (pst != null) {
            try {
                pst.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
    public static int updata(String sql, Object... args) throws Exception {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        int number = 0;
        connection = dataSource.getConnection();
        preparedStatement = connection.prepareStatement(sql);
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
        number = preparedStatement.executeUpdate();

        return number;
    }

    public static ResultSet select(String sql,Object... args) throws Exception{
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        connection=dataSource.getConnection();
        preparedStatement=connection.prepareStatement(sql);
//        if(preparedStatement.executeQuery().getRow()==0){
//            System.out.println("result size :"+0);
//            return null;
//        }else {
        for (int i = 0; i < args.length; i++) {
            preparedStatement.setObject(i + 1, args[i]);
        }
            return preparedStatement.executeQuery();
//        }
    }

    public static int selectCount(String sql,Object... args) throws Exception{
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        connection=dataSource.getConnection();
//        preparedStatement=connection.prepareStatement(sql);
        Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);

        ResultSet rs=stmt.executeQuery(sql);
        int count = 0;
        if (rs.next()) {
            count = rs.getInt(1);
        }
        return count;
    }
}
