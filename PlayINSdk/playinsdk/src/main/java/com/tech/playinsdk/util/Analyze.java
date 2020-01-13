package com.tech.playinsdk.util;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class Analyze {

    private static class AnalyzeClassInstance {
        private static final Analyze instance = new Analyze();
    }

    private Analyze() {}

    public static Analyze getInstance() {
        return AnalyzeClassInstance.instance;
    }

    public void reset() {
        sendTotal = 0;
        sendFail = 0;
        recvVideoTotal = 0;
        recvVideoFail = 0;
        vd_0_50 = 0;
        vd_50_100 = 0;
        vd_100_150 = 0;
        vd_150_max = 0;
        errMsg = null;
    }

    public JSONObject report() {
        /*
        phone		手机型号
        version	    手机版本

        recvSucc	接收视频总帧数
        recvFail	丢弃视频帧数

        sendSucc	发送手势总帧数
        sendFail	丢弃手势帧数

        vd1			解码区间 0_50
        vd2			解码区间 50_100
        vd3			解码区间 100_150
        vd4			解码区间 150_max

        errMsg		错误信息
        */

        JSONObject obj = new JSONObject();
        try {
//            obj.put("recvStatus", recvVideoTotal + "-" + recvVideoFail);
//            obj.put("sendStatus", sendTotal + "-" + sendFail);
//            obj.put("vdStatus", vd_0_50 + "-" + vd_50_100 + "-" + vd_100_150 + "-" + vd_150_max);

            obj.put("recvSucc", recvVideoTotal);
            obj.put("recvFail", recvVideoFail);
            obj.put("sendSucc", sendTotal);
            obj.put("sendFail", sendFail);
            obj.put("vd1", vd_0_50);
            obj.put("vd2", vd_50_100);
            obj.put("vd3", vd_100_150);
            obj.put("vd4", vd_150_max);

            if (!TextUtils.isEmpty(errMsg)) {
                obj.put("errMsg", errMsg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    // 解码统计
    private int vd_0_50 = 0;
    private int vd_50_100 = 0;
    private int vd_100_150 = 0;
    private int vd_150_max = 0;
    public void videoDecoder(int duration) {
        if (duration < 50) {
            vd_0_50++;
        } else if (duration < 100) {
            vd_50_100++;
        } else if (duration < 150) {
            vd_100_150++;
        } else {
            vd_150_max++;
        }
    }

    // 发送触控数据统计
    private long sendTotal = 0;
    private long sendFail = 0;
    public void sendResult(boolean reslut) {
        sendTotal++;
        if (!reslut) sendFail++;
    }

    // 接受视频数据统计
    private long recvVideoTotal = 0;
    private long recvVideoFail = 0;
    public void receiveVideoResult(boolean reslut) {
        recvVideoTotal++;
        if (!reslut) recvVideoFail++;
    }

    private String errMsg;
    public void playError(Exception ex) {
        errMsg = ex.toString();
    }
}
