package com.vis.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vis.R;
import com.vis.beans.FbProfile;
import com.vis.beans.Interest;
import com.vis.utilities.Constants;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InterestActivity extends AppCompatActivity {

    SharedPreferences preferences;
    List<Interest> mainList;
    Set<String> selectedInterests;
    MyCustomAdapter dataAdapter;
    SharedPreferences.Editor editor;
    TableLayout tableLayout;
    FbProfile fbProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);
        mainList = new ArrayList<Interest>();
        preferences = getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        editor = preferences.edit();

        Button submit = (Button) findViewById(R.id.submit);
        Button cancel = (Button) findViewById(R.id.cancel);
        tableLayout = (TableLayout) findViewById(R.id.table_layout);
        String responseFromFb = getIntent().getStringExtra(Constants.FB_USER_INFO);
        if (responseFromFb != null && !responseFromFb.isEmpty()) {
            fbProfile = new Gson().fromJson(responseFromFb, FbProfile.class);
        } else {
            String responseFromDb = preferences.getString(Constants.FB_USER_INFO, null);
            if (responseFromDb != null && !responseFromDb.isEmpty()) {
                fbProfile = new Gson().fromJson(responseFromDb, FbProfile.class);
            }
        }
        new CallWebservice().execute();
        selectedInterests = new HashSet<>();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (Interest interest : mainList) {
                    if (interest.isSelected()) {
                        selectedInterests.add(interest.getInterest());

                    }
                }
                editor.putStringSet(Constants.PREFERENCES_SELECTED_INTERESTS, selectedInterests).commit();
                editor.putBoolean(Constants.PREFERENCES_INTEREST, false).commit();

                finish();
            }
        });

        boolean isFirstTime = preferences.getBoolean(Constants.PREFERENCES_INTEREST, true);
        if(isFirstTime)
        {
            cancel.setVisibility(View.GONE);
            submit.setText("Lets Get Started");
        }
        else
        {
            submit.setText("Save");
            cancel.setVisibility(View.VISIBLE);
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    class CallWebservice extends AsyncTask<Void, Void, List<Interest>> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(InterestActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Please wait..");
            progressDialog.setTitle("Getting Info");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected List<Interest> doInBackground(Void... voids) {/*

            boolean isFirstTime = preferences.getBoolean(Constants.PREFERENCES_INTEREST, true);

            Set<String> selectedInterest = preferences.getStringSet(Constants.PREFERENCES_SELECTED_INTERESTS, null);
            //Create request
            SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.INTEREST_LIST_METHOD_NAME);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;
            //Set output SOAP object
            envelope.setOutputSoapObject(request);


            //Create HTTP call object
            HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.INTEREST_LIST_URL);

            try {
                //Invole web service
                androidHttpTransport.call(Constants.INTEREST_LIST_ACTION, envelope);
                SoapObject resultRequestSOAP = (SoapObject) envelope.bodyIn;
                SoapObject root = (SoapObject) resultRequestSOAP.getProperty("IntrestListResult");
                int count = root.getPropertyCount();
                for (int i = 0; i < count; i++) {
                    Object property = root.getProperty(i);
                    Interest interest = new Interest();
                    boolean selected = false;
                    if (isFirstTime) {
                        selected = true;
                    } else {
                        if (selectedInterest != null)
                            selected = selectedInterest.contains(String.valueOf(property));
                    }
                    interest.setSelected(selected);
                    interest.setInterest(String.valueOf(property));
                    mainList.add(interest);
                }

                return mainList;


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;*/


            boolean isFirstTime = preferences.getBoolean(Constants.PREFERENCES_INTEREST, true);

            Set<String> selectedInterest = preferences.getStringSet(Constants.PREFERENCES_SELECTED_INTERESTS, null);
            //Create request
            SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.INTEREST_LIST_BY_ID_METHOD_NAME);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;
            //Set output SOAP object
            PropertyInfo userId = new PropertyInfo();
            userId.setName("UserId");
            userId.setValue(fbProfile.getFbUserId());
            userId.setType(String.class);

            request.addProperty(userId);
            envelope.setOutputSoapObject(request);
            //Create HTTP call object
            HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.INTEREST_LIST_BY_ID_URL);

            try {
                //Invole web service
                androidHttpTransport.call(Constants.INTEREST_LIST_BY_ID_ACTION, envelope);
                SoapObject resultRequestSOAP = (SoapObject) envelope.bodyIn;
                SoapObject root = (SoapObject) resultRequestSOAP.getProperty("SendIntrestListByUserIdResult");
                int count = root.getPropertyCount();
                for (int i = 0; i < count; i++) {
                    Object property = root.getProperty(i);
                    Interest interest = new Interest();
                    boolean selected = false;
                    if (isFirstTime) {
                        selected = true;
                    } else {
                        if (selectedInterest != null)
                            selected = selectedInterest.contains(String.valueOf(property));
                    }
                    interest.setSelected(selected);
                    interest.setInterest(String.valueOf(property));
                    mainList.add(interest);
                }

                return mainList;


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;


        }

        @Override
        protected void onPostExecute(List<Interest> strings) {
            super.onPostExecute(strings);
            try {

                if (strings != null) {

                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Button button = (Button) view;

                            int selected = Integer.parseInt(String.valueOf(view.getTag()));
                            Interest interest = mainList.get(selected);
                            if (interest.isSelected()) {
                                button.setBackgroundResource(R.drawable.custom_button);
                                button.setTextColor(getResources().getColor(R.color.statusbarcolor));
                                interest.setSelected(false);
                            } else {
                                button.setBackgroundResource(R.drawable.custom_button_for_white_background);
                                button.setTextColor(Color.WHITE);
                                interest.setSelected(true);
                            }
                            button.requestLayout();
                            mainList.set(selected, interest);

                        }
                    };
                    TableRow.LayoutParams params = new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 20, 20);

                    TableRow.LayoutParams paramsForTableRow = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                    paramsForTableRow.setMargins(10, 10, 10, 10);

                    for (int i = 0; i < strings.size(); i += 4) {
                        if (i + 4 <= strings.size()) {
                            TableRow tableRow = new TableRow(InterestActivity.this);
                            tableRow.setLayoutParams(paramsForTableRow);

                            Button button1 = new Button(InterestActivity.this);
                            button1.setLayoutParams(params);
                            button1.setTag(i);
                            button1.setText(strings.get(i).getInterest());
                            Interest interest = mainList.get(i);
                            if (!interest.isSelected()) {
                                button1.setBackgroundResource(R.drawable.custom_button);
                                button1.setTextColor(getResources().getColor(R.color.statusbarcolor));
                            } else {
                                button1.setBackgroundResource(R.drawable.custom_button_for_white_background);
                                button1.setTextColor(Color.WHITE);
                            }
                            button1.setOnClickListener(onClickListener);


                            Button button2 = new Button(InterestActivity.this);
                            button2.setLayoutParams(params);
                            button2.setTag(i+1);
                            button2.setText(strings.get(i + 1).getInterest());
                            Interest interest2 = mainList.get(i + 1);
                            if (!interest2.isSelected()) {
                                button2.setBackgroundResource(R.drawable.custom_button);
                                button2.setTextColor(getResources().getColor(R.color.statusbarcolor));
                            } else {
                                button2.setBackgroundResource(R.drawable.custom_button_for_white_background);
                                button2.setTextColor(Color.WHITE);
                            }

                            button2.setOnClickListener(onClickListener);


                            Button button3 = new Button(InterestActivity.this);
                            button3.setLayoutParams(params);
                            button3.setTag(i+2);

                            button3.setText(strings.get(i + 2).getInterest());
                            Interest interest3 = mainList.get(i + 2);
                            if (!interest3.isSelected()) {
                                button3.setBackgroundResource(R.drawable.custom_button);
                                button3.setTextColor(getResources().getColor(R.color.statusbarcolor));
                            } else {
                                button3.setBackgroundResource(R.drawable.custom_button_for_white_background);
                                button3.setTextColor(Color.WHITE);
                            }
                            button3.setOnClickListener(onClickListener);


                            Button button4 = new Button(InterestActivity.this);
                            button4.setLayoutParams(params);
                            button4.setTag(i+3);

                            button4.setText(strings.get(i + 3).getInterest());
                            Interest interest4 = mainList.get(i + 3);
                            if (!interest4.isSelected()) {
                                button4.setBackgroundResource(R.drawable.custom_button);
                                button4.setTextColor(getResources().getColor(R.color.statusbarcolor));
                            } else {
                                button4.setBackgroundResource(R.drawable.custom_button_for_white_background);
                                button4.setTextColor(Color.WHITE);
                            }

                            button4.setOnClickListener(onClickListener);


                            tableRow.addView(button1);
                            tableRow.addView(button2);
                            tableRow.addView(button3);
                            tableRow.addView(button4);

                            tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                        }
                    }

                    if (strings.size() % 4 != 0) {
                        int totalInterests = strings.size();
                        int remainingInterests = strings.size() % 4;
                        TableRow tableRow = new TableRow(InterestActivity.this);
                        tableRow.setLayoutParams(paramsForTableRow);
                        for (int i = 1; i <=remainingInterests; i++) {
                            Button button = new Button(InterestActivity.this);
                            button.setLayoutParams(params);
                            Interest interest = mainList.get(totalInterests - i);
                            button.setTag(totalInterests-i);
                            button.setText(strings.get(totalInterests - i).getInterest());
                            button.setOnClickListener(onClickListener);
                            if (!interest.isSelected()) {
                                button.setBackgroundResource(R.drawable.custom_button);
                                button.setTextColor(getResources().getColor(R.color.statusbarcolor));
                            } else {
                                button.setBackgroundResource(R.drawable.custom_button_for_white_background);
                                button.setTextColor(Color.WHITE);
                            }
                            tableRow.addView(button);

                        }
                        tableRow.setLayoutParams(paramsForTableRow);
                        tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                    }
                    tableLayout.requestLayout();


                   /* dataAdapter = new MyCustomAdapter(getApplicationContext(),
                            R.layout.interest_info, strings);
                    ListView listView = (ListView) findViewById(R.id.listView1);
                    // Assign adapter to ListView
                    listView.setAdapter(dataAdapter);
                    listView.requestLayout();*/


                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try
            {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

            }
            catch(Exception e)
            {

            }

        }
    }
}

class MyCustomAdapter extends ArrayAdapter<Interest> {

    public List<Interest> interestList;
    Context mContext;

    public MyCustomAdapter(Context context, int textViewResourceId,
                           List<Interest> interestList) {
        super(context, textViewResourceId, interestList);

        this.interestList = interestList;
        mContext = context;
    }

    private class ViewHolder {
        TextView code;
        CheckBox name;
    }

    @Override
    public int getCount() {

        return interestList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.interest_info, null);

            holder = new ViewHolder();
            //     holder.code = (TextView) convertView.findViewById(R.id.code);
            holder.name = (CheckBox) convertView.findViewById(R.id.checkBox);
            convertView.setTag(holder);

            holder.name.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    Interest interest = (Interest) cb.getTag();
                    interest.setSelected(cb.isChecked());
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Interest interest = interestList.get(position);
        //  holder.code.setText(interest.getInterest());
        holder.name.setText(interest.getInterest());
        holder.name.setChecked(interest.isSelected());
        holder.name.setTag(interest);

        return convertView;

    }
}





/*
package com.vis.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.vis.R;
import com.vis.beans.Interest;
import com.vis.utilities.Constants;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InterestActivity extends AppCompatActivity {

    SharedPreferences preferences;
    List<Interest> mainList;
    Set<String> selectedInterests;
    MyCustomAdapter dataAdapter;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);
        mainList = new ArrayList<Interest>();
        preferences = getSharedPreferences(Constants.PREFERENCES_NAME, MODE_PRIVATE);
        editor = preferences.edit();
        new CallWebservice().execute();
        Button submit = (Button) findViewById(R.id.submit);
        Button cancel = (Button) findViewById(R.id.cancel);
        selectedInterests = new HashSet<>();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Interest> interestList = dataAdapter.interestList;
                for (Interest interest : interestList) {
                    if (interest.isSelected()) {
                        selectedInterests.add(interest.getInterest());
                    }
                }
                editor.putStringSet(Constants.PREFERENCES_SELECTED_INTERESTS, selectedInterests).commit();
                editor.putBoolean(Constants.PREFERENCES_INTEREST, false).commit();

                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }

    class CallWebservice extends AsyncTask<Void, Void, List<Interest>> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(InterestActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("Please wait..");
            progressDialog.setTitle("Getting Info");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected List<Interest> doInBackground(Void... voids) {

            boolean isFirstTime = preferences.getBoolean(Constants.PREFERENCES_INTEREST, true);

            Set<String> selectedInterest = preferences.getStringSet(Constants.PREFERENCES_SELECTED_INTERESTS, null);
            //Create request
            SoapObject request = new SoapObject(Constants.NAMESPACE, Constants.INTEREST_LIST_METHOD_NAME);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;
            //Set output SOAP object
            envelope.setOutputSoapObject(request);


            //Create HTTP call object
            HttpTransportSE androidHttpTransport = new HttpTransportSE(Constants.INTEREST_LIST_URL);

            try {
                //Invole web service
                androidHttpTransport.call(Constants.INTEREST_LIST_ACTION, envelope);
                SoapObject resultRequestSOAP = (SoapObject) envelope.bodyIn;
                SoapObject root = (SoapObject) resultRequestSOAP.getProperty("IntrestListResult");
                int count = root.getPropertyCount();
                for (int i = 0; i < count; i++) {
                    Object property = root.getProperty(i);
                    Interest interest = new Interest();
                    boolean selected = false;
                    if (isFirstTime) {
                        selected = true;
                    } else {
                        if (selectedInterest != null)
                            selected = selectedInterest.contains(String.valueOf(property));
                    }
                    interest.setSelected(selected);
                    interest.setInterest(String.valueOf(property));
                    mainList.add(interest);
                }

                return mainList;


            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;


        }

        @Override
        protected void onPostExecute(List<Interest> strings) {
            super.onPostExecute(strings);
            try
            {
                if (progressDialog != null && progressDialog.isShowing() ) {
                    progressDialog.dismiss();
                }
                if (strings != null) {

                    dataAdapter = new MyCustomAdapter(getApplicationContext(),
                            R.layout.interest_info, strings);
                    ListView listView = (ListView) findViewById(R.id.listView1);
                    // Assign adapter to ListView
                    listView.setAdapter(dataAdapter);
                    listView.requestLayout();


                }
            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }
}

class MyCustomAdapter extends ArrayAdapter<Interest> {

    public List<Interest> interestList;
    Context mContext;

    public MyCustomAdapter(Context context, int textViewResourceId,
                           List<Interest> interestList) {
        super(context, textViewResourceId, interestList);

        this.interestList = interestList;
        mContext = context;
    }

    private class ViewHolder {
        TextView code;
        CheckBox name;
    }

    @Override
    public int getCount() {

        return interestList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.interest_info, null);

            holder = new ViewHolder();
            //     holder.code = (TextView) convertView.findViewById(R.id.code);
            holder.name = (CheckBox) convertView.findViewById(R.id.checkBox);
            convertView.setTag(holder);

            holder.name.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    Interest interest = (Interest) cb.getTag();
                    interest.setSelected(cb.isChecked());
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Interest interest = interestList.get(position);
        //  holder.code.setText(interest.getInterest());
        holder.name.setText(interest.getInterest());
        holder.name.setChecked(interest.isSelected());
        holder.name.setTag(interest);

        return convertView;

    }
}



*/
