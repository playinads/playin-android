package com.tech.playinsdk;

import com.tech.playinsdk.http.HttpException;
import com.tech.playinsdk.http.HttpHelper;
import com.tech.playinsdk.listener.HttpListener;
import com.tech.playinsdk.listener.InitListener;
import com.tech.playinsdk.model.ApiService;
import com.tech.playinsdk.model.entity.Advert;
import com.tech.playinsdk.model.entity.Config;
import com.tech.playinsdk.model.entity.PlayInfo;
import com.tech.playinsdk.util.PlayLog;

import java.util.List;

public class PlayInSdk {

    private static PlayInSdk sInstance = new PlayInSdk();
    public static PlayInSdk getInstance() {
        return sInstance;
    }

    private String sdkKey;
    private Config configData;

    public void setDebug(boolean debug) {
        PlayLog.DEBUG = debug;
    }

    public String getApiHost() {
        if (null != configData) {
            return configData.getHost();
        }
        return null;
    }

    /**
     * configWithKey.
     * @param sdkKey
     * @param initListener
     */
    public void configWithKey(final String sdkKey, final InitListener initListener) {
        if (null == sdkKey || sdkKey.isEmpty()) {
            initListener.failure(new Exception("[PlayIn] configureWithKey: invalid key"));
            return;
        }
        this.sdkKey = sdkKey;
        ApiService.config(new HttpListener<Config>() {
            @Override
            public void success(final Config cf) {
                String host = cf.getHost();
                if (host == null || host.isEmpty()) {
                    initListener.failure(new Exception("[PlayIn] auth: invalid host"));
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
                        PlayLog.e("[PlayIn] auth: internal onPlayError: " + ex);
                        initListener.failure(new Exception("[PlayIn] auth: internal onPlayError"));
                    }
                });
            }

            @Override
            public void failure(HttpException ex) {
                PlayLog.e("[PlayIn] configureWithKey: internal onPlayError: " + ex);
                initListener.failure(new Exception("[PlayIn] configureWithKey: internal  onPlayError"));
            }
        });
    }

    /**
     * Check for playable equipment
     * @param adId
     * @param httpListener
     */
    public void checkAvailable(final String adId, final HttpListener<Boolean> httpListener) {
        ApiService.userAvailable(getApiHost(), adId, httpListener);
    }

    /**
     * Get game information
     * @param adId
     * @param httpListener
     */
    public void userActions(String adId, final HttpListener<PlayInfo> httpListener) {
        ApiService.userActionsPlay(getApiHost(), adId, sdkKey, "", httpListener);
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
    }

    private void setHttpHelperSessionKey(String sessionKey) {
        HttpHelper.obtian().setSessionKey(sessionKey);
    }
}
