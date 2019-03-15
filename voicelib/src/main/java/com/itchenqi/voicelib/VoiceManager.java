package com.itchenqi.voicelib;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.tts.client.SpeechError;
import com.itchenqi.voicelib.baiduasr.AsrManager;
import com.itchenqi.voicelib.baidutts.TtsManager;
import com.itchenqi.voicelib.http.HttpGetUtil;
import com.itchenqi.voicelib.http.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 语音管理
 * Created by root on 19-3-13.
 */

public class VoiceManager {
    private TtsManager ttsManager;
    private AsrManager asrManager;
    private Context mContext;

    public VoiceManager(Context mContext, String appId, String appKey, String secretKey) {
        this.mContext = mContext;
        init(appId, appKey, secretKey);
    }

    public void init(String appId, String appKey, String secretKey) {
        //语音识别
        asrManager = new AsrManager() {
            @Override
            public void onAsrFinalResult(String result) {
                super.onAsrFinalResult(result);
                Log.d("baiduQueryonAsr11", result);
                baiduQuery(result);
            }
        };
        //语音合成
        ttsManager = new TtsManager() {
            @Override
            public void onStart(String utteranceId) {
                super.onStart(utteranceId);
            }

            @Override
            public void onFinish(String utteranceId) {
                super.onFinish(utteranceId);
                asrManager.start();
                for (int i = 0; i < listeners.size(); i++) {
                    listeners.get(i).onTtsFinish();
                }
            }

            @Override
            public void onErrors(String utteranceId, SpeechError speechError) {
                super.onErrors(utteranceId, speechError);
                asrManager.start();
            }
        };
        ttsManager.init(mContext, appId, appKey, secretKey);
        asrManager.init(mContext);
        asrManager.start();
    }

    public void speak(String content) {
        if (ttsManager == null)
            return;
        ttsManager.stop();
        asrManager.stop();
        asrManager.cancel();
        ttsManager.speak(content);
    }

    public void speak(int content) {
        try{
            if (ttsManager == null)
                return;
            String mContent = mContext.getResources().getString(content);
            ttsManager.stop();
            asrManager.stop();
            asrManager.cancel();
            ttsManager.speak(mContent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        if (ttsManager != null) {
            ttsManager.release();
            ttsManager = null;
        }
        if (asrManager != null) {
            asrManager.release();
            asrManager = null;
        }
    }

    private List<VoiceListener> listeners = new ArrayList<>();

    public void setVoicelistener(VoiceListener voicelistener) {
        listeners.add(voicelistener);
    }

    public void removeVoicelistener(VoiceListener voicelistener) {
        listeners.remove(voicelistener);
    }

    //回调监听
    public interface VoiceListener {
        void onTtsFinish();

        void onAsrResult(String result);
    }

    /**
     * 阿里智能回复接口
     *
     * @param question
     */
    public void aliQuery(String question) {
        String url = "http://jisuznwd.market.alicloudapi.com/iqa/query?question=" + question;
        new HttpGetUtil() {
            @Override
            public void onSuccess(String result) {
                super.onSuccess(result);
                VoiceUtils.speak(result);
            }

            @Override
            public void onError() {
                super.onError();
                VoiceUtils.speak("主人，小二没听清楚，请再说一次");
            }
        }.post(url);
    }

    /**
     * 百度Unit接口
     *
     * @param question
     */
    public void baiduQuery(final String question) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String log_id = String.valueOf(System.currentTimeMillis());
                    String service_id = "S15028";
                    String session_id = "";
                    // 请求URL
                    String talkUrl = "https://aip.baidubce.com/rpc/2.0/unit/service/chat";
                    // 请求参数
                    String params = "{\"log_id\":\"" + log_id + "\",\"version\":\"2.0\",\"service_id\":\"" + service_id + "\",\"session_id\":\"" + session_id + "\",\"request\":{\"query\":\"" + question + "\",\"user_id\":\"88888\"}}";
                    String accessToken = "24.b23aaa20a6a6bb67c61168b40ea0edfd.2592000.1555151443.282335-15759231";
                    String result = HttpUtil.post(talkUrl, accessToken, "application/json", params);
                    Log.i("baiduQuery", result);
                    //解析json数据
                    if (TextUtils.isEmpty(result)) {
                        aliQuery(question);
                    } else {
                        JSONObject dataJb = new JSONObject(result);
                        JSONObject resultJb = dataJb.getJSONObject("result");
                        JSONArray responseList = resultJb.getJSONArray("response_list");
                        if (responseList != null && responseList.length() > 0) {
                            JSONObject jsonObject = (JSONObject) responseList.get(0);
                            JSONObject quRes = jsonObject.getJSONObject("qu_res");
                            if (quRes != null) {
                                JSONArray candidates = quRes.getJSONArray("candidates");
                                if (candidates != null && candidates.length() > 0) {
                                    JSONObject candidatesBean = (JSONObject) candidates.get(0);
                                    JSONArray slots = candidatesBean.getJSONArray("slots");
                                    if (slots != null && slots.length() > 0) {
                                        JSONObject slotsBean = slots.getJSONObject(0);
                                        if (slotsBean != null) {
                                            String normalized_word = slotsBean.getString("normalized_word");
                                            if (TextUtils.isEmpty(normalized_word) || normalized_word.equals("我不知道应该怎么答复您。")) {
                                                aliQuery(question);
                                            } else {
                                                HandleUnitData(normalized_word);
                                            }
                                        } else {
                                            aliQuery(question);
                                        }
                                    } else {
                                        aliQuery(question);
                                    }
                                } else {
                                    aliQuery(question);
                                }

                            } else {
                                aliQuery(question);
                            }
                        } else {
                            aliQuery(question);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    aliQuery(question);
                }
            }
        }).start();
    }

    /**
     * 处理Unit数据
     *
     * @param say
     */
    private void HandleUnitData(String say) {
        if (say.contains("interaction:")) {
            String code = say.substring(12, say.length());
            Log.d("HandleUnitData", code);
            //处理操作
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).onAsrResult(code);
            }
        } else {
            VoiceUtils.speak(say);
        }
    }
}
