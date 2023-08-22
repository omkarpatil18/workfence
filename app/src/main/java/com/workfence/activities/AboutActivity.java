package com.workfence.activities;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.workfence.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView tv = findViewById(R.id.policy);
        TextView tv2 = findViewById(R.id.textView27);
        TextView tv3 = findViewById(R.id.textView3);
        String link = "Check out our website at- <a href = 'http://www.workfence.in/'>www.workfence.in</a>";
        //tv.setText(Html.fromHtml(getString(R.string.privacy_policy)));
        tv2.setText(Html.fromHtml(link));

        String dist_issues = "In case distance estimation is not accurate for you, or for any other issues, please reach out to us at info@workfence.in";
        tv3.setText(dist_issues);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}