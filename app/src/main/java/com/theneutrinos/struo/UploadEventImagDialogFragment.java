package com.theneutrinos.struo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Aman Deep Singh on 22-01-2017.
 */

public class UploadEventImagDialogFragment extends DialogFragment
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Upload Image")
                .setMessage("Where do you want to upload the image from?")
                .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((ActuallyAddEventActivity)getActivity()).openIntentGallery();
                    }
                }).setNeutralButton("Camera", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((ActuallyAddEventActivity)getActivity()).openIntentCamera();
            }
        });
        return builder.create();
    }
}
