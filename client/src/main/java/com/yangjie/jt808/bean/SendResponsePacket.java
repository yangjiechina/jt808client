package com.yangjie.jt808.bean;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.converters.date.DateStringConverter;

import java.util.Date;

public class SendResponsePacket {
    @ExcelProperty(value = "手机号")
    @ColumnWidth(12)
    private String phone;
    @ExcelProperty(value = "发送数据包")
    @ColumnWidth(90)
    private String sendPacket;
    @ExcelProperty(value = "发送时间",converter = DateStringConverter.class)
    @ColumnWidth(18)
    private Date sendTime;
    @ColumnWidth(40)
    @ExcelProperty(value = "响应数据包")
    private String responsePacket;
    @ExcelProperty(value = "响应时间")
    @ColumnWidth(18)
    private Date responseTime;
    @ExcelProperty(value = "响应码")
    private String status;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSendPacket() {
        return sendPacket;
    }

    public void setSendPacket(String sendPacket) {
        this.sendPacket = sendPacket;
    }

    public String getResponsePacket() {
        return responsePacket;
    }

    public void setResponsePacket(String responsePacket) {
        this.responsePacket = responsePacket;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getSendTime() {
        return sendTime;
    }

    public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    public Date getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }
}
