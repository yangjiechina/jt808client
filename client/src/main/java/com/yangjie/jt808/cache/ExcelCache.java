package com.yangjie.jt808.cache;

import com.yangjie.jt808.bean.SendResponsePacket;

import java.util.ArrayList;
import java.util.List;

public class ExcelCache {

/*    public static List<SendResponsePacket> registerList = new ArrayList<>(1000);

    public static List<SendResponsePacket> authenticationList = new ArrayList<>(1000);*/

    public static volatile List<SendResponsePacket> gpsList = new ArrayList<>(100000);

}
