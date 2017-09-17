package com.lu.lubottommenu.api;

import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by 陆正威 on 2017/9/17.
 */

public interface ITheme extends Serializable,Parcelable{
    int[] getBackGroundColors();
    int getAccentColor();
    int getNormalColor();
}
