package com.theneutrinos.struo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;

import com.theneutrinos.struo.AddPostActivity;

import java.io.IOException;

public class UploadImageDialogFragment extends DialogFragment
{

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Upload Image");
        builder.setMessage("Where do you want to upload the image from?");
        builder.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //intent.setType("image/*");
                //startActivityForResult(intent, galleryRequestCode);
                //AddPostActivity.flag = 1;
                ((AddPostActivity)getActivity()).openIntentGallery();

            }
        });
        builder.setNeutralButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //startActivityForResult(cameraIntent, galleryRequestCode);
                //AddPostActivity.flag = 2;
                ((AddPostActivity)getActivity()).openIntentCamera();
            }
        });
        return builder.create();
    }
}
