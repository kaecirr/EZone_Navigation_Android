package com.example.kaelansinclair.ezone_navigation_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.resources.IAFloorPlan;
import com.indooratlas.android.sdk.resources.IALatLng;
import com.indooratlas.android.sdk.resources.IALocationListenerSupport;
import com.indooratlas.android.sdk.resources.IAResourceManager;
import com.indooratlas.android.sdk.resources.IAResult;
import com.indooratlas.android.sdk.resources.IAResultCallback;
import com.indooratlas.android.sdk.resources.IATask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final String PREFS_NAME = "IsLoggedIn";
    private static int PRIVATE_MODE = 0;
    private static boolean first = true;

    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private EditText edtSeach;
    private SearchView searchView;

    private static GoogleMap mMap;
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private static final int MAX_DIMENSION = 2048;
    private static final String TAG = "IADemo";

    private static final float HUE_IAGRN = 133.0f;

    private Marker mMarker;
    private CircleOptions mMarkerOptions;
    private CircleOptions mMarkerErrorOptions;
    //private Circle mMarker;
    private Circle mMarkerError;
    private Marker mPoint;
    private Marker mPoint2;
    private IARegion mOverlayFloorPlan = null;
    private GroundOverlay mGroundOverlay = null;
    private IATask<IAFloorPlan> mFetchFloorPlanTask;
    private Target mLoadTarget;
    private boolean mCameraPositionNeedsUpdating = true; // update on first location
    private IALocationManager mIALocationManager;
    private IAResourceManager mResourceManager;

    private static Set<Polyline> polyline;

    JSONObject jsonInner;

    JSONObject json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("EZone Indoor Navigation");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                      //  .setAction("Action", null).show();
                if (mMap != null && mMarker != null) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), 17.5f));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, PRIVATE_MODE);
        if (first) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("logged", false); // set it to false when the user is logged out
            editor.commit();
            first = false;
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        // prevent the screen going to sleep while app is on foreground
        findViewById(android.R.id.content).setKeepScreenOn(true);

        mIALocationManager = IALocationManager.create(this);
        mResourceManager = IAResourceManager.create(this);

        //IARegion r = IARegion.floorPlan("208ae45e-8d22-4faa-bfb2-e245f956de3b");

        //test(r);

        polyline = new HashSet<Polyline>();

        jsonInner = new JSONObject();
        try {
            jsonInner.put("building", "computerScience");
            jsonInner.put("floor", "second");
            jsonInner.put("startLongitude", "-31.97444473");
            jsonInner.put("startLatitude", "115.8599");
            jsonInner.put("endLongitude", "-31.97222274");
            jsonInner.put("endLatitude", "115.823");
            jsonInner.put("algorithm", "DJ");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        json = new JSONObject();
        try {
            json.put("requestMessage", "");
            json.put("mapDataRequest", jsonInner);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, PRIVATE_MODE);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        MenuItem item = navigationView.getMenu().findItem(R.id.nav_signin);
        if (prefs.getBoolean("logged", true)) { //user logged in before
            item.setTitle("Sign Out");
            item.setIcon(android.R.drawable.ic_menu_revert);
        }

        if (mMap == null) {
            SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
            mapFragment.getMapAsync(this);
        }

        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), mListener);
        mIALocationManager.registerRegionListener(mRegionListener);
        if (mMarker != null) {
            if (mCameraPositionNeedsUpdating) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), 17.5f));
                mCameraPositionNeedsUpdating = false;
            }
        }
    }

    @Override
    public void onPause() {

        super.onPause();

        mIALocationManager.removeLocationUpdates(mListener);
        mIALocationManager.registerRegionListener(mRegionListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIALocationManager.destroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        }else if(isSearchOpened) {
            handleMenuSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            //case R.id.action_settings:
               // return true;
            case R.id.action_search:
                handleMenuSearch();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void handleMenuSearch(){
        ActionBar action = getSupportActionBar(); //get the actionbar

        if(isSearchOpened){ //test if the search is open

            action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
            action.setDisplayShowTitleEnabled(true); //show the title in the action bar

            //hides the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtSeach.getWindowToken(), 0);

            //add the search icon in the action bar
            mSearchAction.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_search));

            isSearchOpened = false;
        } else { //open the search entry

            action.setDisplayShowCustomEnabled(true); //enable it to display a
            // custom view in the action bar.
            action.setCustomView(R.layout.search_bar);//add the custom view
            action.setDisplayShowTitleEnabled(false); //hide the title

            edtSeach = (EditText)action.getCustomView().findViewById(R.id.edtSearch); //the text editor

            //this is a listener to do a search when the user clicks on search button
            edtSeach.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        doSearch();
                        return true;
                    }
                    return false;
                }
            });


            edtSeach.requestFocus();

            //open the keyboard focused in the edtSearch
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edtSeach, InputMethodManager.SHOW_IMPLICIT);


            //add the close icon
            mSearchAction.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_close_clear_cancel));

            isSearchOpened = true;
        }
    }

    private void doSearch() {
        //
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_signin) {
            // Handle the camera action
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            if (prefs.getBoolean("logged", true)) { //user logged in before
                item.setTitle("Sign In");
                item.setIcon(R.drawable.common_full_open_on_phone);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("logged", false); // set it to false when the user is logged out
                editor.commit();
            }
            else startActivity(new Intent(MenuActivity.this, LoginActivity.class));
        } else if (id == R.id.nav_favourites) {

            JSONObject jsonInner = new JSONObject();
            try {
                jsonInner.put("building", "computerScience");
                jsonInner.put("floor", "second");
                jsonInner.put("startLongitude", "-31.97444473");
                jsonInner.put("startLatitude", "115.8599");
                jsonInner.put("endLongitude", "-31.97222274");
                jsonInner.put("endLatitude", "115.823");
                jsonInner.put("algorithm", "DJ");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JSONObject json = new JSONObject();
            try {
                json.put("requestMessage", "");
                json.put("mapDataRequest", jsonInner);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            TestRequest t = new TestRequest("http://52.64.190.66:8080/springMVC-1.0-SNAPSHOT/path", json.toString());

            t.execute();

        } else if (id == R.id.nav_recentlocations) {
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private IALocationListener mListener = new IALocationListenerSupport() {

        @Override
        public void onLocationChanged(IALocation location) {

            Log.d(TAG, "new location received with coordinates: " + location.getLatitude()
                    + "," + location.getLongitude());

            if (mMap == null) {
                // location received before map is initialized, ignoring update here
                return;
            }

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (mMarker == null) {
                // first location, add marker
                // mMarkerOptions = new CircleOptions().center(latLng).radius(0.3).strokeColor(Color.argb(255, 8, 0, 255)).fillColor(Color.argb(255, 0, 170, 255)).zIndex(100);
                // mMarker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_circle)));

                Drawable circleDrawable = getResources().getDrawable(R.drawable.marker_circle);
                BitmapDescriptor markerIcon = getMarkerIconFromDrawable(circleDrawable);
                mMarker = mMap.addMarker(new MarkerOptions().position(latLng).icon(markerIcon).zIndex(100).anchor(0.5f,0.5f));

                mMarkerErrorOptions = new CircleOptions().center(latLng).radius(location.getAccuracy()).strokeColor(Color.argb(255, 8, 0, 255)).strokeWidth(5).fillColor(Color.argb(128, 0, 170, 255)).zIndex(99);
                mMarkerError = mMap.addCircle(mMarkerErrorOptions);

                if (mPoint2 != null && mPoint2.isVisible()) {

                    try {
                        jsonInner.put("startLongitude", String.valueOf(mMarker.getPosition().longitude));
                        jsonInner.put("startLatitude", String.valueOf(mMarker.getPosition().latitude));
                        jsonInner.put("endLongitude", String.valueOf(mPoint2.getPosition().longitude));
                        jsonInner.put("endLatitude", String.valueOf(mPoint2.getPosition().latitude));

                        json.put("mapDataRequest", jsonInner);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    TestRequest t = new TestRequest("http://52.64.190.66:8080/springMVC-1.0-SNAPSHOT/path", json.toString());

                    t.execute();
                }
            } else {
                // move existing markers position to received location
                mMarkerError.setCenter(latLng);
                mMarkerError.setRadius(location.getAccuracy());
                mMarker.setPosition(latLng);


                if (mPoint2 != null && mPoint2.isVisible()) {


                    try {
                        jsonInner.put("startLongitude", String.valueOf(mMarker.getPosition().longitude));
                        jsonInner.put("startLatitude", String.valueOf(mMarker.getPosition().latitude));
                        jsonInner.put("endLongitude", String.valueOf(mPoint2.getPosition().longitude));
                        jsonInner.put("endLatitude", String.valueOf(mPoint2.getPosition().latitude));

                        json.put("mapDataRequest", jsonInner);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    TestRequest t = new TestRequest("http://52.64.190.66:8080/springMVC-1.0-SNAPSHOT/path", json.toString());

                    t.execute();
                }
            }

            // our camera position needs updating if location has significantly changed
            if (mCameraPositionNeedsUpdating) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.5f));
                mCameraPositionNeedsUpdating = false;
            }
        }

    };

    public static void drawPolyline(String response) {
        Log.d("quickDebug", response);
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("mapDataResponse")) {
                JSONObject jsonObject2 = (JSONObject) jsonObject.get("mapDataResponse");
                if (jsonObject2.has("path"))  {

                    JSONArray path = jsonObject2.getJSONArray("path");
                    Log.d("wtf", path.toString());
                    if (polyline != null) {
                        Log.d("wtf565", path.toString());
                        Iterator<Polyline> pol = polyline.iterator();
                        while (pol.hasNext()) pol.next().remove();
                    }
                    polyline = new HashSet<Polyline>();
                    Log.d("wtf3", String.valueOf(path.length()));
                    for (int i = 0; i < path.length() - 1; i++) {
                        Log.d("wtf", path.get(i).toString());
                        JSONObject sourceJSON = (JSONObject) path.get(i);
                        JSONObject targetJSON = (JSONObject) path.get(i + 1);
                        if (sourceJSON.has("latitude") && sourceJSON.has("longitude") && targetJSON.has("latitude") && targetJSON.has("longitude")) {
                            Log.d("help", (String) sourceJSON.get("latitude"));
                            Log.d("help2", (String) sourceJSON.get("longitude"));
                            Log.d("help3",  String.valueOf(Double.valueOf((String) targetJSON.get("latitude"))));
                            LatLng source = new LatLng(Double.valueOf((String) sourceJSON.get("latitude")), Double.valueOf((String) sourceJSON.get("longitude")));
                            LatLng target = new LatLng(Double.valueOf((String) targetJSON.get("latitude")), Double.valueOf((String) targetJSON.get("longitude")));
                            final PolylineOptions rectOptions = new PolylineOptions();
                            rectOptions.add(source, target).color(Color.RED);
                            polyline.add(mMap.addPolyline(rectOptions));
                            Polyline u = (Polyline) polyline.toArray()[0];
                            Log.d("wtf2", String.valueOf(source.latitude));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * Listener that changes overlay if needed
     */
    private IARegion.Listener mRegionListener = new IARegion.Listener() {

        @Override
        public void onEnterRegion(IARegion region) {
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                final String newId = region.getId();
                // Are we entering a new floor plan or coming back the floor plan we just left?
                if (mGroundOverlay == null || !region.equals(mOverlayFloorPlan)) {
                    mCameraPositionNeedsUpdating = true; // entering new fp, need to move camera
                    if (mGroundOverlay != null) {
                        mCameraPositionNeedsUpdating = false;
                        mGroundOverlay.remove();
                        mGroundOverlay = null;
                    }
                    mOverlayFloorPlan = region; // overlay will be this (unless error in loading)
                    fetchFloorPlan(newId);
                } else {
                    mGroundOverlay.setTransparency(0.0f);
                }
            }
           // showInfo("Enter " + (region.getType() == IARegion.TYPE_VENUE
             //       ? "VENUE "
               //     : "FLOOR_PLAN ") + region.getId());
        }

        @Override
        public void onExitRegion(IARegion region) {
            if (mGroundOverlay != null) {
                // Indicate we left this floor plan but leave it there for reference
                // If we enter another floor plan, this one will be removed and another one loaded
                mGroundOverlay.setTransparency(0.5f);
            }
           // showInfo("Enter " + (region.getType() == IARegion.TYPE_VENUE
             //       ? "VENUE "
               //     : "FLOOR_PLAN ") + region.getId());
        }

    };

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private GoogleMap.OnMapClickListener mClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            if (mMap != null) {
/*
                if (mPoint == null && mPoint2 == null) {
                    // first location, add marker

                    Drawable circleDrawable = getResources().getDrawable(R.drawable.marker_circle);
                    BitmapDescriptor markerIcon = getMarkerIconFromDrawable(circleDrawable);
                    mPoint = mMap.addMarker(new MarkerOptions().position(latLng).icon(markerIcon).zIndex(100));

                }
                */

                if (mPoint2 == null) {
                    mPoint2 = mMap.addMarker(new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory.defaultMarker(HUE_IAGRN)));

                    if (mPoint2 != null && mPoint2.isVisible()) {
/*
                        try {
                            jsonInner.put("startLongitude", String.valueOf(mPoint.getPosition().longitude));
                            jsonInner.put("startLatitude", String.valueOf(mPoint.getPosition().latitude));
                            jsonInner.put("endLongitude", String.valueOf(mPoint2.getPosition().longitude));
                            jsonInner.put("endLatitude", String.valueOf(mPoint2.getPosition().latitude));

                            json.put("mapDataRequest", jsonInner);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d("quickDebug", json.toString());
                        TestRequest t = new TestRequest("http://52.64.190.66:8080/springMVC-1.0-SNAPSHOT/path", json.toString());

                        t.execute();
                        */
                    }
                }
                else if (mPoint2 != null) {
                    if (mPoint2.isVisible()) {
                        mPoint2.setVisible(false);
                        //mPoint.setPosition(latLng);
                        if (polyline != null) {
                            Iterator<Polyline> pol = polyline.iterator();
                            while (pol.hasNext()) pol.next().remove();;
                        }
                    }
                    else {
                        // move existing markers position to received location
                        mPoint2.setPosition(latLng);
                        mPoint2.setVisible(true);
                        //mMap.setMyLocationEnabled(false);

                        if (mPoint2 != null && mPoint2.isVisible()) {
/*
                            try {
                                jsonInner.put("startLongitude", String.valueOf(mPoint.getPosition().longitude));
                                jsonInner.put("startLatitude", String.valueOf(mPoint.getPosition().latitude));
                                jsonInner.put("endLongitude", String.valueOf(mPoint2.getPosition().longitude));
                                jsonInner.put("endLatitude", String.valueOf(mPoint2.getPosition().latitude));

                                json.put("mapDataRequest", jsonInner);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("quickDebug", json.toString());
                            TestRequest t = new TestRequest("http://52.64.190.66:8080/springMVC-1.0-SNAPSHOT/path", json.toString());

                            t.execute();
                            */
                        }
                    }
                }
            }
        }
    };

    public void test(IARegion region) {
        if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
            final String newId = region.getId();
            // Are we entering a new floor plan or coming back the floor plan we just left?
            if (mGroundOverlay == null || !region.equals(mOverlayFloorPlan)) {
                mCameraPositionNeedsUpdating = true; // entering new fp, need to move camera
                if (mGroundOverlay != null) {
                    mGroundOverlay.remove();
                    mGroundOverlay = null;
                }
                mOverlayFloorPlan = region; // overlay will be this (unless error in loading)
                fetchFloorPlan(newId);
            } else {
                mGroundOverlay.setTransparency(0.0f);
            }
        }
       // showInfo("Enter " + (region.getType() == IARegion.TYPE_VENUE
         //       ? "VENUE "
           //     : "FLOOR_PLAN ") + region.getId());
    }

    /**
     * Checks that we have access to required information, if not ask for users permission.
     */
    private void ensurePermissions() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // we don't have access to coarse locations, hence we have not access to wifi either
            // check if this requires explanation to user
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle(R.string.location_permission_request_title)
                        .setMessage(R.string.location_permission_request_rationale)
                        .setPositiveButton(R.string.permission_button_accept, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "request permissions");
                                ActivityCompat.requestPermissions(MenuActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .setNegativeButton(R.string.permission_button_deny, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MenuActivity.this,
                                        R.string.location_permission_denied_message,
                                        Toast.LENGTH_LONG).show();
                            }
                        })
                        .show();

            } else {

                // ask user for permission
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);

            }

        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ensurePermissions();
        }
        else {
            map.setMyLocationEnabled(false);
        }

        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setOnMapClickListener(mClickListener);
        mMap = map;
    }

    /**
     * Sets bitmap of floor plan as ground overlay on Google Maps
     */
    private void setupGroundOverlay(IAFloorPlan floorPlan, Bitmap bitmap) {

        if (mGroundOverlay != null) {
            mGroundOverlay.remove();
        }

        if (mMap != null) {
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            IALatLng iaLatLng = floorPlan.getCenter();
            LatLng center = new LatLng(iaLatLng.latitude, iaLatLng.longitude);
            GroundOverlayOptions fpOverlay = new GroundOverlayOptions()
                    .image(bitmapDescriptor)
                    .position(center, floorPlan.getWidthMeters(), floorPlan.getHeightMeters())
                    .bearing(floorPlan.getBearing());

            mGroundOverlay = mMap.addGroundOverlay(fpOverlay);
        }
    }

    /**
     * Download floor plan using Picasso library.
     */
    private void fetchFloorPlanBitmap(final IAFloorPlan floorPlan) {

        final String url = floorPlan.getUrl();

        if (mLoadTarget == null) {
            mLoadTarget = new Target() {

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Log.d(TAG, "onBitmap loaded with dimensions: " + bitmap.getWidth() + "x"
                            + bitmap.getHeight());
                    setupGroundOverlay(floorPlan, bitmap);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    // N/A
                }

                @Override
                public void onBitmapFailed(Drawable placeHolderDraweble) {
                    showInfo("Failed to load bitmap");
                    mOverlayFloorPlan = null;
                }
            };
        }

        RequestCreator request = Picasso.with(this).load(url);

        final int bitmapWidth = floorPlan.getBitmapWidth();
        final int bitmapHeight = floorPlan.getBitmapHeight();

        if (bitmapHeight > MAX_DIMENSION) {
            request.resize(0, MAX_DIMENSION);
        } else if (bitmapWidth > MAX_DIMENSION) {
            request.resize(MAX_DIMENSION, 0);
        }

        request.into(mLoadTarget);
    }

    /**
     * Fetches floor plan data from IndoorAtlas server.
     */
    private void fetchFloorPlan(String id) {

        // if there is already running task, cancel it
        cancelPendingNetworkCalls();

        final IATask<IAFloorPlan> task = mResourceManager.fetchFloorPlanWithId(id);

        task.setCallback(new IAResultCallback<IAFloorPlan>() {

            @Override
            public void onResult(IAResult<IAFloorPlan> result) {

                if (result.isSuccess() && result.getResult() != null) {
                    // retrieve bitmap for this floor plan metadata
                    fetchFloorPlanBitmap(result.getResult());
                } else {
                    // ignore errors if this task was already canceled
                    if (!task.isCancelled()) {
                        // do something with error
                        showInfo("Loading floor plan failed: " + result.getError());
                        mOverlayFloorPlan = null;
                    }
                }
            }
        }, Looper.getMainLooper()); // deliver callbacks using main looper

        // keep reference to task so that it can be canceled if needed
        mFetchFloorPlanTask = task;

    }

    /**
     * Helper method to cancel current task if any.
     */
    private void cancelPendingNetworkCalls() {
        if (mFetchFloorPlanTask != null && !mFetchFloorPlanTask.isCancelled()) {
            mFetchFloorPlanTask.cancel();
        }
    }

    private void showInfo(String text) {
        final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), text,
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.button_close, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
}
