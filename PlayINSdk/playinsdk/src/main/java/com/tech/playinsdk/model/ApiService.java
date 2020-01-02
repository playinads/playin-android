package com.tech.playinsdk.model;

import com.tech.playinsdk.model.entity.Advert;
import com.tech.playinsdk.model.entity.Config;
import com.tech.playinsdk.model.entity.PlayInfo;
import com.tech.playinsdk.http.HttpException;
import com.tech.playinsdk.http.HttpHelper;
import com.tech.playinsdk.listener.HttpListener;
import com.tech.playinsdk.util.Constants;
import com.tech.playinsdk.util.PlayLog;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiService {

    public static void config(final HttpListener<Config> listener) {
        HttpHelper.obtian().doGet(Constants.getConfigHost(), null, new HttpListener<JSONObject>() {
            @Override
            public void success(JSONObject result) {
                String host = result.optString("host");
                String ip = result.optString("ip");
                listener.success(new Config(host, ip));
            }

            @Override
            public void failure(HttpException e) {
                listener.failure(e);
            }
        });
    }

    /**
     * userAuth
     * @param host
     * @param sdkKey
     * @param listener
     */
    public static void userAuth(String host, String sdkKey, final HttpListener<String> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("sdk_key", sdkKey);
        HttpHelper.obtian().doPost(host + "/user/auth", params,
                new HttpListener<JSONObject>() {
            @Override
            public void success(JSONObject result) {
                listener.success(result.optString("session_key"));
            }

            @Override
            public void failure(HttpException e) {
                listener.failure(e);
            }
        });
    }

    /**
     * userAvailable
     * @param host
     * @param adId
     * @param listener
     */
    public static void userAvailable(String host, String adId, final HttpListener<Boolean> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("ad_id", adId);
        params.put("os_type", Constants.OS_TYPE);
        HttpHelper.obtian().doPost(host + "/user/available", params,
                new HttpListener<JSONObject>() {
                    @Override
                    public void success(JSONObject result) {
                        listener.success(true);
                    }

                    @Override
                    public void failure(HttpException e) {
                        listener.failure(e);
                    }
                });
    }

    /**
     * userActionsPlay.
     * @param host
     * @param adId
     * @param sdkKey
     * @param deviceId
     * @param listener
     */
    public static void userActionsPlay(String host, String adId, String sdkKey,
                                       String deviceId, final HttpListener<PlayInfo> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("ad_id", adId);
        params.put("os_type", Constants.OS_TYPE);
        params.put("sdk_key", sdkKey);
        params.put("sdk_device_id", deviceId);
        params.put("sdk_version", Constants.VERSION);
        try {
            JSONObject speedObj = new JSONObject();
            speedObj.put("ip",0);
            params.put("stream_speed", speedObj.toString());
        } catch (Exception ex) {
        }

        HttpHelper.obtian().doPost(host + "/user/actions/play", params,
                new HttpListener<JSONObject>() {
                    @Override
                    public void success(JSONObject result) {
                        listener.success(PlayInfo.jsonToPlayInfo(result));
                    }

                    @Override
                    public void failure(HttpException e) {
                        listener.failure(e);
                    }
                });
    }

    public static void userAppKeys(String host, final HttpListener<List<Advert>> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("os_type", Constants.OS_TYPE);
        HttpHelper.obtian().doGet(host + "/user/app_keys", params,
                new HttpListener<JSONObject>() {
                    @Override
                    public void success(JSONObject result) {
                        listener.success(Advert.jsonToPlayInfo(result));
                    }

                    @Override
                    public void failure(HttpException e) {
                        listener.failure(e);
                    }
                });
    }

    public static void report(String host, String token, String action) {
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("action", action);
        HttpHelper.obtian().doPost(host + "/user/report/", params,
                new HttpListener<JSONObject>() {
                    @Override
                    public void success(JSONObject result) {
                        PlayLog.e("[PIReport] report success ");
                    }

                    @Override
                    public void failure(HttpException e) {
                        PlayLog.e("[PIReport] endplay report error: " + e);
                    }
                });
    }

    public static void analyze(String host, String token, String data) {
        Map<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("event_name", "android_sdk");
        params.put("data", data);
        HttpHelper.obtian().doPost(host + "/analytics/event", params,
                new HttpListener<JSONObject>() {
                    @Override
                    public void success(JSONObject result) {
                        PlayLog.e("[PIAnalyze] report success ");
                    }

                    @Override
                    public void failure(HttpException e) {
                        PlayLog.e("[PIAnalyze] error: " + e);
                    }
                });
    }
}
