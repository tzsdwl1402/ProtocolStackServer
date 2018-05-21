package com.yijiagou.message;

import com.yijiagou.exception.MessageException;

/**
 * Created by wangwei on 17-9-9.
 */
public class PSResponse extends Message {

    //oooo|err
    //
    public PSResponse(String msg) throws MessageException {
        super(msg);
    }

    @Override
    public String getHead() {
        return super.getHead();
    }

    @Override
    public String getBody() {
        return super.getBody();
    }
}
