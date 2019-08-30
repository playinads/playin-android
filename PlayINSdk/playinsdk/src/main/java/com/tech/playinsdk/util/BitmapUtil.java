package com.tech.playinsdk.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;

import java.lang.annotation.Target;

public class BitmapUtil {

    /**
     * gaussian blur
     */
    public static Bitmap rsBlur(Context context, Bitmap source, int radius) {
        int width = Math.round(source.getWidth() * 0.1f);
        int height = Math.round(source.getHeight() * 0.1f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Bitmap inputBitmap = Bitmap.createScaledBitmap(source, width, height, false);
            Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
            RenderScript renderScript = RenderScript.create(context);
            ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
            Allocation inputAllocation = Allocation.createFromBitmap(renderScript, inputBitmap);
            Allocation outputAllocation = Allocation.createFromBitmap(renderScript, outputBitmap);
            scriptIntrinsicBlur.setRadius(radius);
            scriptIntrinsicBlur.setInput(inputAllocation);
            scriptIntrinsicBlur.forEach(outputAllocation);
            outputAllocation.copyTo(outputBitmap);
            return outputBitmap;
        } else {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.parseColor("#aa000000"));
            return bitmap;
        }
    }
}
