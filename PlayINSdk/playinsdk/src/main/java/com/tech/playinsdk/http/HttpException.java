package com.tech.playinsdk.http;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.UnknownHostException;

public class HttpException extends Exception {

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public HttpException(JSONObject rootObj) {
        this.code = rootObj.optInt("code");
        String msg = rootObj.optString("onPlayError");
        if (TextUtils.isEmpty(msg)) {
            msg = rootObj.optString("error");
        }
        this.message = msg;
    }

    public HttpException(int code, String msg) {
        this.code = code;
        this.message = msg;
    }

    public HttpException(Exception ex) {
        if (ex instanceof HttpException) {
            this.code = ((HttpException)ex).getCode();
            this.message = ex.getMessage();
            return;
        }
        if (ex instanceof UnknownHostException || ex instanceof ConnectException) {
            this.code = -2;
            this.message = "network connection error";
        } else if (ex instanceof JSONException) {
            this.code = -1;
            this.message = "Server internal error";
        } else {
            String message = ex.getMessage();
            if (message == null || TextUtils.isEmpty(message)) {
                message = "other error";
            }
            this.code = -3;
            this.message = message;
        }
    }

    @Override
    public String toString() {
        return "[HttpException] " + message;
    }
}
