package com.example.kaelansinclair.ezone_navigation_android;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.kaelansinclair.ezone_navigation_android.Map.setFloorPlans;
import static com.example.kaelansinclair.ezone_navigation_android.Map.drawPolyline;
import static com.example.kaelansinclair.ezone_navigation_android.Map.mapInitialisation;
import static com.example.kaelansinclair.ezone_navigation_android.Map.setRooms;

/**
 * Created by kaelan on 29/05/17.
 */

public class BackendRequest extends AsyncTask<Void, Void, String> {

    private String URL = "http://52.64.190.66:8080/springMVC-1.0-SNAPSHOT/";

    private Exception exception;

    private String apiCall;
    private String query;
    private boolean init;

    public BackendRequest(String u, String q, boolean init) {
        apiCall = u;
        query = q;
        this.init = init;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    //"{ exampleObject: \"name\" }"
    @Override
    protected String doInBackground(Void... params) {

        try {
            String response = makePostRequest(URL + apiCall, query);
            Log.d("INFO", response);
            return response;
        } catch (IOException ex) {
            ex.printStackTrace();
            return "";
        }
    }

    @Override
    protected void onPostExecute(String response) {
        if(response == null) {
            response = "THERE WAS AN ERROR";
        }
        else if (apiCall.equals("path")) drawPolyline(response);
        else if (apiCall.equals("floorPlan")) {
            if (init) mapInitialisation(response);
            else setFloorPlans(response);
        }
        else if (apiCall.equals("rooms")) setRooms(response);

        Log.d("INFO5", response);
    }

    public static String makePostRequest(String stringUrl, String payload) throws IOException {
        String mes = "";
        try {
            URL url = new URL(stringUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());

            os.write(payload.getBytes());
            os.flush();
            os.close();

            Log.d("INFO", String.valueOf(conn.getResponseCode()));

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            StringBuilder response = new StringBuilder();
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            mes = response.toString();
            Log.d("INFO", payload);
            Log.d("INFO", mes);
            conn.disconnect();

        }catch (Exception e) {
            e.printStackTrace();

        }
        return mes;
    }
}
