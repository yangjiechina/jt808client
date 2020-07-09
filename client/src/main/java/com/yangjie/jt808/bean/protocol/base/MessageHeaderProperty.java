package com.yangjie.jt808.bean.protocol.base;

import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.codec.BooleanCodec;
import com.yangjie.bitoperator.enums.DataFormat;
import com.yangjie.bitoperator.enums.LengthUnit;

public class MessageHeaderProperty {

    @BitsProperty(length = 2,unit = LengthUnit.BIT)
    private byte retain;

    @BitsProperty(length = 1,unit = LengthUnit.BIT,dataFormat = DataFormat.BOOLEAN,equals = 0x0,codec = BooleanCodec.class)
    private boolean hasSubPacket;


    @BitsProperty(length = 3,unit = LengthUnit.BIT)
    private int encryptionMode;

    @BitsProperty(length = 10,unit = LengthUnit.BIT)
    private int length;



    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getEncryptionMode() {
        return encryptionMode;
    }

    public void setEncryptionMode(int encryptionMode) {
        this.encryptionMode = encryptionMode;
    }

    public boolean isHasSubPacket() {
        return hasSubPacket;
    }

    public void setHasSubPacket(boolean hasSubPacket) {
        this.hasSubPacket = hasSubPacket;
    }

    public byte getRetain() {
        return retain;
    }

    public void setRetain(byte retain) {
        this.retain = retain;
    }
}
