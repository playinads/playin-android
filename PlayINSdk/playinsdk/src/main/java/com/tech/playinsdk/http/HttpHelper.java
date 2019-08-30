package com.tech.playinsdk.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.tech.playinsdk.util.PILog;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpHelper {

    private static ExecutorService sThreadPool = Executors.newFixedThreadPool(4);
    private static Handler sHandler = new Handler(Looper.getMainLooper());
    private static int TIMEOUT = 20000;

    private static class HttpHelperHolder {
        private static final HttpHelper INSTANCE = new HttpHelper();
    }

    public static HttpHelper obtian() {
        return HttpHelperHolder.INSTANCE;
    }

    private String sessionKey;

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public void doGet(final String url, final Map<String, String> params, final HttpListener<JSONObject> listener) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                InputStream is = null;
                BufferedReader br = null;
                try {
                    String newUrl;
                    String paramsUrl = getUrlFromParams(params);
                    if (!TextUtils.isEmpty(paramsUrl)) {
                        newUrl = url + "?" + paramsUrl;
                    } else {
                        newUrl = url;
                    }
                    connection = getHttpURLConnection(newUrl);
                    addAuthorization(connection, params);
                    int status = connection.getResponseCode();
                    if(status == 200){
                        is = connection.getInputStream();
                    } else {
                        throw new HttpException(-1, "responseCode:" + status);
                    }
                    br = new BufferedReader(new InputStreamReader(is));
                    final StringBuilder sb = new StringBuilder();
                    String readLine;
                    while((readLine = br.readLine()) != null){
                        sb.append(readLine);
                    }
                    deliverResult(url, listener, sb.toString());
                } catch (Exception e) {
                    PILog.v("deliverFailure:  " + url + "  " + e);
                    e.printStackTrace();
                    deliverFailure(listener, new HttpException(e));
                } finally {
                    connection.disconnect();
                    closeStream(is, br);
                }
            }
        };
        sThreadPool.submit(runnable);
    }

    public void doPost(final String url, final Map<String, String> params, final HttpListener<JSONObject> listener) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                InputStream is = null;
                BufferedReader br = null;
                try {
                    connection = getHttpURLConnection(url);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    addAuthorization(connection, params);
                    connection.connect();
                    String content = getJsonFromParams(params);
                    if (!TextUtils.isEmpty(content)) {
                        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                        out.writeBytes(content);
                        out.flush();
                        out.close();
                    }
                    int status = connection.getResponseCode();
                    if (status == 200) {
                        is = connection.getInputStream();
                    } else {
                        throw new HttpException(-1, "服务端状态码:" + status);
                    }
                    br = new BufferedReader(new InputStreamReader(is));
                    final StringBuilder sb = new StringBuilder();
                    String readLine;
                    while((readLine = br.readLine()) != null){
                        sb.append(readLine);
                    }
                    deliverResult(url, listener, sb.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    deliverFailure(listener, new HttpException(e));
                } finally {
                    connection.disconnect();
                    closeStream(is, br);
                }
            }
        };
        sThreadPool.submit(runnable);
    }

    private HttpURLConnection getHttpURLConnection(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        return connection;
    }

    private void addAuthorization(HttpURLConnection connection, Map<String, String> params) {
        String authValue = sessionKey;
        if (null != params && params.containsKey("session_key")) {
            authValue = params.get("session_key");
        }
        connection.setRequestProperty("Authorization", authValue);
    }

    private void deliverResult(String url, final HttpListener listener, final String reponse) throws JSONException {
        PILog.v("deliverResult:  " + url + "  " + reponse);
        JSONObject result = new JSONObject(reponse);
        if (result.getInt("code") == 0) {
            deliverSuccess(listener, result.optJSONObject("data"));
        } else {
            deliverFailure(listener, new HttpException(result));
        }
    }

    private void deliverSuccess(final HttpListener listener, final JSONObject result) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.success(result);
            }
        });
    }

    private void deliverFailure(final HttpListener listener, final HttpException ex) {
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                listener.failure(ex);
            }
        });
    }

    // 将Map连接成key1＝value1&key2=value2.
    private String getUrlFromParams(Map<String, String> params) {
        final StringBuilder sb = new StringBuilder();
        try {
            if (null != params) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    sb.append('=');
                    sb.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    sb.append('&');
                }
                if (sb.length() > 0) {
                    sb.delete(sb.length() - 1, sb.length());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    // 将Map转JSON格式
    private String getJsonFromParams(Map<String, String> params) throws JSONException {
        JSONObject obj = new JSONObject();
        if (null != params) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                obj.put(entry.getKey(), entry.getValue());
            }
        }
        return obj.toString();
    }

    private void closeStream(Closeable... closeables) {
        for (Closeable stream: closeables) {
            if (null != stream) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void getHttpBitmap(final String url, final HttpListener<Bitmap> listener) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                InputStream is = null;
                try{
                    URL myFileURL = new URL(url);
                    conn = (HttpURLConnection) myFileURL.openConnection();
                    conn.setConnectTimeout(TIMEOUT);
                    is = conn.getInputStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(is);
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.success(bitmap);
                        }
                    });
                } catch (final Exception e) {
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.failure(new HttpException(e));
                        }
                    });
                } finally {
                    conn.disconnect();
                    closeStream(is);
                }
            }
        };
        sThreadPool.submit(runnable);
    }
}
