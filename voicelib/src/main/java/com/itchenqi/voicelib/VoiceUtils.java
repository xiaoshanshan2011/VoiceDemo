package com.itchenqi.voicelib;

import android.content.Context;

/**
 * Created by root on 19-3-14.
 */

public class VoiceUtils {
    private static VoiceManager manager;

    //初始化
    public static void init(Context context, String appId, String appKey, String secretKey) {
        manager = new VoiceManager(context, appId, appKey, secretKey);
    }

    //注册监听
    public static void registerListener(VoiceManager.VoiceListener listener) {
        manager.setVoicelistener(listener);
    }

    //取消注册监听
    public static void unRegisterListener(VoiceManager.VoiceListener listener) {
        manager.removeVoicelistener(listener);
    }

    //语音合成
    public static void speak(String content) {
        manager.speak(content);
    }

    //语音合成
    public static void speak(int content) {
        manager.speak(content);
    }

    //释放语音资源
    public static void onDestroy() {
        if (manager != null) {
            manager.onDestroy();
            manager = null;
        }
    }

    public static void test() {
        manager.baiduQuery("你好");
    }
}
