package com.example.rajvi.musiceventapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import Data.CustomListViewAdapter;
import Util.Prefs;
import Util.utils;
import model.Event;

public class MainActivity extends AppCompatActivity {

    private CustomListViewAdapter adapter;
    private ArrayList<Event> listEvents = new ArrayList<Event>();
    private ListView listView;
    private TextView selectedCity;
    ProgressDialog progressDialog;
    Prefs prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.list);
        selectedCity = (TextView) findViewById(R.id.selectedLocationText);
        adapter = new CustomListViewAdapter(MainActivity.this, R.layout.list_row, listEvents);
        listView.setAdapter(adapter);
        prefs = new Prefs(MainActivity.this);
        String city = prefs.getCity();
        if(city!="")
            selectedCity.setText("Selected City: " + city);
        getEvents(city);
    }

    private void getEvents(String city) {
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        String url = utils.URL_LEFT + city + utils.URL_RIGHT;
        JsonArrayRequest eventsRequest = new JsonArrayRequest(Request.Method.GET,
                url, (JSONArray) null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    listEvents.clear();
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject eventObject = response.getJSONObject(i);

                        // artists Object
                        JSONArray artistsArray = eventObject.getJSONArray("artists");
                        JSONObject artistObject = artistsArray.getJSONObject(0);
                        String artistName = artistObject.getString("name");

                        // Venue Object
                        JSONObject venueObject = eventObject.getJSONObject("venue");
                        String venueName = venueObject.getString("name");

                        //Location Object
                        String city = venueObject.getString("city");
                        String country = venueObject.getString("country");


                        // Get started date
                        String startedDate = eventObject.getString("datetime");

                        // Get website
                        String website = eventObject.getString("url");

                        Event event = new Event();
                        event.setHeadLiner(artistName);
                        event.setCity(city);
                        event.setVenueName(venueName);
                        event.setCountry(country);
                        event.setStartDate(startedDate);
                        event.setWebsite(website);

                        // Get URL Image
                        getImageURL(artistName, event, i, response.length());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        AppController.getInstance().addToRequestQueue(eventsRequest);
    }


    private void getImageURL(String artistName, final Event event, final int position, final int size) {
        String URL = utils.IMAGE_URL_LEFT + artistName + utils.IMAGE_URL_RIGHT;
        URL = URL.replace(" ", "%20");
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, URL, (JSONObject) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            String imageURL = response.getString("image_url");
                            event.setUrl(imageURL);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        listEvents.add(event);
                        if (position == size - 1)
                            adapter.notifyDataSetChanged();
                        hideProgressDialog();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        AppController.getInstance().addToRequestQueue(objectRequest);
    }

    private void hideProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
            progressDialog = null;
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
        if (id == R.id.change_locationId) {
            showDialogInput();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDialogInput() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.change_location);
        final EditText cityInput = new EditText(MainActivity.this);
        cityInput.setHint(prefs.getCity());
        cityInput.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(cityInput);
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (cityInput.getText().toString().equalsIgnoreCase(""))
                    return;
                Log.v("Return",""+cityInput.getText().toString());
                String newCity = cityInput.getText().toString().trim();
                prefs.setCity(newCity);
                getEvents(newCity);
                selectedCity.setText("Selected City: " + newCity);
            }
        });
        builder.create().show();
    }

}
