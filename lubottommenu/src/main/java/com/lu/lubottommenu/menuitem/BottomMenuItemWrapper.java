package com.lu.lubottommenu.menuitem;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.view.View;

import com.lu.lubottommenu.logiclist.MenuItem;
import com.lu.lubottommenu.logiclist.MenuItemFactory;

/**
 * unused
 * Created by 陆正威 on 2017/9/6.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class BottomMenuItemWrapper extends AbstractBottomMenuItem {

    private AbstractBottomMenuItem actualItem;

    public BottomMenuItemWrapper(Context context){
        this(context,null);
    }

    BottomMenuItemWrapper(Context context, MenuItem menuItem) {
        super(context,menuItem);
    }

    public BottomMenuItemWrapper(Parcel source) {
        super(source);
    }

    public void wrapper(AbstractBottomMenuItem item){
        actualItem = item;
    }

    /*
        生成默认BottomMenuItem
     */
    private AbstractBottomMenuItem generateBottomMenuItem(Context context, Long id){
        return MenuItemFactory.generateImageItem(context,id,-1);
    }

    @NonNull
    @Override
    public View createView() {
        return actualItem.createView();
    }

    @Override
    public void settingAfterCreate(boolean isSelected, View view) {

    }

    @Override
    public Long getItemId() {
        return actualItem.getItemId();
    }

    @Override
    public View getMainView() {
        return actualItem.getMainView();
    }

    private boolean checkNull(Object object){
        return object == null;
    }

    public static final Creator CREATOR = new Creator() {
        @Override
        public BottomMenuItemWrapper createFromParcel(Parcel source) {
            return new BottomMenuItemWrapper(source);
        }

        @Override
        public BottomMenuItemWrapper[] newArray(int size) {
            return new BottomMenuItemWrapper[size];
        }
    };
}
