package com.yangjie.jt808.bean;

import com.yangjie.bitoperator.annotations.BitsProperty;
import com.yangjie.bitoperator.enums.DataFormat;

public class Register {

    @BitsProperty(length = 2,desc = "省域ID")
    private int provinceId= 11;

    @BitsProperty(length = 2,desc = "市县域ID")
    private int cityId=1101;

    @BitsProperty(length = 5,desc = "制造ID",dataFormat = DataFormat.STRING)
    private String manufacturerId="12345";

    @BitsProperty(length = 8,desc = "终端型号",dataFormat = DataFormat.STRING)
    private String mode = "AB123456";

    @BitsProperty(length = 7,desc = "终端ID",dataFormat = DataFormat.STRING)
    private String deviceId = "A112233";

    @BitsProperty(length = 1,desc = "车牌颜色")
    private int carColor;

    @BitsProperty(desc = "车牌",dataFormat = DataFormat.STRING)
    private String plateNumber = "粤B12345";

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getManufacturerId() {
        return manufacturerId;
    }

    public void setManufacturerId(String manufacturerId) {
        this.manufacturerId = manufacturerId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getCarColor() {
        return carColor;
    }

    public void setCarColor(int carColor) {
        this.carColor = carColor;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }
}
