package com.lu.base.depence;

import android.app.ActivityManager;
import android.app.Application;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.imnjh.imagepicker.PickerConfig;
import com.imnjh.imagepicker.SImagePicker;
import com.lu.base.depence.fresco.FrescoCacheParams;
import com.lu.base.depence.fresco.FrescoImageLoader;
import com.lu.base.depence.retrofit.RetrofitClient;
import com.lu.base.depence.tools.Constant;
import com.lu.base.depence.tools.Utils;

import java.io.File;


/**
 * Created by 陆正威 on 2017/4/6.
 */

public class AppManager extends Application {
    private static AppManager context;
    private ActivityManager activityManager;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ScreenParamsInit();
        FrescoInit();
        SImagePicker.init(new PickerConfig.Builder().setAppContext(this)
                .setImageLoader(new FrescoImageLoader())
                .build());
    }

    public static AppManager app() {
        return context;
    }

    private void ScreenParamsInit() {
        int a[] = Utils.getScreenSize();
        Constant.screenWithPx = a[0];
        Constant.screenHeightPx = a[1];
        Constant.screenWithDp = Utils.px2dip(Constant.screenWithPx);
        Constant.screenHeightDp = Utils.px2dip(Constant.screenHeightPx);
    }

    private void FrescoInit() {
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(this)
                .setMaxCacheSize(40 * ByteConstants.MB)
                .setBaseDirectoryPathSupplier(new Supplier<File>() {
                    @Override
                    public File get() {
                        return getCacheDir();
                    }
                })
                .build();

        final FrescoCacheParams bitmapCacheParams = new FrescoCacheParams(activityManager);
        ImagePipelineConfig imagePipelineConfig = OkHttpImagePipelineConfigFactory.newBuilder(this, RetrofitClient.getInstance().getOkHttpClient())
                .setMainDiskCacheConfig(diskCacheConfig)
                .setBitmapMemoryCacheParamsSupplier(bitmapCacheParams)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, imagePipelineConfig);
    }

}
