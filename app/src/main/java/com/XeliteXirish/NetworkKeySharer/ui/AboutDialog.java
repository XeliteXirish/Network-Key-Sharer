package com.XeliteXirish.NetworkKeySharer.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.XeliteXirish.NetworkKeySharer.BuildConfig;
import com.XeliteXirish.NetworkKeySharer.R;


public class AboutDialog extends AlertDialog {

    private String aboutMessage;

    public AboutDialog(Context context) {
        super(context);
        setTitle(R.string.about_dialog_title);
        buildAboutMessage();
        setMessage(""); // will be set in @AboutDialog#show
        setButton(BUTTON_POSITIVE, getContext().getString(R.string.action_close), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dismiss();
            }
        });
    }

    private void buildAboutMessage() {
        String appVersion = BuildConfig.VERSION_NAME;
        aboutMessage = String.format(getContext().getString(R.string.about_dialog_msg_description), appVersion)
                + getContext().getString(R.string.about_dialog_msg_credits_title)
                + getContext().getString(R.string.about_dialog_msg_credits)
                + getContext().getString(R.string.about_dialog_author_title)
                + getContext().getString(R.string.about_dialog_author);
    }

    @Override
    public void show() {
        super.show();
        /* Get TextView from the original AlertDialog layout */
        TextView aboutContent = (TextView) findViewById(android.R.id.message);
        aboutContent.setText(Html.fromHtml(aboutMessage));
        aboutContent.setMovementMethod(LinkMovementMethod.getInstance()); // can click on links
    }
}
