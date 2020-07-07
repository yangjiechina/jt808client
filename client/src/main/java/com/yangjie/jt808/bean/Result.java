package com.yangjie.jt808.bean;

public class Result {

    public static final int STARTED_SUCCESS_TYPE = 1;
    public static final int STARTED_FAILED_TYPE = 2;
    public static final int OTHER_TYPE = 3;
    public static final int GPS_TYPE = 4;

    public Result() {
    }

    public Result(String msg) {
        this.msg = msg;
    }

    public Result(int type, String msg) {
        this.type = type;
        this.msg = msg;
    }

    private int type = OTHER_TYPE;

    private String msg;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
