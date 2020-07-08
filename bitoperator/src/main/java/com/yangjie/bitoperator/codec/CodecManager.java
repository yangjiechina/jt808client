package com.yangjie.bitoperator.codec;

import com.yangjie.bitoperator.utils.BeanUtils;

import java.util.concurrent.ConcurrentHashMap;

public class CodecManager {

    private static ConcurrentHashMap<String, Codec> codecCacheMap = new ConcurrentHashMap<>();
    private static CodecManager codecManager = null;

    public static CodecManager getInstance() {
        synchronized (CodecManager.class) {
            if (codecManager == null) {
                codecManager = new CodecManager();
            }
        }
        return codecManager;
    }

    public synchronized Codec getCodec(Class<? extends Codec> cls) {
        String name = cls.getName();
        Codec codec;
        if (!codecCacheMap.containsKey(name)) {
            codec = (Codec) BeanUtils.newBean(cls);
            codecCacheMap.put(name, codec);
        } else {
            codec = codecCacheMap.get(name);
        }
        if (codec == null) {
            throw new NullPointerException("codec");
        }
        return codec;
    }
}
