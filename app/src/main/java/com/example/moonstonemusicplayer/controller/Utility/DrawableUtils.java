package com.example.moonstonemusicplayer.controller.Utility;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;

public class DrawableUtils {

    /**
     * Returns a tinted drawable with the specified color.
     *
     * @param context The context to access resources.
     * @param drawableResId The drawable resource ID.
     * @param tintColor The color to tint the drawable.
     * @return A tinted drawable, or null if the drawable resource is invalid.
     */
    public static Drawable getTintedDrawable(Context context, @DrawableRes int drawableResId, int tintColor) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableResId);
        if (drawable != null) {
            drawable = drawable.mutate(); // Ensure this instance is unique
            drawable.setTint(tintColor); // Apply tint
        }
        return drawable;
    }
}

