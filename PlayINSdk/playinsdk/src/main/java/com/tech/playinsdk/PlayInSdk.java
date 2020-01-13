package com.tech.playinsdk;

import android.os.Build;

import com.tech.playinsdk.http.HttpException;
import com.tech.playinsdk.http.HttpHelper;
import com.tech.playinsdk.listener.HttpListener;
import com.tech.playinsdk.listener.InitListener;
import com.tech.playinsdk.model.ApiService;
import com.tech.playinsdk.model.entity.Advert;
import com.tech.playinsdk.model.entity.Config;
import com.tech.playinsdk.model.entity.PlayInfo;
import com.tech.playinsdk.util.Analyze;
import com.tech.playinsdk.util.Constants;
import com.tech.playinsdk.util.PlayLog;

import org.json.JSONObject;

import java.util.List;

public class PlayInSdk {

    private static PlayInSdk sInstance = new PlayInSdk();
    public static PlayInSdk getInstance() {
        return sInstance;
    }

    private String sdkKey;
    private Config configData;

    public String getApiHost() {
        if (null != configData) {
            return configData.getHost();
        }
        return null;
    }

    /**
     * confirmKey.
     * @param sdkKey
     * @param initListener
     */
    public void confirmKey(final String sdkKey, final InitListener initListener) {
        if (null == sdkKey || sdkKey.isEmpty()) {
            initListener.failure(new Exception("configureWithKey: invalid key"));
            return;
        }
        this.sdkKey = sdkKey;
        ApiService.config(new HttpListener<Config>() {
            @Override
            public void success(final Config cf) {
                String host = cf.getHost();
                if (host == null || host.isEmpty()) {
                    initListener.failure(new Exception("auth: invalid host"));
                    return;
                }
                ApiService.userAuth(cf.getHost(), sdkKey, new HttpListener<String>() {
                    @Override
                    public void success(String sessionKey) {
                        setHttpHelperSessionKey(sessionKey);
                        PlayInSdk.this.configData = cf;
                        initListener.success();
                    }

                    @Override
                    public void failure(HttpException ex) {
                        PlayLog.e("auth: internal didConnectFail: " + ex);
                        initListener.failure(new Exception("auth: internal didConnectFail"));
                    }
                });
            }

            @Override
            public void failure(HttpException ex) {
                PlayLog.e("configureWithKey: internal didConnectFail: " + ex);
                initListener.failure(new Exception("configureWithKey: internal  didConnectFail"));
            }
        });
    }

    /**
     * Check for playable equipment
     * @param adId
     * @param httpListener
     */
    public void confirmPlayableAd(final String adId, final HttpListener<Boolean> httpListener) {
        ApiService.userAvailable(getApiHost(), adId, httpListener);
    }

    /**
     * Get game information
     * @param adId
     * @param httpListener
     */
    public void userActions(String adId, String deviceId, final HttpListener<PlayInfo> httpListener) {
        ApiService.userActionsPlay(getApiHost(), adId, sdkKey, deviceId, httpListener);
        Analyze.getInstance().reset();
    }

    /**
     * Get the game list
     * @param httpListener
     */
    public void userAppKeys(final HttpListener<List<Advert>> httpListener) {
        ApiService.userAppKeys(getApiHost(), httpListener);
    }

    /**
     * data report
     * @param token
     * @param action
     */
    public void report(String token, String action) {
        ApiService.report(getApiHost(), token, action);
        try {
            JSONObject analyzeData = Analyze.getInstance().report();
            analyzeData.put("phone", Build.BRAND + "-" + Build.MODEL);
            analyzeData.put("version", Build.VERSION.RELEASE);
            PlayLog.e("analyzeData =============>  " + analyzeData.toString());
            ApiService.analyze(getApiHost(), token, analyzeData.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setHttpHelperSessionKey(String sessionKey) {
        HttpHelper.obtian().setSessionKey(sessionKey);
    }

    public PlayInSdk setLog(boolean flag) {
        PlayLog.DEBUG = flag;
        return this;
    }

    public PlayInSdk setTest(boolean flag) {
        Constants.TEST = flag;
        return this;
    }
}
