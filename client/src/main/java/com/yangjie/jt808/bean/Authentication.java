package com.yangjie.jt808.bean;


import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.enums.DataFormat;

public class Authentication {

    @BitsProperty(dataFormat = DataFormat.STRING)
    private String code;

    public Authentication(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
