package com.yijiagou.exception;

/**
 * Created by wangwei on 17-9-9.
 */
public class MessageException extends Exception {
    public MessageException(){
        super();
    }

    public MessageException(String msg){
        super(msg);
    }
}
