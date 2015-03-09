package de.peculator.wlan_fingerprinting;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment(getApplicationContext()))
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        WifiManager wifi;
        ListView lv;
        TextView textStatus;
        Button buttonAnalyse;
        int size = 0;
        List<ScanResult> results;
        List<MyScanResults> myScanResults;
        public static int currentPlaceIndex=0;

        String ITEM_KEY = "key";
        ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();
        SimpleAdapter adapter;
        Context context;
        TextView tvAnalyse;

        public PlaceholderFragment(){}

        public PlaceholderFragment(Context applicationContext) {
            context = applicationContext;
            myScanResults = new LinkedList<MyScanResults>();
            }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            tvAnalyse = (TextView) rootView.findViewById(R.id.analyzeResults);

                    textStatus = (TextView) rootView.findViewById(R.id.textStatus);
            buttonAnalyse = (Button) rootView.findViewById(R.id.buttonScan);
            buttonAnalyse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    arraylist.clear();
                    currentPlaceIndex = -1;

                    wifi.startScan();

                    Toast.makeText(context, "Analyzing....", Toast.LENGTH_SHORT).show();
                    textStatus.setText("Analyzing...");
                    updateListView();
                }
            });
            lv = (ListView) rootView.findViewById(R.id.list);

            wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifi.isWifiEnabled() == false) {
                Toast.makeText(context, "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
                textStatus.setText("wifi is disabled..making it enabled");
                wifi.setWifiEnabled(true);
            }


            this.adapter = new SimpleAdapter(context, arraylist, R.layout.row, new String[] { ITEM_KEY }, new int[] { R.id.list_value });
            lv.setAdapter(this.adapter);

            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    results = wifi.getScanResults();
                    size = results.size();
                    textStatus.setText("Data received: " + size);
                    if(currentPlaceIndex!=-1)
                        myScanResults.add(new MyScanResults(currentPlaceIndex,results));
                    else{
                        analyze();
                    }
                    updateListView();
                }
            }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

            LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.linearLayout);
            String [] places = getResources().getStringArray(R.array.locations);

            for (int i = 0; i < places.length; i++) {
                Button b = new Button(context);
                b.setLayoutParams(new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                b.setText(places[i]);
                final int finalI = i;
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentPlaceIndex = finalI;
                        arraylist.clear();
                        Toast.makeText(context, "Scanning....", Toast.LENGTH_SHORT).show();
                        textStatus.setText("Scanning...");
                        updateListView();
                        wifi.startScan();
                    }
                });


                ll.addView(b);
            }

            return rootView;
        }

        private void analyze() {
            int[] matches = new int[myScanResults.size()];

            //Count matches

            for (int i = 0; i < myScanResults.size(); i++) {
                int localMatches = 0;

                for (int j = 0; j < results.size(); j++) {
                    for (int k = 0; k < myScanResults.get(i).getResults().size(); k++) {
                        if(results.get(j).SSID == myScanResults.get(i).getResults().get(k).SSID)
                            localMatches++;
                    }
                }
                matches[i]= localMatches;
            }
                         //Return best match

            int maxMatches = 0;
            int index = -1;

            for (int i = 0; i < matches.length; i++) {
                Log.i("my", matches[i] + " ");
                if(matches[i]>= maxMatches){
                    maxMatches = matches[i];
                    index = i;
                }
            }

            if(index!=-1)
                tvAnalyse.setText(getResources().getStringArray(R.array.locations)[myScanResults.get(index).getPlaceID()]);
        }

        public void updateListView(){
            try {
                size = size - 1;
                while (size >= 0) {
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put(ITEM_KEY, results.get(size).SSID + ": " + results.get(size).level +" ("+ results.get(size).frequency +")");

                    arraylist.add(item);
                    size--;
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
            }
        }


    }
}
