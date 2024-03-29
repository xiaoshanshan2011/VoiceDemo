package com.itchenqi.voicelib.baiduasr;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.itchenqi.voicelib.baiduasr.params.CommonRecogParams;
import com.itchenqi.voicelib.baiduasr.params.OfflineRecogParams;
import com.itchenqi.voicelib.baiduasr.params.OnlineRecogParams;
import com.itchenqi.voicelib.baiduasr.recog.MyRecognizer;
import com.itchenqi.voicelib.baiduasr.recog.listener.IRecogListener;
import com.itchenqi.voicelib.baiduasr.recog.listener.MessageStatusRecogListener;

import java.util.HashMap;
import java.util.Map;

import static com.itchenqi.voicelib.baiduasr.recog.IStatus.STATUS_CORRECT_FINISHED;

/**
 * Created by root on 19-3-13.
 */

public class AsrManager {
    private Context mContext;
    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    protected MyRecognizer myRecognizer;

    /*
     * 本Activity中是否需要调用离线命令词功能。根据此参数，判断是否需要调用SDK的ASR_KWS_LOAD_ENGINE事件
     */
    protected boolean enableOffline = false;

    private static final String TAG = "ActivityAbstractRecog";

    protected Handler handler;

    protected void handleMsg(Message msg) {
        if (msg != null && msg.obj != null) {
            switch (msg.what) {
                case STATUS_CORRECT_FINISHED:
                    //识别完成
                    try{
                        onAsrFinalResult(String.valueOf(msg.obj));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public void onAsrFinalResult(String result){

    }

    public void init(Context context) {
        mContext = context;
        handler = new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handleMsg(msg);
            }

        };
        // 基于DEMO集成第1.1, 1.2, 1.3 步骤 初始化EventManager类并注册自定义输出事件
        // DEMO集成步骤 1.2 新建一个回调类，识别引擎会回调这个类告知重要状态和识别结果
        IRecogListener listener = new MessageStatusRecogListener(handler);
        // DEMO集成步骤 1.1 1.3 初始化：new一个IRecogListener示例 & new 一个 MyRecognizer 示例,并注册输出事件
        myRecognizer = new MyRecognizer(context.getApplicationContext(), listener);
        if (enableOffline) {
            // 基于DEMO集成1.4 加载离线资源步骤(离线时使用)。offlineParams是固定值，复制到您的代码里即可
            Map<String, Object> offlineParams = OfflineRecogParams.fetchOfflineParams();
            myRecognizer.loadOfflineEngine(offlineParams);
        }
    }

    /**
     * 开始录音，点击“开始”按钮后调用。
     * 基于DEMO集成2.1, 2.2 设置识别参数并发送开始事件
     */
    public void start() {
        // DEMO集成步骤2.1 拼接识别参数： 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        //final Map<String, Object> params = fetchParams();
        Map<String, Object> params = new HashMap<>();
        params.put("accept-audio-volume", false);
        params.put("vad.endpoint-timeout", 0);
        // params 也可以根据文档此处手动修改，参数会以json的格式在界面和logcat日志中打印
        Log.i(TAG, "设置的start输入参数：" + params);
        // 复制此段可以自动检测常规错误
        /*(new AutoCheck(mContext.getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        Log.d("handleMessage", message);
                        ; // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }
        }, enableOffline)).checkAsr(params);*/

        // 这里打印出params， 填写至您自己的app中，直接调用下面这行代码即可。
        // DEMO集成步骤2.2 开始识别
        myRecognizer.start(params);
    }

    protected Map<String, Object> fetchParams() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        //  上面的获取是为了生成下面的Map， 自己集成时可以忽略
        CommonRecogParams apiParams = new OnlineRecogParams();
        Map<String, Object> params = apiParams.fetch(sp);
        //  集成时不需要上面的代码，只需要params参数。
        return params;
    }

    /**
     * 开始录音后，手动点击“停止”按钮。
     * SDK会识别不会再识别停止后的录音。
     * 基于DEMO集成4.1 发送停止事件 停止录音
     */
    public void stop() {
        myRecognizer.stop();
    }

    public void release() {
        myRecognizer.release();
    }

    /**
     * 开始录音后，手动点击“取消”按钮。
     * SDK会取消本次识别，回到原始状态。
     * 基于DEMO集成4.2 发送取消事件 取消本次识别
     */
    public void cancel() {
        myRecognizer.cancel();
    }
}
