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



