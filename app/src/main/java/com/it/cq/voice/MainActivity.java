package com.it.cq.voice;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.itchenqi.voicelib.VoiceManager;
import com.itchenqi.voicelib.VoiceUtils;

public class MainActivity extends AppCompatActivity implements VoiceManager.VoiceListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        VoiceUtils.registerListener(this);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // VoiceUtils.speak("你好，我是售酒机器人");
                VoiceUtils.test();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VoiceUtils.unRegisterListener(this);
    }

    @Override
    public void onTtsFinish() {

    }

    @Override
    public void onAsrResult(String result) {
        Log.d("onAsrFinalResult", result);
    }
}
