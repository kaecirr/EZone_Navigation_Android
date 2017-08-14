package com.example.kaelansinclair.ezone_navigation_android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

public class BeginNavigationDialogFragment extends DialogFragment{

    public static BeginNavigationDialogFragment newFragmentInstance(){
        BeginNavigationDialogFragment newFragment = new BeginNavigationDialogFragment();
        return newFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Location");
        builder.setMessage("test");
        builder.setPositiveButton("Start Navigation",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });

        builder.setNeutralButton("More Info",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                        dialog.cancel();
                    }
                });
        //Create the AlertDialog object and return it
        return builder.create();
    }


//DialogFragment dialog = BeginNavigationDialogFragment.newFragmentInstance();
       // dialog.show(getFragmentManager(), "tag");
}
