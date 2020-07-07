package com.yangjie.bitoperator.test;

import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.enums.DataFormat;
import com.yangjie.bitoperator.enums.LengthUnit;

import java.util.List;

public class RtpHeader {

    @BitsProperty(length = 1,unit = LengthUnit.BYTE,dataFormat = DataFormat.BEAN)
    private ReadBean readBean;

    @BitsProperty(length = 3,unit = LengthUnit.BYTE,dataFormat = DataFormat.LIST)
    private List<ReadBean> readBeanList;


    @BitsProperty(length = 2, unit = LengthUnit.BIT)
    private Integer version = 0;

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
    private int ssrc;

    @BitsProperty(length = 1, unit = LengthUnit.BYTE)
    private int nameLength;

    @BitsProperty(length = 0, unit = LengthUnit.BYTE)
    private String name;



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

    @Override
    public String toString() {
        return "RtpHeader{" +
                "readBean=" + readBean +
                ", readBeanList=" + readBeanList +
                ", version=" + version +
                ", padding=" + padding +
                ", xExtend=" + xExtend +
                ", cc=" + cc +
                ", marker=" + marker +
                ", pt=" + pt +
                ", seqNumber=" + seqNumber +
                ", timestamp=" + timestamp +
                ", ssrc=" + ssrc +
                ", nameLength=" + nameLength +
                ", name='" + name + '\'' +
                '}';
    }
}
