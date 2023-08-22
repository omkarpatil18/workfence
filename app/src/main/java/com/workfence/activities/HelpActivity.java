package com.workfence.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.workfence.R;
import com.workfence.adapters.SpinnerAdapter;
import com.workfence.others.Utils;

import java.util.ArrayList;

public class HelpActivity extends Activity implements AdapterView.OnItemSelectedListener {

    Button back_btn;
    Spinner spinner;

    TextView tv_help, tv_links;
    ImageView iv_help;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        spinner = findViewById(R.id.spinner);
        back_btn = findViewById(R.id.button);
        iv_help = findViewById(R.id.help_img);
        tv_help = findViewById(R.id.help_tv);
        tv_links = findViewById(R.id.links_tv);

        String link1 = "<b>More on Social distancing</b><br>1.<a href='https://www.cdc.gov/coronavirus/2019-ncov/prevent-getting-sick/social-distancing.html'>CDC</a><br>" +
                "2.<a href='https://www.who.int/emergencies/diseases/novel-coronavirus-2019/advice-for-public'>WHO</a><br>" +
                "3.<a href='https://gujcost.gujarat.gov.in/Images/images/pdf/Social-Distancing-Social-Media.pdf'>Department of Science and Technology, Government of Gujarat</a>";
        tv_links.setText(Html.fromHtml(link1));
        tv_links.setMovementMethod(LinkMovementMethod.getInstance());

        ArrayList<String> categories = new ArrayList<>();
        categories.add("Personal Distancing");
        if (Utils.getIsAdmin(this)) {

            categories.add("Employee Distancing");
            categories.add("Daily Attendance");
            categories.add("Employee Metrics");
        }

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        spinner.setOnItemSelectedListener(this);

        SpinnerAdapter dataAdapter = new SpinnerAdapter(this, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(0);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                iv_help.setImageResource(R.drawable.help_personal_interac_img);
                tv_help.setText(getString(R.string.help_personal_interaction));
                break;
            case 1:
                iv_help.setImageResource(R.drawable.help_interac_img);
                tv_help.setText(getString(R.string.help_company_interaction));
                break;
            case 2:
                iv_help.setImageResource(R.drawable.help_attendance);
                tv_help.setText(getString(R.string.help_attendance));
                break;
            case 3:
                iv_help.setImageResource(R.drawable.help_emp_metrics_img);
                tv_help.setText(getString(R.string.help_emp_metrics));
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}