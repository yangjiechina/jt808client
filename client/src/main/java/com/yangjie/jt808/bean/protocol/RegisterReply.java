package com.yangjie.jt808.bean.protocol;

import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.jt808.codec.RegisterReplyCodec;

public class RegisterReply {

    @BitsProperty(length = 2)
    private int flowNumber;

    @BitsProperty(length = 1)
    private int result;

    @BitsProperty(length = 1,codec = RegisterReplyCodec.class)
    private String authenticationCode;

    public int getFlowNumber() {
        return flowNumber;
    }

    public void setFlowNumber(int flowNumber) {
        this.flowNumber = flowNumber;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getAuthenticationCode() {
        return authenticationCode;
    }

    public void setAuthenticationCode(String authenticationCode) {
        this.authenticationCode = authenticationCode;
    }
}
