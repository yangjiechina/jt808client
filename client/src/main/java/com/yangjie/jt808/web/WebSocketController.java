package com.yangjie.jt808.web;

import com.alibaba.fastjson.JSONObject;
import com.yangjie.jt808.bean.Result;
import com.yangjie.jt808.client.TCPClient;
import com.yangjie.jt808.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.yangjie.jt808.bean.Result.*;

@ServerEndpoint("/action/{serverIp}/{serverPort}/{clientCount}/{gpsInterval}")
@RestController
public class WebSocketController implements TCPClient.OnNotifyListener {
    private final Logger log = LoggerFactory.getLogger(WebSocketController.class);
    private Session session;

    private volatile long beginPhone = 0x013500000000L;
    private String cityCode = "粤B";
    private volatile int beginPlateNumber = 11111;
    private volatile int beginLocalPort = 15000;

    private Map<Long, TCPClient> clientMap = null;

    public static int targetCount = 0;
    public static int THREAD_POOL_SIZE = 0;

    public static int gpsCount;
    public static int interval;
    private Pattern pattern = Pattern.compile("[0-9]*");

    @OnOpen
    public void onOpen(Session session) {
        log.info("onOpen");
        this.session = session;
    }

    @OnMessage
    public void onMessage(byte messages, Session session, @PathParam("serverIp") String serverIp, @PathParam("serverPort") int serverPort,
                          @PathParam("clientCount") int clientCount, @PathParam("gpsInterval") Integer interval) {
        if (interval == null || interval < 1) {
            interval = 1;
        }
        WebSocketController.interval = interval;
        THREAD_POOL_SIZE = clientCount * 2;
        if (0 == messages) {
            gpsCount = 0;
            sendText("开始执行...");
            if (clientCount > 0) {
                clientMap = new ConcurrentHashMap<>((int) (clientCount * 1.5));
            }
            ThreadUtils.executeBySingleAtFixRate(new ThreadUtils.SimpleTask<Object>() {
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
            }, 1, TimeUnit.SECONDS);
            while (clientCount > 0) {
                checkPhone();
                TCPClient tcpClient = new TCPClient(beginPhone, cityCode + String.valueOf(beginPlateNumber));
                tcpClient.setOnNotifyListener(this);
                tcpClient.start(beginLocalPort, serverIp, serverPort);
                clientMap.put(beginPhone, tcpClient);
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
            sendText("结束执行...");
            releaseAllClients();
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

    private void releaseAllClients() {
        if (clientMap != null) {
            Iterator<Map.Entry<Long, TCPClient>> iterator = clientMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, TCPClient> entry = iterator.next();
                TCPClient tcpClient = entry.getValue();
                tcpClient.release();
                iterator.remove();
            }
        }
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

    @OnClose
    public void onClose(Session session) {
        log.info("onClose");
        this.session = null;
        releaseAllClients();
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
