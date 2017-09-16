package com.lu.lubottommenu.menuitem;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.lu.lubottommenu.R;
import com.lu.lubottommenu.logiclist.MenuItem;

/**
 *
 * Created by 陆正威 on 2017/9/6.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class ImageViewButtonItem extends BottomMenuItem<ImageButton> implements Parcelable {

    public interface OnImageViewButtonItemClickListener{
        boolean onItemClick(MenuItem item,boolean isSelected);
    }

    private int idRes;
    private boolean enableAutoSet = true;//点击后根据是否选中自动设置显示的效果
    private OnImageViewButtonItemClickListener mOnItemClickListener;

    public ImageViewButtonItem(Context context, MenuItem menuItem, int idRes) {
        this(context, menuItem, idRes, true);
    }

    public ImageViewButtonItem(Context context, MenuItem menuItem, int idRes, boolean enableAutoSet) {
        super(context, menuItem);
        this.idRes = idRes;
        this.enableAutoSet = enableAutoSet;
    }

    @SuppressWarnings("deprecation")
    @NonNull
    @Override
    public ImageButton createView() {
        ImageButton imageViewButton = new ImageButton(getContext());
        if(!enableAutoSet) {
            //无边框的带有水波纹的按钮样式
            TypedArray typedArray = getContext().obtainStyledAttributes(new int[]{R.attr.selectableItemBackgroundBorderless});
            Drawable drawable = typedArray.getDrawable(0);
            imageViewButton.setBackgroundDrawable(drawable);
            typedArray.recycle();
        }else
            imageViewButton.setBackgroundDrawable(null);

        imageViewButton.setImageResource(idRes);
        imageViewButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        return imageViewButton;
    }

    @Override
    public void settingAfterCreate(boolean isSelected, final ImageButton imageViewButton) {
        if (enableAutoSet) {
            if (isSelected) {
                imageViewButton.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
            } else {
                imageViewButton.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
            }
        }else {
            imageViewButton.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        }
    }

    //自己有点击事件时根据自身的返回值拦截，否则父类方法始终返回false不拦截
    @Override
    public boolean onItemClickIntercept() {
        return mOnItemClickListener == null ? super.onItemClickIntercept():
                mOnItemClickListener.onItemClick(getMenuItem(),isSelected());
    }

    @Override
    public void onSelectChange(boolean isSelected) {
        ImageButton imageViewButton = (ImageButton) getMainView();
        if (imageViewButton == null) return;
        settingAfterCreate(isSelected, imageViewButton);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.idRes);
        dest.writeInt(this.enableAutoSet ? 1 : 0);
    }

    protected ImageViewButtonItem(Parcel in) {
        super(in);
        this.idRes = in.readInt();
        this.enableAutoSet = in.readInt() == 1;
    }

    public static final Creator<ImageViewButtonItem> CREATOR = new Creator<ImageViewButtonItem>() {
        @Override
        public ImageViewButtonItem createFromParcel(Parcel source) {
            return new ImageViewButtonItem(source);
        }

        @Override
        public ImageViewButtonItem[] newArray(int size) {
            return new ImageViewButtonItem[size];
        }
    };

    public boolean isEnableAutoSet() {
        return enableAutoSet;
    }

    public void setEnableAutoSet(boolean enableAutoSet) {
        this.enableAutoSet = enableAutoSet;
    }

    public void setOnItemClickListener(OnImageViewButtonItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

}
