package com.yangjie.jt808.bean.base;


import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.enums.DataFormat;
import com.yangjie.jt808.codec.CheckCodec;

import java.lang.reflect.ParameterizedType;

public class Message<T> {

    @BitsProperty(length = 2)
    private int id;

    @BitsProperty(dataFormat = DataFormat.BEAN)
    private MessageHeaderProperty messageProperty = new MessageHeaderProperty();

    @BitsProperty(length = 6)
    private long phone;

    @BitsProperty(length = 2)
    private int flowNumber;

    @BitsProperty(dataFormat = DataFormat.BEAN)
    private T body;

    @BitsProperty(length = 1, codec = CheckCodec.class)
    private int checkCode;

    public Message() {
    }


    public Message(int id, int bodyLength, long phone, int flowNumber, T body) {
        this.id = id;
        messageProperty.setLength(bodyLength);
        this.phone = phone;
        this.flowNumber = flowNumber;
        this.body = body;
    }

    public Class<?> getTClass() {
        return (Class<?>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public static void main(String[] args) {
        Message<String> stringMessage = new Message<>();
        stringMessage.getTClass();
    }

    public T getBody() {
        return body;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MessageHeaderProperty getMessageProperty() {
        return messageProperty;
    }

    public void setMessageProperty(MessageHeaderProperty messageProperty) {
        this.messageProperty = messageProperty;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }

    public int getFlowNumber() {
        return flowNumber;
    }

    public void setFlowNumber(int flowNumber) {
        this.flowNumber = flowNumber;
    }

    public void setBody(T body) {
        this.body = body;
    }

    public int getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(int checkCode) {
        this.checkCode = checkCode;
    }

}
