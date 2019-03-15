package com.it.cq.voice;

import android.app.Application;

import com.itchenqi.voicelib.VoiceUtils;


/**
 * Created by root on 19-3-13.
 */

public class MyApp extends Application {
    protected String appId = "15742871";

    protected String appKey = "Phx9AGDtPDqCIBxjMxcXwdbO";

    protected String secretKey = "a36m6odyChMrvYe3BylMqATHeSw3j8su";

    @Override
    public void onCreate() {
        super.onCreate();
        VoiceUtils.init(this, appId, appKey, secretKey);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        VoiceUtils.onDestroy();
    }
}
