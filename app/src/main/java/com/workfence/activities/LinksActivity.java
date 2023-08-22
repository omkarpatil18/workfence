package com.workfence.activities;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.workfence.R;

public class LinksActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_links);

        TextView tv_links = findViewById(R.id.links_tv);

        String link1 = "<br>1.<a href='https://www.cdc.gov/coronavirus/2019-ncov/prevent-getting-sick/social-distancing.html'>CDC</a><br>" +
                "2.<a href='https://www.who.int/emergencies/diseases/novel-coronavirus-2019/advice-for-public'>WHO</a><br>" +
                "3.<a href='https://gujcost.gujarat.gov.in/Images/images/pdf/Social-Distancing-Social-Media.pdf'>Department of Science and Technology, Government of Gujarat</a>";
        tv_links.setText(Html.fromHtml(link1));
        tv_links.setMovementMethod(LinkMovementMethod.getInstance());

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