package com.itchenqi.voicelib.baidutts;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.itchenqi.voicelib.baidutts.control.InitConfig;
import com.itchenqi.voicelib.baidutts.control.MySyntherizer;
import com.itchenqi.voicelib.baidutts.control.NonBlockSyntherizer;
import com.itchenqi.voicelib.baidutts.listener.UiMessageListener;
import com.itchenqi.voicelib.baidutts.util.AutoCheck;
import com.itchenqi.voicelib.baidutts.util.OfflineResource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.itchenqi.voicelib.baidutts.MainHandlerConstant.PRINT;
import static com.itchenqi.voicelib.baidutts.MainHandlerConstant.UI_CHANGE_INPUT_TEXT_SELECTION;
import static com.itchenqi.voicelib.baidutts.MainHandlerConstant.UI_CHANGE_SYNTHES_TEXT_SELECTION;

/**
 * 语音合成管理
 * Created by root on 19-3-13.
 */

public class TtsManager {
    protected Handler mainHandler;
// ================== 初始化参数设置开始 ==========================
    /**
     * 发布时请替换成自己申请的appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     */
    protected String appId = "15742871";

    protected String appKey = "Phx9AGDtPDqCIBxjMxcXwdbO";

    protected String secretKey = "a36m6odyChMrvYe3BylMqATHeSw3j8su";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.MIX;

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_MALE;

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================

    // 主控制类，所有合成控制方法从这个类开始
    protected MySyntherizer synthesizer;
    private Context mContext;

    public void init(Context context, String appId, String appKey, String secretKey) {
        this.appId = appId;
        this.appKey = appKey;
        this.secretKey = secretKey;
        mContext = context;
        mainHandler = new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handle(msg);
            }

        };
        initialTts(); // 初始化TTS引擎
    }

    protected void handle(Message msg) {
        int what = msg.what;
        switch (what) {
            case PRINT:
                break;
            case UI_CHANGE_INPUT_TEXT_SELECTION:
                break;
            case UI_CHANGE_SYNTHES_TEXT_SELECTION:
                break;
            default:
                break;
        }
    }

    /**
     * 初始化引擎，需要的参数均在InitConfig类里
     * <p>
     * DEMO中提供了3个SpeechSynthesizerListener的实现
     * MessageListener 仅仅用log.i记录日志，在logcat中可以看见
     * UiMessageListener 在MessageListener的基础上，对handler发送消息，实现UI的文字更新
     * FileSaveListener 在UiMessageListener的基础上，使用 onSynthesizeDataArrived回调，获取音频流
     */
    protected void initialTts() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler) {

            @Override
            public void onSpeechStart(String utteranceId) {
                super.onSpeechStart(utteranceId);
                onStart(utteranceId);
            }

            @Override
            public void onSpeechFinish(String utteranceId) {
                super.onSpeechFinish(utteranceId);
                onFinish(utteranceId);
            }

            @Override
            public void onError(String utteranceId, SpeechError speechError) {
                super.onError(utteranceId, speechError);
                onErrors(utteranceId, speechError);
            }
        };


        Map<String, String> params = getParams();


        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);

        // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
        // 上线时请删除AutoCheck的调用
        AutoCheck.getInstance(mContext.getApplicationContext()).check(initConfig, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
                        toPrint(message); // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });
        synthesizer = new NonBlockSyntherizer(mContext.getApplicationContext(), initConfig, mainHandler); // 此处可以改为MySyntherizer 了解调用过程
    }

    public void onStart(String utteranceId) {

    }

    public void onFinish(String utteranceId) {

    }

    public void onErrors(String utteranceId, SpeechError speechError) {

    }

    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }

    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(mContext.getApplicationContext(), voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            toPrint("【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }

    protected void toPrint(String str) {
        Message msg = Message.obtain();
        msg.obj = str;
        mainHandler.sendMessage(msg);
    }

    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     */
    public void speak(String content) {
        // 需要合成的文本text的长度不能超过1024个GBK字节。
        // 合成前可以修改参数：
        // Map<String, String> params = getParams();
        // synthesizer.setParams(params);
        synthesizer.speak(content);
    }

    public void stop() {
        synthesizer.stop();
    }

    public void release() {
        synthesizer.release();
    }
}
