package com.vis.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vis.R;
import com.vis.beans.FbProfile;
import com.vis.beans.Feedback;
import com.vis.utilities.Constants;
import com.vis.utilities.WebServiceUtility;

public class FeedbackActivity extends AppCompatActivity {

    FbProfile fbProfile;
    SharedPreferences prefs;
    EditText email,feedback;
    Button submit,cancel;
    String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        email = (EditText) findViewById(R.id.input_email);
        feedback = (EditText) findViewById(R.id.input_feedback);
        submit = (Button) findViewById(R.id.submit);
        cancel = (Button) findViewById(R.id.cancel);
        prefs = getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        String emailString =null;
        String fbUser = prefs.getString(Constants.FB_USER_INFO,null);
        if(fbUser!=null)
        {
            fbProfile = new Gson().fromJson(fbUser,FbProfile.class);
        }

        if(fbProfile!=null)
        {
            userId = fbProfile.getFbUserId();
            emailString = fbProfile.getEmail();
        }

        if(emailString!=null)
        {
            email.setText(emailString);
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(email.getText().toString().isEmpty() || feedback.getText().toString().isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"All fields are Mandatory",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Feedback feedbackBean = new Feedback();
                    feedbackBean.setUserId(userId);
                    feedbackBean.setEmailId(email.getText().toString());
                    feedbackBean.setFeedback(feedback.getText().toString());
                    new WebServiceUtility(getApplicationContext(),Constants.FEEDBACK,feedbackBean);
                    Toast.makeText(getApplicationContext(),"Thank you for your feedback.",Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

    }

}
