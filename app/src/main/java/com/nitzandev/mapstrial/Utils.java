package com.nitzandev.mapstrial;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Created by nitzanwerber on 5/8/15.
 */
public class Utils {

    static public void showErrorDialog(Context context, int res) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder.setTitle(R.string.err_dialog_title);
        alertDialogBuilder.setMessage(res);
        alertDialogBuilder.setPositiveButton(context.getString(R.string.got_it), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    static public void showErrorDialog(final Context context, ArrayList<Integer> resList) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder.setTitle(R.string.err_dialog_title);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View errView = layoutInflater.inflate(R.layout.err_alert, null, false);
        LinearLayout ll = (LinearLayout) errView.findViewById(android.R.id.inputArea);
        for (Integer i : resList) {
            TextView v = new TextView(context);
            v.setGravity(Gravity.CENTER);
            v.setText(i);
            ll.addView(v);
        }
        alertDialogBuilder.setView(errView);
        alertDialogBuilder.setPositiveButton(context.getString(R.string.got_it), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }


    public static String milliToString(long millis) {

        long hrs = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long min = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        long sec = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        long mls = millis % 1000;
        String toRet = String.format("%02d:%02d:%02d:%03d", hrs, min, sec, mls);
        return toRet;
    }
}
