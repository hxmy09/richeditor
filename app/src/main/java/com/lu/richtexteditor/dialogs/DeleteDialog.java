package com.lu.richtexteditor.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.lu.richtexteditor.R;

/**
 * Created by 陆正威 on 2017/9/12.
 */

public class DeleteDialog extends DialogFragment {
    public static final String Tag = "delete_dialog_fragment";
    private View dialog;
    private Long imageId;
    private OnDialogClickListener listener;

    public static DeleteDialog createDeleteDialog(Long imageId){
        final DeleteDialog newDialog = new DeleteDialog();
        newDialog.setImageId(imageId);
        return newDialog;
    }

    public DeleteDialog(){

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity()).setMessage(R.string.delete_pic)
                .setPositiveButton(R.string.general_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        if(listener!=null)
                            listener.onConfirmButtonClick(imageId);
                    }
                })
                .setNegativeButton(R.string.general_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        if(listener!= null)
                            listener.onCancelButtonClick();
                    }
                }).setTitle(R.string.handles).create();
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public void setListener(OnDialogClickListener listener) {
        this.listener = listener;
    }

    public interface OnDialogClickListener {
        void onConfirmButtonClick(Long ud);
        void onCancelButtonClick();
    }
}
