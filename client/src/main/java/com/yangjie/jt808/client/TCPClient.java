package com.yangjie.jt808.client;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.yangjie.bitoperator.BitOperator;
import com.yangjie.bitoperator.converters.ByteArrayToNumberUtils;
import com.yangjie.bitoperator.utils.HexStringUtils;
import com.yangjie.bitoperator.utils.SizeUtils;
import com.yangjie.jt808.bean.*;
import com.yangjie.jt808.bean.protocol.base.Message;
import com.yangjie.jt808.bean.protocol.*;
import com.yangjie.jt808.cache.ExcelCache;
import com.yangjie.jt808.constants.MessageIdsConstants;
import com.yangjie.jt808.constants.ResultStatusConstants;
import com.yangjie.jt808.message.MessageManager;
import com.yangjie.jt808.message.SyncFuture;
import com.yangjie.jt808.utils.ThreadUtils;
import com.yangjie.jt808.web.WebSocketController;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.yangjie.jt808.bean.Result.*;
import static com.yangjie.jt808.constants.MessageIdsConstants.*;


public class TCPClient {

    @ExcelIgnore
    private Logger log = LoggerFactory.getLogger(TCPClient.class);
    @ExcelIgnore
    private EventLoopGroup mWorkerGroup;
    @ExcelIgnore
    private static final int CONNECT_TIME_OUT = 5000;
    @ExcelIgnore
    private OnNotifyListener onNotifyListener;
    @ExcelIgnore
    private StringBuffer stringBuffer = new StringBuffer(100);
    @ExcelIgnore
    private ThreadUtils.SimpleTask<Object> task;
    @ExcelIgnore
    private ThreadUtils.SimpleTask<Object> heartBeatTask;
    @ExcelIgnore
    private ThreadUtils.SimpleTask<Object> uploadPositionTask;

    @ExcelIgnore
    private long phone;
    @ExcelProperty(value = "手机号")
    @ColumnWidth(12)
    private String hexPhone;
    @ExcelProperty(value = "车牌号")
    private String plateNumber;
    @ExcelProperty(value = "注册成功")
    private String registeredSuccess = "否";
    @ExcelProperty(value = "鉴权成功")
    private String authenticationSuccess = "否";
    @ExcelProperty(value = "鉴权码")
    private String authenticationCode;
    @ExcelProperty(value = "gps发送总数")
    private int gpsSendCount;
    @ExcelProperty(value = "gps成功数")
    private int gpsSuccessCount;
    @ExcelProperty(value = "gps失败数")
    private int gpsFailedCount;


    public TCPClient(long phone, String plateNumber) {
        this.phone = phone;
        this.hexPhone = Long.toHexString(phone);
        this.plateNumber = plateNumber;
    }

    public interface OnNotifyListener {
        void onMessage(int type, String message);
    }

    public void setOnNotifyListener(OnNotifyListener onNotifyListener) {
        this.onNotifyListener = onNotifyListener;
    }

    public void start(int localPort, String serverIp, int serverPort) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                mWorkerGroup = new NioEventLoopGroup();
                Bootstrap clientBootstrap = new Bootstrap();
                clientBootstrap.group(mWorkerGroup).
                        channel(NioSocketChannel.class).
                        remoteAddress(new InetSocketAddress(serverIp, serverPort))
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ChannelPipeline pipeline = socketChannel.pipeline();
                                pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.wrappedBuffer(new byte[]{0x7e}), Unpooled.wrappedBuffer(new byte[]{0x7e, 0x7e})));
                                //(byte) 0x7e
                                pipeline.addLast(new ClientHandler());
                            }
                        });//

                try {
                    clientBootstrap.bind(localPort);
                    ChannelFuture channelFuture = clientBootstrap.connect();
                    boolean connectStatus = channelFuture.await(CONNECT_TIME_OUT, TimeUnit.MILLISECONDS);
                    if (connectStatus) {
                    /*    sendMessage(STARTED_SUCCESS_TYPE, buildMessage(hexPhone, " 连接服务成功"));
                        log.info("{} 连接服务成功", phone);*/
                        channelFuture.channel().closeFuture().sync();
                    } else {
                        log.info("{} 连接服务失败", phone);
                        sendMessage(STARTED_FAILED_TYPE, buildMessage(hexPhone, " 连接服务失败"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("{} 启动client失败 >>> {}", phone, e.getMessage());
                    sendMessage(STARTED_FAILED_TYPE, buildMessage(hexPhone, " 启动client失败 port ", String.valueOf(localPort), "\r\n", e.getMessage()));
                } finally {
                    unBind();
                }

            }
        }).start();
    }

    private void unBind() {
        if (mWorkerGroup != null && !mWorkerGroup.isShutdown()) {
            mWorkerGroup.shutdownGracefully();
        }
    }


    public void stop() {
        unBind();
    }

    public void release() {
        stop();
        cancelTasks();
    }

    public void cancelTasks() {
        if (task != null) {
            task.cancel();
        }
        if (heartBeatTask != null) {
            heartBeatTask.cancel();
        }
        if (uploadPositionTask != null) {
            uploadPositionTask.cancel();
        }
    }

    public void sendMessage(int type, String message) {
        if (onNotifyListener != null) {
            onNotifyListener.onMessage(type, message);
        }
    }

    public void sendMessage(String message) {
        sendMessage(Result.OTHER_TYPE, message);
    }

    public String buildMessage(String... args) {
        stringBuffer.delete(0, stringBuffer.length());
        stringBuffer.append("\r\n");
        for (int i = 0; i < args.length; i++) {
            stringBuffer.append(args[i]);
        }
        return stringBuffer.toString();
    }


    private class ClientHandler extends ChannelInboundHandlerAdapter {
        Channel mChannel = null;
        private boolean mIsConnected;
        private final Logger log = LoggerFactory.getLogger(ClientHandler.class);
        private int flowNumber = 1;
        private MessageManager messageManager = MessageManager.INSTANCE;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                ByteBuf buf = (ByteBuf) msg;

                int length = buf.readableBytes();
                if (length <= 0) {
                    return;
                }
                byte[] data = new byte[length];
                buf.readBytes(data);
                log.info("接收到消息 >>> {}", HexStringUtils.toHexString(data));
                short msgId = ByteArrayToNumberUtils.toShort(data[0], data[1]);
                switch (msgId) {
                    case (short) PLATFORM_COMMON_REPLY:
                        decodePlatformCommonReply(data);
                        break;
                    case (short) REGISTER_REPLY:
                        decodeRegisterReply(data);
                        break;
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }

        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
            mIsConnected = true;
            mChannel = ctx.channel();
            task = new ThreadUtils.SimpleTask<Object>() {
                @Override
                public Object doInBackground() throws Throwable {
                    Thread.sleep(2000);
                    sendRegister();
                    return null;
                }

                @Override
                public void onSuccess(Object result) {

                }
            };
            ThreadUtils.executeByFixed(WebSocketController.THREAD_POOL_SIZE, task);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            mIsConnected = false;
            cancelTasks();
        }

        public void decodePlatformCommonReply(byte[] data) {
            Object o = BitOperator.decode(data, Message.class).genericType(PlatformCommonReply.class).doDecode();
            if (o != null) {
                Message<PlatformCommonReply> message = (Message<PlatformCommonReply>) o;
                message.setResponseHexString(HexStringUtils.toHexString(data));
                PlatformCommonReply platformCommonReply = message.getBody();
                //int result = platformCommonReply.getResult();
                int requestMsgId = platformCommonReply.getId();
                String key = buildKey(requestMsgId, platformCommonReply.getFlowNumber());
                messageManager.put(key, message);
            }
        }

        public void decodeRegisterReply(byte[] data) {
            Object o = BitOperator.decode(data, Message.class).genericType(RegisterReply.class).doDecode();
            if (o != null) {
                Message<RegisterReply> result = (Message<RegisterReply>) o;
                String key = buildKey(REGISTER_REPLY, result.getBody().getFlowNumber());
                messageManager.put(key, o);
            }
        }


        public void sendHeartBeat() {
            Message heartBeat = new Message(HEARTBEAT, 0, phone, flowNumber, null);
            byte[] heartBeatData = BitOperator.encode(heartBeat).doEncode().toArray();
            sendPacket(Unpooled.wrappedBuffer(new byte[]{0x07E}, heartBeatData, new byte[]{0x7E}));
            log.info("{} 发送心跳 >>> {}", phone, HexStringUtils.toHexString(heartBeatData));
            flowNumber++;
        }

        public void sendRegister() throws InterruptedException {
            //1.发起注册
            Register register = new Register();
            register.setPlateNumber(plateNumber);
            Message<Register> message = new Message<>(REGISTER, SizeUtils.getObjByteSize(register), phone, flowNumber, register);
            byte[] registerData = BitOperator.encode(message).doEncode().toArray();
            sendPacket(Unpooled.wrappedBuffer(new byte[]{0x07E}, registerData, new byte[]{0x7E}));
            log.info("{} 发起注册 >>> {}", phone, HexStringUtils.toHexString(registerData));
            sendMessage(buildMessage(hexPhone, " 发起注册 ", HexStringUtils.toHexString(registerData)));
            String key = buildKey(REGISTER_REPLY, flowNumber);
            SyncFuture<?> receive = messageManager.receive(key);
            Object result = receive.get(5, TimeUnit.SECONDS);
            int resultCode = -1;
            if (result != null) {
                Message<RegisterReply> registerReplyMessage = (Message<RegisterReply>) result;
                resultCode = registerReplyMessage.getBody().getResult();
                if (resultCode == 0) {
                    flowNumber++;
                    String token = registerReplyMessage.getBody().getAuthenticationCode();
                    log.info("{} 注册成功 token >>> {}", phone, token);
                    registeredSuccess = "是";
                    authenticationCode = token;
                    sendMessage(buildMessage(hexPhone, " 注册成功 token >>>", token));
                    sendAuthentication(token);
                    return;
                }
            }
            log.info("{} 注册失败 result >>>> {}", phone, resultCode == -1 ? "未响应" : String.valueOf(resultCode));
            sendMessage(STARTED_FAILED_TYPE, buildMessage(hexPhone, " 注册失败 result >>>>", resultCode == -1 ? "未响应" : String.valueOf(resultCode)));
            //sendRegister();
        }

        public void sendAuthentication(String code) throws InterruptedException {
            Authentication authentication = new Authentication(code);
            Message<Authentication> message = new Message<>(AUTHENTICATION, SizeUtils.getObjByteSize(authentication), phone, flowNumber, authentication);
            byte[] authenticationData = BitOperator.encode(message).doEncode().toArray();
            sendPacket(Unpooled.wrappedBuffer(new byte[]{0x07E}, authenticationData, new byte[]{0x7E}));
            log.info("{} 发送鉴权消息 >>> {}", phone, HexStringUtils.toHexString(authenticationData));
            sendMessage(buildMessage(hexPhone, " 发送鉴权消息 >>>>", HexStringUtils.toHexString(authenticationData)));
            String key = buildKey(AUTHENTICATION, flowNumber);
            SyncFuture<?> receive = messageManager.receive(key);
            Object result = receive.get(5, TimeUnit.SECONDS);
            if (result != null) {
                log.info("{} 鉴权成功 ", phone);
                authenticationSuccess = "是";
                sendMessage(STARTED_SUCCESS_TYPE, buildMessage(hexPhone, " 鉴权成功 "));
                flowNumber++;
                heartBeatTask = new ThreadUtils.SimpleTask<Object>() {
                    @Override
                    public Object doInBackground() throws Throwable {
                        sendHeartBeat();
                        return null;
                    }

                    @Override
                    public void onSuccess(Object result) {

                    }
                };
                uploadPositionTask = new ThreadUtils.SimpleTask<Object>() {
                    @Override
                    public Object doInBackground() throws Throwable {
                        sendLocation();
                        return null;
                    }

                    @Override
                    public void onSuccess(Object result) {

                    }
                };
                ThreadUtils.executeByFixedAtFixRate(WebSocketController.THREAD_POOL_SIZE, heartBeatTask, 10, TimeUnit.SECONDS);
                ThreadUtils.executeByFixedAtFixRate(WebSocketController.THREAD_POOL_SIZE, uploadPositionTask, WebSocketController.interval, TimeUnit.SECONDS);

            } else {
                sendMessage(STARTED_FAILED_TYPE, buildMessage(hexPhone, " 鉴权失败 "));
                log.info("{} 鉴权失败 ", phone);
                //sendAuthentication(code);
            }
        }

        public void sendLocation() throws InterruptedException {
            Location location = new Location();

            Message<Location> message = new Message<>(LOCATION, SizeUtils.getObjByteSize(location), phone, flowNumber, location);
            byte[] locationData = BitOperator.encode(message).doEncode().toArray();
            sendPacket(Unpooled.wrappedBuffer(new byte[]{0x07E}, locationData, new byte[]{0x7E}));
            Date sendDate = new Date();
            String hexMessage = HexStringUtils.toHexString(locationData);
            log.info("{} 发送位置消息 >>> {}", phone, hexMessage);
            SendResponsePacket sendResponsePacket = null;
            if (WebSocketController.hasSaveData) {
                sendResponsePacket = new SendResponsePacket();
                sendResponsePacket.setPhone(hexPhone);
                sendResponsePacket.setSendPacket(hexMessage);
                sendResponsePacket.setSendTime(sendDate);
            }

            String key = buildKey(LOCATION, flowNumber);
            SyncFuture<?> receive = messageManager.receive(key);
            Object result = receive.get(WebSocketController.interval, TimeUnit.SECONDS);
            flowNumber++;
            gpsSendCount++;


            int status = -1;
            if (result != null) {
                Message<PlatformCommonReply> messageResult = (Message<PlatformCommonReply>) result;
                status = messageResult.getBody().getResult();
                if (WebSocketController.hasSaveData && sendResponsePacket != null) {
                    Date responseDate = new Date();
                    sendResponsePacket.setResponseTime(responseDate);
                    sendResponsePacket.setResponsePacket(messageResult.getResponseHexString());
                    sendResponsePacket.setStatus(String.valueOf(status));
                }
                if (ResultStatusConstants.SUCCESS == status) {
                    sendMessage(GPS_TYPE, " success ");
                    gpsSuccessCount++;
                }
            }
            if (ResultStatusConstants.SUCCESS != status) {
                gpsFailedCount++;
            }
            if (WebSocketController.hasSaveData && sendResponsePacket != null) {
                ExcelCache.gpsList.add(sendResponsePacket);
            }


            //sendMessage(buildMessage(hexPhone, " 发送位置消息 >>> ", HexStringUtils.toHexString(locationData)));

        }


        public String buildKey(int msgId, int flowNumber) {
            return String.valueOf(phone).concat(String.valueOf(msgId)).concat(String.valueOf(flowNumber));
        }

        private void sendPacket(ByteBuf byteBuf) {
            if (mChannel != null && mChannel.isActive()) {
                mChannel.writeAndFlush(byteBuf);
            }
        }

        private void sendPacket(byte[] data) {
            if (mChannel != null && mChannel.isActive()) {
                mChannel.writeAndFlush(Unpooled.wrappedBuffer(data));
            }
        }
    }

    public String getHexPhone() {
        return hexPhone;
    }

    public void setHexPhone(String hexPhone) {
        this.hexPhone = hexPhone;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getRegisteredSuccess() {
        return registeredSuccess;
    }

    public void setRegisteredSuccess(String registeredSuccess) {
        this.registeredSuccess = registeredSuccess;
    }

    public String getAuthenticationSuccess() {
        return authenticationSuccess;
    }

    public void setAuthenticationSuccess(String authenticationSuccess) {
        this.authenticationSuccess = authenticationSuccess;
    }

    public String getAuthenticationCode() {
        return authenticationCode;
    }

    public void setAuthenticationCode(String authenticationCode) {
        this.authenticationCode = authenticationCode;
    }

    public int getGpsSendCount() {
        return gpsSendCount;
    }

    public void setGpsSendCount(int gpsSendCount) {
        this.gpsSendCount = gpsSendCount;
    }

    public int getGpsSuccessCount() {
        return gpsSuccessCount;
    }

    public void setGpsSuccessCount(int gpsSuccessCount) {
        this.gpsSuccessCount = gpsSuccessCount;
    }

    public int getGpsFailedCount() {
        return gpsFailedCount;
    }

    public void setGpsFailedCount(int gpsFailedCount) {
        this.gpsFailedCount = gpsFailedCount;
    }
}
