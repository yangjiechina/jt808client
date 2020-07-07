package com.yangjie.jt808.bean;


import com.yangjie.bitoperator.annotations.BitsProperty;

public class PlatformCommonReply {

    @BitsProperty(length = 2)
    private int flowNumber;

    @BitsProperty(length = 2)
    private int id;

    @BitsProperty(length = 1)
    private int result;

    public int getFlowNumber() {
        return flowNumber;
    }

    public void setFlowNumber(int flowNumber) {
        this.flowNumber = flowNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
