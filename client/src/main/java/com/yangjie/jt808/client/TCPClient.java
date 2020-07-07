package com.yangjie.jt808.client;

import com.yangjie.bitoperator.BitOperator;
import com.yangjie.bitoperator.converters.ByteArrayToNumberUtils;
import com.yangjie.bitoperator.utils.HexStringUtils;
import com.yangjie.bitoperator.utils.SizeUtils;
import com.yangjie.jt808.bean.*;
import com.yangjie.jt808.bean.base.Message;
import com.yangjie.jt808.constants.MessageIdsConstants;
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
import java.util.concurrent.TimeUnit;

import static com.yangjie.jt808.bean.Result.*;
import static com.yangjie.jt808.constants.MessageIdsConstants.*;


public class TCPClient {

    private Logger log = LoggerFactory.getLogger(TCPClient.class);

    private EventLoopGroup mWorkerGroup;

    private static final int CONNECT_TIME_OUT = 5000;

    private long phone;

    private String plateNumber;

    private OnNotifyListener onNotifyListener;

    private StringBuffer stringBuffer = new StringBuffer(100);

    private ThreadUtils.SimpleTask<Object> task;
    private ThreadUtils.SimpleTask<Object> heartBeatTask;

    public TCPClient(long phone, String plateNumber) {
        this.phone = phone;
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
                    /*    sendMessage(STARTED_SUCCESS_TYPE, buildMessage(Long.toHexString(phone), " 连接服务成功"));
                        log.info("{} 连接服务成功", phone);*/
                        channelFuture.channel().closeFuture().sync();
                    } else {
                        log.info("{} 连接服务失败", phone);
                        sendMessage(STARTED_FAILED_TYPE, buildMessage(Long.toHexString(phone), " 连接服务失败"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("{} 启动client失败 >>> {}", phone, e.getMessage());
                    sendMessage(STARTED_FAILED_TYPE, buildMessage(Long.toHexString(phone), " 启动client失败 port ", String.valueOf(localPort), "\r\n", e.getMessage()));
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
        if (task != null && !task.isDone()) {
            task.cancel();
        }
        if (heartBeatTask != null && !heartBeatTask.isDone()) {
            heartBeatTask.cancel();

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
                Message<PlatformCommonReply> result = (Message<PlatformCommonReply>) o;
                int requestMsgId = result.getBody().getId();
                if (LOCATION == requestMsgId) {
                    //sendMessage(GPS_TYPE, buildMessage(Long.toHexString(phone), " gps响应 "));
                    sendMessage(GPS_TYPE, " gps响应 ");
                }
                String key = buildKey(requestMsgId, result.getBody().getFlowNumber());

                messageManager.put(key, o);
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
            try {
                Thread.sleep(1000 * 10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendHeartBeat();

        }

        public void sendRegister() throws InterruptedException {
            //1.发起注册
            Register register = new Register();
            register.setPlateNumber(plateNumber);
            Message<Register> message = new Message<>(REGISTER, SizeUtils.getObjByteSize(register), phone, flowNumber, register);
            byte[] registerData = BitOperator.encode(message).doEncode().toArray();
            sendPacket(Unpooled.wrappedBuffer(new byte[]{0x07E}, registerData, new byte[]{0x7E}));
            log.info("{} 发起注册 >>> {}", phone, HexStringUtils.toHexString(registerData));
            sendMessage(buildMessage(Long.toHexString(phone), " 发起注册 ", HexStringUtils.toHexString(registerData)));
            String key = String.valueOf(REGISTER_REPLY).concat(String.valueOf(flowNumber));
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
                    sendMessage(buildMessage(Long.toHexString(phone), " 注册成功 token >>>", token));
                    sendAuthentication(token);
                    return;
                }
            }
            log.info("{} 注册失败 result >>>> {}", phone, resultCode == -1 ? "未响应" : String.valueOf(resultCode));
            sendMessage(STARTED_FAILED_TYPE, buildMessage(Long.toHexString(phone), " 注册失败 result >>>>", resultCode == -1 ? "未响应" : String.valueOf(resultCode)));
            //sendRegister();
        }

        public void sendAuthentication(String code) throws InterruptedException {
            Authentication authentication = new Authentication(code);
            Message<Authentication> message = new Message<>(AUTHENTICATION, SizeUtils.getObjByteSize(authentication), phone, flowNumber, authentication);
            byte[] authenticationData = BitOperator.encode(message).doEncode().toArray();
            sendPacket(Unpooled.wrappedBuffer(new byte[]{0x07E}, authenticationData, new byte[]{0x7E}));
            log.info("{} 发送鉴权消息 >>> {}", phone, HexStringUtils.toHexString(authenticationData));
            sendMessage(buildMessage(Long.toHexString(phone), " 发送鉴权消息 >>>>", HexStringUtils.toHexString(authenticationData)));
            String key = buildKey(AUTHENTICATION, flowNumber);
            SyncFuture<?> receive = messageManager.receive(key);
            Object result = receive.get(5, TimeUnit.SECONDS);
            if (result != null) {
                log.info("{} 鉴权成功 ", phone);
                sendMessage(STARTED_SUCCESS_TYPE, buildMessage(Long.toHexString(phone), " 鉴权成功 "));
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
                ThreadUtils.executeByFixed(WebSocketController.THREAD_POOL_SIZE, heartBeatTask);
                sendLocation();

            } else {
                sendMessage(STARTED_FAILED_TYPE, buildMessage(Long.toHexString(phone), " 鉴权失败 "));
                log.info("{} 鉴权失败 ", phone);
                //sendAuthentication(code);
            }
        }

        public void sendLocation() {
            Location location = new Location();
            Message<Location> message = new Message<>(MessageIdsConstants.LOCATION, SizeUtils.getObjByteSize(location), phone, flowNumber, location);
            byte[] locationData = BitOperator.encode(message).doEncode().toArray();
            sendPacket(Unpooled.wrappedBuffer(new byte[]{0x07E}, locationData, new byte[]{0x7E}));
            log.info("{} 发送位置消息 >>> {}", phone, HexStringUtils.toHexString(locationData));
            //sendMessage(buildMessage(Long.toHexString(phone), " 发送位置消息 >>> ", HexStringUtils.toHexString(locationData)));
            flowNumber++;
            try {
                Thread.sleep(1000 * WebSocketController.interval);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendLocation();
        }

        public String buildKey(int msgId, int flowNumber) {
            return String.valueOf(msgId).concat(String.valueOf(flowNumber));
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

    public static void main(String[] args) {
/*        TCPClient tcpClient = new TCPClient();
        tcpClient.start(10000, "127.0.0.1", 7611);
        Scanner scanner = new Scanner(System.in);
        scanner.next();*/
    }

    public static void main1(String[] args) throws ClassNotFoundException {
        //1.发起注册
        Register register = new Register();
        Message<Register> message = new Message<>(MessageIdsConstants.REGISTER, SizeUtils.getObjByteSize(register), 0x013555555555L, 1, register);
        byte[] registerData = BitOperator.encode(message).doEncode().toArray();
        Unpooled.wrappedBuffer(new byte[]{0x07E}, registerData, new byte[]{0x7E});
        System.out.println("发送注册信息 >>> " + HexStringUtils.toHexString(registerData));
        //响应鉴权码
        //发送鉴权码
        sendAuthentication("test_token");
        decodeRegisterReply(new byte[]{(byte) 0x81, 0x00, 0x00, 0x0D, 0x01, 0x35, 0x55, 0x55, 0x55, 0x55, 0x00, 0x00, 0x00, 0x01, 0x00, 0x74, 0x65, 0x73, 0x74, 0x5F, 0x74, 0x6F, 0x6B, 0x65, 0x6E, (byte) 0x8B});
        sendLocation();
    }

    public static void sendAuthentication(String code) {
        Authentication authentication = new Authentication(code);
        Message<Authentication> message = new Message<>(MessageIdsConstants.AUTHENTICATION, SizeUtils.getObjByteSize(authentication), 0x013555555555L, 1, authentication);
        byte[] authenticationData = BitOperator.encode(message).doEncode().toArray();
        System.out.println("发送鉴权消息 >>> " + HexStringUtils.toHexString(authenticationData));
    }

    public static void sendLocation() {
        Location location = new Location();
        Message<Location> message = new Message<>(MessageIdsConstants.LOCATION, SizeUtils.getObjByteSize(location), 0x013555555555L, 1, location);
        byte[] locationData = BitOperator.encode(message).doEncode().toArray();
        System.out.println("发送位置消息 >>> " + HexStringUtils.toHexString(locationData));
    }

    public static void decodePlatformCommonReply(byte[] data) {
        Object o = BitOperator.decode(data, Message.class).genericType(PlatformCommonReply.class).doDecode();
    }

    public static void decodeRegisterReply(byte[] data) {
        Object o = BitOperator.decode(data, Message.class).genericType(RegisterReply.class).doDecode();
        System.out.println("注册包");
    }

}
