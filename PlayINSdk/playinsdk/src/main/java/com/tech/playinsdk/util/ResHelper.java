package com.tech.playinsdk.util;

import android.content.Context;

public class ResHelper {

    public static int getLayoutId(Context context, String name) {
        return context.getResources().getIdentifier(name, "layout", context.getPackageName());
    }

    public static int getResId(Context context, String name) {
        return context.getResources().getIdentifier(name, "id", context.getPackageName());

    }

    public static int getResDraw(Context context, String name) {
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());

    }

    public static int getResStyle(Context context, String name) {
        return context.getResources().getIdentifier(name, "style", context.getPackageName());

    }

    public static int getResStr(Context context, String name) {
        return context.getResources().getIdentifier(name, "string", context.getPackageName());
    }

    public static int getResXml(Context context, String name) {
        return context.getResources().getIdentifier(name, "xml", context.getPackageName());
    }
}
