package com.workfence.others;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.workfence.R;

public class CustomDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Context context;
    ImageView close_img, imageView;
    TextView textView;
    int image, text;

    public CustomDialog(Context context, int image, int text) {
        super(context);
        this.context = context;
        this.image = image;
        this.text = text;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.help_dialog);

        imageView = findViewById(R.id.help_img);
        close_img = findViewById(R.id.close_img);
        textView = findViewById(R.id.help_tv);

        imageView.setImageResource(image);
        textView.setText(text);

        close_img.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close_img) {
            dismiss();
        }
    }
}