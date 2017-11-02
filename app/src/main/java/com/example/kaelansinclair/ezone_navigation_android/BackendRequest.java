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
import static com.example.kaelansinclair.ezone_navigation_android.Map.setRoomsInit;

/**
 * Performs the backend server requests as an asynchronous task.
 */

public class BackendRequest extends AsyncTask<Void, Void, String> {

    // The URL to the backend server
    private String URL = "http://52.64.190.66:8080/springMVC-1.0-SNAPSHOT/";

    private Exception exception;

    private String apiCall;
    private String query;
    private boolean init;

    /**
     * Constructor for the BackendRequest.
     * @param u the API call being made
     * @param q the query to send
     * @param init if this is an initialisation call (only on application startup)
     */
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
        Log.d("INFO5", response);
        if(response == null) {
            response = "THERE WAS AN ERROR";
            // TODO: 3/11/2017
        }
        else if (response.equals("500") || response.equals("400")) this.execute(); // Retry on failure TODO: Set up a maximum limit on this
        else if (apiCall.equals("path")) drawPolyline(response); // Draw path
        else if (apiCall.equals("floorPlan")) {
            if (init) mapInitialisation(response); // On startup initialisation, initialise the map
            else setFloorPlans(response); // Set the focused building floor plan IDs
        }
        else if (apiCall.equals("rooms")) {
            if (init) setRoomsInit(response); // On startup get list of rooms for on device search. Should be implemented off device and on the server
            else setRooms(response); // Set the rooms for the building
        }
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
