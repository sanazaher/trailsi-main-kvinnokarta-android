package com.trailsi.kvinnokarta.android.common.utils;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;

import com.trailsi.kvinnokarta.android.R;
import com.trailsi.kvinnokarta.android.common.notify.Notify;
import com.trailsi.kvinnokarta.android.common.notify.NotifyAccept;

public class DialogUtil {

    public static void ShowAlert(final Context context, String title, String message, final NotifyAccept notify) {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dlg_alert, null);
        final AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppTheme_Dialog))
                .setView(promptsView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView txtTitle = promptsView.findViewById(R.id.txt_title);
        TextView txtMsg = promptsView.findViewById(R.id.txt_msg);
        txtTitle.setText(title);
        txtMsg.setText(message);

        Button btnOK = promptsView.findViewById(R.id.btn_ok);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify.onAccept(null);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void ShowAskPurchase(final Context context, int count, final Notify notify) {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dlg_ask_purchase, null);
        final AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppTheme_Dialog))
                .setView(promptsView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView txtCount = promptsView.findViewById(R.id.txt_count);
        txtCount.setText(count + " POI(s) remaining");

        Button btnFree = promptsView.findViewById(R.id.btn_free);
        btnFree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify.onCancel(null);
                dialog.dismiss();
            }
        });
        Button btnPurchase = promptsView.findViewById(R.id.btn_purchase);
        btnPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify.onAccept(true);
                dialog.dismiss();
            }
        });
        Button btnRestore = promptsView.findViewById(R.id.btn_restore);
        btnRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify.onAccept(false);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public static void ShowNoFreePurchase(final Context context, final Notify notify) {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.dlg_no_free_purchase, null);
        final AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppTheme_Dialog))
                .setView(promptsView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        Button btnPurchase = promptsView.findViewById(R.id.btn_purchase);
        btnPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify.onAccept(true);
                dialog.dismiss();
            }
        });

        Button btnRestore = promptsView.findViewById(R.id.btn_restore);
        btnRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify.onAccept(false);
                dialog.dismiss();
            }
        });

        Button btnCancel = promptsView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify.onCancel(null);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
