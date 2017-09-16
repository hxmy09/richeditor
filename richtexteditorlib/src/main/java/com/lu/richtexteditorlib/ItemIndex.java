package com.lu.richtexteditorlib;


import android.support.v4.util.ArraySet;

/**
 * Created by 陆正威 on 2017/9/15.
 */

@SuppressWarnings("WeakerAccess")
public class ItemIndex {
    public static final long INSERT_IMAGE = 0x01;
    public static final long A = 0x02;
    public static final long MORE = 0x03;
    public static final long UNDO = 0x04;
    public static final long REDO = 0x05;
    public static final long BOLD = 0x06 ;
    public static final long ITALIC = 0x07 ;
    public static final long STRIKE_THROUGH = 0x08 ;
    public static final long BLOCK_QUOTE = 0x09 ;
    public static final long H1 = 0x0a ;
    public static final long H2 = 0x0b ;
    public static final long H3 = 0x0c ;
    public static final long H4 = 0x0d;
    public static final long HALVING_LINE = 0x0e;
    public static final long LINK = 0x0f;

    private static ArraySet<Long> registerSet = new ArraySet<>();

    // TODO: 2017/9/16 为自定义添加按钮准备
    public static boolean register(long id){
        return registerSet.add(id);
    }

    public static boolean hasRegister(long id){
        return registerSet.contains(id);
    }
}
