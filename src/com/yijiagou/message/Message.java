package com.yijiagou.message;

import com.yijiagou.exception.MessageException;

/**
 * Created by wangwei on 17-9-9.
 */
public class Message {
    protected String head;
    protected String body;

    public Message() {

    }

    //head|body
    public Message(String msg) throws MessageException {
        if (msg == null) {
            throw new MessageException("Message package is null");
        }

        String[] msgs = msg.split("\\|");
        if (msgs.length < 2) {
            throw new MessageException("Message package error");
        }
        this.head = msgs[0];
        this.body = msgs[1];

    }

    public String getHead() {
        return this.head;
    }

    public String getBody() {
        return this.body;
    }

    public String toString() {
        StringBuffer pack = new StringBuffer();
        pack.append(this.head);
        pack.append("|");
        pack.append(this.body);
        return pack.toString();
    }
}
