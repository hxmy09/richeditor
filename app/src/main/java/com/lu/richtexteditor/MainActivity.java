package com.lu.richtexteditor;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.imnjh.imagepicker.SImagePicker;
import com.imnjh.imagepicker.activity.PhotoPickerActivity;
import com.lu.base.depence.retrofit.uploader.RxUploader;
import com.lu.base.depence.retrofit.uploader.api.Uploader;
import com.lu.base.depence.retrofit.uploader.beans.UploadProgress;
import com.lu.base.depence.tools.SizeUtil;
import com.lu.base.depence.tools.TimeUtils;
import com.lu.base.depence.tools.Utils;
import com.lu.lubottommenu.LuBottomMenu;
import com.lu.lubottommenu.api.IBottomMenuItem;
import com.lu.lubottommenu.logiclist.MenuItem;
import com.lu.lubottommenu.logiclist.MenuItemFactory;
import com.lu.myview.customview.richeditor.RichEditor;
import com.lu.richtexteditor.dialogs.DeleteDialog;
import com.lu.richtexteditor.dialogs.LinkDialog;
import com.lu.richtexteditorlib.SimpleRichEditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;

import static com.lu.myview.customview.richeditor.RichEditor.Type.BLOCKQUOTE;
import static com.lu.myview.customview.richeditor.RichEditor.Type.BOLD;
import static com.lu.myview.customview.richeditor.RichEditor.Type.ITALIC;
import static com.lu.myview.customview.richeditor.RichEditor.Type.STRIKETHROUGH;


public class MainActivity extends AppCompatActivity implements SimpleRichEditor.OnEditorClickListener {

    private static final int REQUEST_CODE_IMAGE = 101;
    private LuBottomMenu mLuBottomMenu;
    private SimpleRichEditor mRichTextView;

    private ArrayList<String> selImageList; //当前选择的所有图片

    private ArrayList<String> insertedImages;
    //private ArrayList<long> failedImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selImageList = new ArrayList<>();
        initView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMAGE) {
            final ArrayList<String> pathList =
                    data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT_SELECTION);
            selImageList.addAll(pathList);

            long id;
            // TODO: 2017/9/14 在线程中添加
            for (String path :
                    pathList) {
                id = SystemClock.currentThreadTimeMillis();
                Log.e("add id",id+"");
                long size[] = SizeUtil.getBitmapSize(path);
                mRichTextView.insertImage(path, id, size[0], size[1]);
                //mRichTextView.setImageUploadProcess(path,20);
                tryUpload(path, id);
            }

            final boolean original =
                    data.getBooleanExtra(PhotoPickerActivity.EXTRA_RESULT_ORIGINAL, false);
        }
    }

    private void tryUpload(String path, final long id) {
        RxUploader.INSTANCE.upload("http://www.lhbzimo.cn:3000/","images", path, id)
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
                            Log.e("onCompleted", responseBody.string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mRichTextView.setImageUploadProcess(id, 100);
                    }

                    @Override
                    public void onFailed(String filePath, Throwable throwable) {
                        Log.e("onFailed", throwable.getMessage());
                        mRichTextView.setImageFailed(id);
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
                    Utils.MakeLongToast("不能为空！");
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
                mRichTextView.deleteImageByUri(id);
            }

            @Override
            public void onCancelButtonClick() {
                //dialog.dismiss();
            }
        });
        dialog.show(getSupportFragmentManager(), DeleteDialog.Tag);
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
        showDeleteDialog(DeleteDialog.createDeleteDialog(id));
    }
}
