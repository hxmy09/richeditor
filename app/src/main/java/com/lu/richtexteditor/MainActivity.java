package com.lu.richtexteditor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.imnjh.imagepicker.SImagePicker;
import com.imnjh.imagepicker.activity.PhotoPickerActivity;
import com.lu.base.depence.retrofit.uploader.RxUploader;
import com.lu.base.depence.retrofit.uploader.api.Uploader;
import com.lu.base.depence.retrofit.uploader.beans.UploadProgress;
import com.lu.base.depence.tools.SizeUtil;
import com.lu.base.depence.tools.Utils;
import com.lu.lubottommenu.LuBottomMenu;
import com.lu.richtexteditor.dialogs.DeleteDialog;
import com.lu.richtexteditor.dialogs.LinkDialog;
import com.lu.richtexteditorlib.SimpleRichEditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.ResponseBody;


public class MainActivity extends AppCompatActivity implements SimpleRichEditor.OnEditorClickListener {

    private static final int REQUEST_CODE_IMAGE = 101;
    private static final String UPLOAD_URL ="http://www.lhbzimo.cn:3000/";
    private static final String PAR_NAME = "images";

    private LuBottomMenu mLuBottomMenu;
    private SimpleRichEditor mRichTextView;

    //private ArrayList<String> selImageList; //当前选择的所有图片

    private HashMap<Long,String> insertedImages;
    private HashMap<Long,String> failedImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        insertedImages = new HashMap<>();
        failedImages = new HashMap<>();
        initView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMAGE) {
            final ArrayList<String> pathList =
                    data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT_SELECTION);

            long id;
            // TODO: 2017/9/14 在线程中添加
            for (String path :
                    pathList) {
                id = SystemClock.currentThreadTimeMillis();
                //Log.e("add id",id+"");
                long size[] = SizeUtil.getBitmapSize(path);
                mRichTextView.insertImage(path, id, size[0], size[1]);
                insertedImages.put(id,path);
                tryUpload(path, id);
            }

            final boolean original =
                    data.getBooleanExtra(PhotoPickerActivity.EXTRA_RESULT_ORIGINAL, false);
        }
    }

    private void tryUpload(String path, final long id) {
        RxUploader.INSTANCE.upload(UPLOAD_URL,PAR_NAME, path, id)
                .start()
                .receiveEvent(new Uploader.OnUploadListener() {
                    @Override
                    public void onStart() {
                        Log.e("start",id+"");
                    }

                    @Override
                    public void onUploading(String filePath, UploadProgress progress) {
                        Log.e("onUploading", id +": " + String.valueOf(progress.getProgress()));
                        mRichTextView.setImageUploadProcess(id, (int) progress.getProgress());
                    }

                    @Override
                    public void onCompleted(String filePath, ResponseBody responseBody) {
                        try {
                            Response response = new GsonBuilder().create().fromJson(responseBody.string(),Response.class);
                            Log.e("onCompleted", response.getData().get(0));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mRichTextView.setImageUploadProcess(id, 100);
                    }

                    @Override
                    public void onFailed(String filePath, Throwable throwable) {
                        Log.e("onFailed", throwable.getMessage());
                        mRichTextView.setImageFailed(id);
                        insertedImages.remove(id);
                        failedImages.put(id,filePath);
                    }
                });
    }

    private void initView() {
        mLuBottomMenu = (LuBottomMenu) findViewById(R.id.lu_bottom_menu);
        mRichTextView = (SimpleRichEditor) findViewById(R.id.rich_text_view);
        mRichTextView.setOnEditorClickListener(this);
        mRichTextView.setLuBottomMenu(mLuBottomMenu);
        initImagePicker();
    }

    private void initImagePicker() {

    }

    private void showImagePicker() {
        SImagePicker
                .from(MainActivity.this)
                .maxCount(9)
                .rowCount(3)
                .pickText(R.string.pick_name)
                .pickMode(SImagePicker.MODE_IMAGE)
                .fileInterceptor(new SingleFileLimitInterceptor())
                .forResult(REQUEST_CODE_IMAGE);
    }

    private void showLinkDialog(final LinkDialog dialog, final boolean isChange) {
        dialog.setListener(new LinkDialog.OnDialogClickListener() {
            @Override
            public void onConfirmButtonClick(String name, String url) {
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(url)) {
                    Utils.MakeLongToast(getString(R.string.not_empty));
                } else {
                    //do something
                    if (!isChange)
                        mRichTextView.insertLink(url, name);
                    else
                        mRichTextView.changeLink(url, name);
                    onCancelButtonClick();
                }
            }

            @Override
            public void onCancelButtonClick() {
                dialog.dismiss();
            }
        });
        dialog.show(getSupportFragmentManager(), LinkDialog.Tag);
    }

    private void showDeleteDialog(final DeleteDialog dialog) {
        dialog.setListener(new DeleteDialog.OnDialogClickListener() {
            @Override
            public void onConfirmButtonClick(Long id) {
                Log.e("onConfirmButtonClick",id.toString());
                mRichTextView.deleteImageById(id);
                removeFromLocalCache(id);
                RxUploader.TaskController controller = RxUploader.INSTANCE.handle(id);
                if(controller!=null) {
                    controller.stopReceive();
                    controller.remove();
                }
            }

            @Override
            public void onCancelButtonClick() {
                //dialog.dismiss();
            }
        });
        dialog.show(getSupportFragmentManager(), DeleteDialog.Tag);
    }

    private void removeFromLocalCache(long id){
        if(insertedImages.containsKey(id))
            insertedImages.remove(id);
        else if(failedImages.containsKey(id))
            failedImages.remove(id);
    }

    @Override
    public void onLinkButtonClick() {
        showLinkDialog(LinkDialog.createLinkDialog(), false);
    }

    @Override
    public void onInsertImageButtonClick() {
        showImagePicker();
    }

    @Override
    public void onLinkClick(String name, String url) {
        showLinkDialog(LinkDialog.createLinkDialog(name, url), true);
    }

    @Override
    public void onImageClick(Long id) {
        Log.e("onImageClick",id.toString());
        if(insertedImages.containsKey(id))
            showDeleteDialog(DeleteDialog.createDeleteDialog(id));
        else if(failedImages.containsKey(id)){
            mRichTextView.setImageReload(id);
            tryUpload(failedImages.get(id),id);
            insertedImages.put(id,failedImages.get(id));
            failedImages.remove(id);
        }
    }
}
