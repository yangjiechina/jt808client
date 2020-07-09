package com.yangjie.jt808.web;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSONObject;
import com.yangjie.jt808.bean.Result;
import com.yangjie.jt808.bean.SendResponsePacket;
import com.yangjie.jt808.cache.ExcelCache;
import com.yangjie.jt808.client.TCPClient;
import com.yangjie.jt808.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.yangjie.jt808.bean.Result.*;

@ServerEndpoint("/action/{serverIp}/{serverPort}/{clientCount}/{gpsInterval}/{hasSaveData}")
@RestController
public class WebSocketController implements TCPClient.OnNotifyListener {
    private final Logger log = LoggerFactory.getLogger(WebSocketController.class);
    private Session session;

    private volatile long beginPhone = 0x013500000000L;
    private String cityCode = "粤B";
    private volatile int beginPlateNumber = 11111;
    private volatile int beginLocalPort = 15000;


    private List<TCPClient> clientList = null;

    public static int THREAD_POOL_SIZE = 0;

    public static int gpsCount;
    public static int interval;
    private Pattern pattern = Pattern.compile("[0-9]*");
    private ThreadUtils.SimpleTask<Object> notifyGpsCountTask;
    public static boolean hasSaveData = false;

    @OnOpen
    public void onOpen(Session session) {
        log.info("onOpen");
        this.session = session;
    }

    @OnMessage
    public void onMessage(byte messages, Session session, @PathParam("serverIp") String serverIp, @PathParam("serverPort") int serverPort,
                          @PathParam("clientCount") int clientCount, @PathParam("gpsInterval") Integer interval,
                          @PathParam("hasSaveData") boolean hasSaveData) {
        if (interval == null || interval < 1) {
            interval = 1;
        }
        WebSocketController.interval = interval;
        THREAD_POOL_SIZE = clientCount * 3;
        WebSocketController.hasSaveData = hasSaveData;
        if (0 == messages) {
            gpsCount = 0;
            sendText("\r\n开始执行...");
            releaseAllClients();
            if (clientCount > 0) {

                clientList = new ArrayList<>((int) (clientCount * 1.1));
                //clientMap = new ConcurrentHashMap<>((int) (clientCount * 1.5));
            }
            if (notifyGpsCountTask != null && !notifyGpsCountTask.isDone()) {
                notifyGpsCountTask.cancel();
            }
            notifyGpsCountTask = new ThreadUtils.SimpleTask<Object>() {
                int lastCount = 0;

                @Override
                public Object doInBackground() throws Throwable {
                    if (gpsCount > lastCount) {
                        sendText(new Result(GPS_TYPE, String.valueOf(gpsCount)));
                        lastCount = gpsCount;
                    }
                    return null;
                }

                @Override
                public void onSuccess(Object result) {

                }
            };
            ThreadUtils.executeBySingleAtFixRate(notifyGpsCountTask, 1, TimeUnit.SECONDS);
            while (clientCount > 0) {
                checkPhone();
                TCPClient tcpClient = new TCPClient(beginPhone, cityCode + String.valueOf(beginPlateNumber));
                tcpClient.setOnNotifyListener(this);
                tcpClient.start(beginLocalPort, serverIp, serverPort);
                //clientMap.put(beginPhone, tcpClient);
                clientList.add(tcpClient);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                beginLocalPort++;
                beginPhone++;
                beginPlateNumber++;
                clientCount--;

            }
        } else if (1 == messages) {
            sendText("\r\n结束执行...");
            stop();
        }
        log.info("onMessage");
    }

    private void checkPhone() {
        String string = Long.toHexString(beginPhone);
        if (!pattern.matcher(string).matches()) {
            beginPhone++;
            checkPhone();
        }
    }

    private void saveDataToExcel() {
        if (clientList != null) {
            String directory = System.getProperty("user.dir") + File.separator + "data";

            if (createDirectory(directory)) {
                sendText("创建文件夹失败");
            }
            long currentTimeMillis = System.currentTimeMillis();
            String file = directory.concat(File.separator.concat(String.valueOf(currentTimeMillis).concat(".xlsx")));

            ExcelWriter excelWriter = null;
            try {
                // 这里 指定文件
                excelWriter = EasyExcel.write(file).build();
                // 去调用写入,这里我调用了五次，实际使用时根据数据库分页的总的页数来。这里最终会写到5个sheet里面
                String sheetName = "总览";
                Class<?> cls = TCPClient.class;
                List<?> list = clientList;
                for (int i = 0; i < 2; i++) {
                    // 每次都要创建writeSheet 这里注意必须指定sheetNo 而且sheetName必须不一样。这里注意DemoData.class 可以每次都变，我这里为了方便 所以用的同一个class 实际上可以一直变
                    if (i == 1) {
                        sheetName = "gps数据记录";
                        cls = SendResponsePacket.class;
                        list = ExcelCache.gpsList;
                    }
                    WriteSheet writeSheet = EasyExcel.writerSheet(i, sheetName).head(cls).build();
                    // 分页去数据库查询数据 这里可以去数据库查询每一页的数据
                    excelWriter.write(list, writeSheet);
                    if (i == 0 && ExcelCache.gpsList.isEmpty()) {
                        break;
                    }
                }
            } finally {
                // 千万别忘记finish 会帮忙关闭流
                if (excelWriter != null) {
                    excelWriter.finish();
                }
            }
        }
    }

    private boolean createDirectory(String directory) {
        File file = new File(directory);
        if (file.isDirectory() && !file.exists()) {
            return file.mkdir();
        }
        return false;
    }

    private void releaseAllClients() {
        if (clientList != null) {
            for (TCPClient tcpClient :
                    clientList) {
                tcpClient.release();

            }
        }
        gpsCount = 0;
        beginPhone = 0x013500000000L;
    }

    public void sendText(String message) {
        sendText(Result.OTHER_TYPE, message);
    }

    public void sendText(int type, String message) {
        sendText(new Result(type, message));
    }

    public synchronized void sendText(Result result) {
        if (this.session == null) {
            log.error("发送消息失败 session == null message >>> {}", result.toString());
            return;
        }
        try {
            session.getBasicRemote().sendText(JSONObject.toJSONString(result));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        try {
            releaseAllClients();
            if (WebSocketController.hasSaveData) {
                saveDataToExcel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (clientList != null) {
                clientList.clear();
            }
            if (ExcelCache.gpsList != null) {
                ExcelCache.gpsList.clear();
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        log.info("onClose");
        this.session = null;
        stop();
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.info("onError");
    }

    @Override
    public void onMessage(int type, String message) {
        if (GPS_TYPE == type) {
            gpsCount++;
        } else {
            sendText(type, message);
        }
    }
}
