package com.tech.playinsdk.model.entity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Advert implements Serializable  {

    private String appKey;
    private String videoId;
    private String videoUrl;
    private String videoPath;
    private int videoTime;
    private String appstoreUrl;
    private String googleplayUrl;
    private String appIcon;
    private String appCover;
    private String appName;
    private int appRate;
    private String adId;
    private String sdkKey;
    private int osType;

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public int getVideoTime() {
        return videoTime;
    }

    public void setVideoTime(int videoTime) {
        this.videoTime = videoTime;
    }

    public String getAppstoreUrl() {
        return appstoreUrl;
    }

    public void setAppstoreUrl(String appstoreUrl) {
        this.appstoreUrl = appstoreUrl;
    }

    public String getGoogleplayUrl() {
        return googleplayUrl;
    }

    public void setGoogleplayUrl(String googleplayUrl) {
        this.googleplayUrl = googleplayUrl;
    }

    public String getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(String appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppCover() {
        return appCover;
    }

    public void setAppCover(String appCover) {
        this.appCover = appCover;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getAppRate() {
        return appRate;
    }

    public void setAppRate(int appRate) {
        this.appRate = appRate;
    }

    public String getAdId() {
        return adId;
    }

    public void setAdId(String adId) {
        this.adId = adId;
    }

    public String getSdkKey() {
        return sdkKey;
    }

    public void setSdkKey(String sdkKey) {
        this.sdkKey = sdkKey;
    }

    public int getOsType() {
        return osType;
    }

    public void setOsType(int osType) {
        this.osType = osType;
    }

    public static List<Advert> jsonToPlayInfo(JSONObject object) {
        List<Advert> advertList = new ArrayList<>();
        Advert advert;
        JSONArray array = object.optJSONArray("apps");
        JSONObject obj;
        for (int i = 0; i < array.length(); i++) {
            obj = array.optJSONObject(i);
            advert = new Advert();
            advert.setAppKey(obj.optString("app_key"));
            advert.setVideoId(obj.optString("video_id"));
            advert.setVideoUrl(obj.optString("video_url"));
            advert.setVideoTime(obj.optInt("video_time"));
            advert.setAppstoreUrl(obj.optString("appstore_url"));
            advert.setGoogleplayUrl(obj.optString("googleplay_url"));
            advert.setAppIcon(obj.optString("app_icon"));
            advert.setAppCover(obj.optString("app_cover"));
            advert.setAppName(obj.optString("app_name"));
            advert.setAppRate(obj.optInt("app_rate"));
            advert.setAdId(obj.optString("ad_id"));
            advert.setSdkKey(obj.optString("sdk_key"));
            advert.setOsType(obj.optInt("os_type"));
            advertList.add(advert);
        }
        return advertList;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Advert)) {
            return false;
        }
        Advert dest = (Advert) obj;
        if (appName == null && dest.appName == null) {
            return true;
        }
        return appName.equals(dest.appName);
    }

    @Override
    public int hashCode() {
        return appName == null ? 0 : appName.hashCode();
    }
}
