package com.yangjie.bitoperator.test;

import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.enums.LengthUnit;

public class ReadBean {

    @BitsProperty(length = 14, unit = LengthUnit.BYTE)
    private String str = "自己";

    @Override
    public String toString() {
        return "ReadBean{" +
                "str='" + str + '\'' +
                '}';
    }
}
