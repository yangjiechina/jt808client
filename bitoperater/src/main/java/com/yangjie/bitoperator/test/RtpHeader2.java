package com.yangjie.bitoperator.test;

import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.enums.DataFormat;
import com.yangjie.bitoperator.enums.LengthUnit;

import java.util.ArrayList;

public class RtpHeader2 {

    @BitsProperty(length = 0, unit = LengthUnit.BYTE)
    private String str = "哈哈，阳光";


    @BitsProperty(length = 0,unit = LengthUnit.BYTE,dataFormat = DataFormat.LIST)
    private ArrayList<ReadBean> readBean;


    @BitsProperty(length = 2, unit = LengthUnit.BIT)
    private Integer version = 2;

    @BitsProperty(length = 1, unit = LengthUnit.BIT)
    private int padding = 0;

    @BitsProperty(length = 1, unit = LengthUnit.BIT)
    private int xExtend = 0;

    @BitsProperty(length = 3, unit = LengthUnit.BIT)
    private int cc = 0;

    @BitsProperty(length = 1, unit = LengthUnit.BIT)
    private int marker = 0;

    @BitsProperty(length = 1, unit = LengthUnit.BYTE)
    private int pt = 0;

    @BitsProperty(length = 2, unit = LengthUnit.BYTE)
    private int seqNumber = 0;

    @BitsProperty(length = 4, unit = LengthUnit.BYTE)
    private int timestamp = 0;

    @BitsProperty(length = 4, unit = LengthUnit.BYTE)
    private int ssrc = 0xffffffff;



    public RtpHeader2(){

        readBean =  new ArrayList<>();
        readBean.add(new ReadBean());
        readBean.add(new ReadBean());
        readBean.add(new ReadBean());
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public int getxExtend() {
        return xExtend;
    }

    public void setxExtend(int xExtend) {
        this.xExtend = xExtend;
    }

    public int getCc() {
        return cc;
    }

    public void setCc(int cc) {
        this.cc = cc;
    }

    public int getMarker() {
        return marker;
    }

    public void setMarker(int marker) {
        this.marker = marker;
    }

    public int getPt() {
        return pt;
    }

    public void setPt(int pt) {
        this.pt = pt;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int getSsrc() {
        return ssrc;
    }

    public void setSsrc(int ssrc) {
        this.ssrc = ssrc;
    }

}
