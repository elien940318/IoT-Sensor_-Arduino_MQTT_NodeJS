package com.example.iot;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new LoadSensorLogs().execute("arduino", "sensorlog");
    }
    class Item{
        int temp, humidity, distance, lux;
        String created_at;
        Item(int temp, int humidity, int distance, int lux, String created_at){
            this.temp=temp;
            this.humidity=humidity;
            this.distance = distance;
            this.lux = lux;
            this.created_at=created_at;
        }
    }
    ArrayList<Item> items=new ArrayList<Item>();
    class ItemAdapter extends ArrayAdapter{
        public ItemAdapter(Context context) {
            super(context, R.layout.list_sensor_item, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view=convertView;
            if(view==null){
                LayoutInflater inflater=
                        (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                view=inflater.inflate(R.layout.list_sensor_item, null);
            }
            TextView tempText=view.findViewById(R.id.temp);
            TextView humidityText=view.findViewById(R.id.humidity);
            TextView distanceText=view.findViewById(R.id.distance);
            TextView luxText=view.findViewById(R.id.lux);
            TextView createdAtText=view.findViewById(R.id.created_at);
            tempText.setText(items.get(position).temp+"[℃]");
            humidityText.setText(items.get(position).humidity+"[%]");
            distanceText.setText(items.get(position).distance+"[cm]");
            luxText.setText(items.get(position).lux+"[lx]");
            createdAtText.setText(items.get(position).created_at);
            return view;
        }
    }
    class LoadSensorLogs extends AsyncTask<String, String, String>{
        ProgressDialog dialog=new ProgressDialog(MainActivity.this);
        @Override
        protected String doInBackground(String... strings) {
            StringBuffer response=new StringBuffer();
            try {
                // http://172.30.1.15:3000/devices/arduino/dht11 이런식으로 들어가게 될것으로 보임...
                String apiURL="http://192.168.10.5:3000/devices/"+strings[0]+"/"+strings[1];
                URL url=new URL(apiURL);
                HttpURLConnection con= (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                int responseCode=con.getResponseCode();
                BufferedReader br;
                if(responseCode==200) {
                    br=new BufferedReader(new InputStreamReader(
                            con.getInputStream()));
                }else{
                    br=new BufferedReader(new InputStreamReader(
                            con.getErrorStream()));
                }
                String inputLine;
                while((inputLine=br.readLine())!=null){
                    response.append(inputLine);
                }
                br.close();
            }catch(Exception e){
                e.printStackTrace();
            }
            Log.i(response.toString(),"ooooooooo");
            return response.toString();
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("센서 로그 정보 수신 중....");
            dialog.show();
        }
        @Override
        protected void onPostExecute(String s) {
            dialog.dismiss();
            try {
                JSONArray array=new JSONArray(s);
                items.clear();
                for(int i=0; i<array.length();i++){
                    JSONObject obj=array.getJSONObject(i);
                    items.add(new Item(obj.getInt("tmp"),
                                        obj.getInt("hum"),
                                        obj.getInt("dis"),
                                        obj.getInt("lux"),
                                        obj.getString("created_at")));
                }//_for
                ItemAdapter adapter=new ItemAdapter(MainActivity.this);
                ListView listView=(ListView)findViewById(R.id.listview);
                listView.setAdapter(adapter);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void clickLedOnButton(View view) {
        new SendLedOnOff().execute("led","on");
    }

    public void clickLedOffButton(View view) {
        new SendLedOnOff().execute("led","off");
    }

    class SendLedOnOff extends AsyncTask<String, String, String>{
        ProgressDialog dialog=new ProgressDialog(MainActivity.this);
        @Override
        protected String doInBackground(String... strings) {
            StringBuffer response=new StringBuffer();
            try {
                // http://172.30.1.15:3000/devices/led/on
                String apiURL="http://192.168.10.5:3000/devices/"+strings[0]+"/"+strings[1];
                URL url=new URL(apiURL);
                HttpURLConnection con= (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                int responseCode=con.getResponseCode();
                BufferedReader br;
                if(responseCode==200) {
                    br=new BufferedReader(new InputStreamReader(
                            con.getInputStream()));
                }else{
                    br=new BufferedReader(new InputStreamReader(
                            con.getErrorStream()));
                }
                String inputLine;
                while((inputLine=br.readLine())!=null){
                    response.append(inputLine);
                }
                br.close();
            }catch(Exception e){
                e.printStackTrace();
            }
            return response.toString();
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("LED 상태 정보 수신 중....");
            dialog.show();
        }
        @Override
        protected void onPostExecute(String s) {
            dialog.dismiss();
        }
    }
}
