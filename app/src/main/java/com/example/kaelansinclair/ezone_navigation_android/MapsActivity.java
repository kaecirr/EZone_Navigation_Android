package com.example.kaelansinclair.ezone_navigation_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

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
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("mapDataResponse")) {
                if (jsonObject.has("path"))  {
                    JSONObject[] path = (JSONObject[]) jsonObject.get("path");
                    if (polyline != null) {
                        Iterator<Polyline> pol = polyline.iterator();
                        while (pol.hasNext()) pol.next().remove();;
                    }
                    for (int i = 0; i < path.length - 1; i++) {
                        if (path[i].has("latitude") && path[i].has("longitude") && path[i + 1].has("latitude") && path[i + 1].has("longitude")) {
                            LatLng source = new LatLng(Double.parseDouble((String) path[i].get("latitude")), Double.parseDouble((String) path[i].get("longitude")));
                            LatLng target = new LatLng(Double.parseDouble((String) path[i + 1].get("latitude")), Double.parseDouble((String) path[i + 1].get("longitude")));
                            final PolylineOptions rectOptions = new PolylineOptions();
                            rectOptions.add(source, target).color(Color.RED);
                            polyline.add(mMap.addPolyline(rectOptions));
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
            showInfo("Enter " + (region.getType() == IARegion.TYPE_VENUE
                    ? "VENUE "
                    : "FLOOR_PLAN ") + region.getId());
        }

        @Override
        public void onExitRegion(IARegion region) {
            if (mGroundOverlay != null) {
                // Indicate we left this floor plan but leave it there for reference
                // If we enter another floor plan, this one will be removed and another one loaded
                mGroundOverlay.setTransparency(0.5f);
            }
            showInfo("Enter " + (region.getType() == IARegion.TYPE_VENUE
                    ? "VENUE "
                    : "FLOOR_PLAN ") + region.getId());
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

                if (mPoint == null && mPoint2 == null) {
                    // first location, add marker

                    Drawable circleDrawable = getResources().getDrawable(R.drawable.marker_circle);
                    BitmapDescriptor markerIcon = getMarkerIconFromDrawable(circleDrawable);
                    mPoint = mMap.addMarker(new MarkerOptions().position(latLng).icon(markerIcon).zIndex(100));

                }

                else if (mPoint2 == null) {
                    mPoint2 = mMap.addMarker(new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory.defaultMarker(HUE_IAGRN)));

                    if (mPoint2 != null && mPoint2.isVisible()) {

                        try {
                            jsonInner.put("startLongitude", String.valueOf(mPoint.getPosition().longitude));
                            jsonInner.put("startLatitude", String.valueOf(mPoint.getPosition().latitude));
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
                else if (mPoint2 != null) {
                    if (mPoint2.isVisible()) {
                        mPoint2.setVisible(false);
                        mPoint.setPosition(latLng);
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

                            try {
                                jsonInner.put("startLongitude", String.valueOf(mPoint.getPosition().longitude));
                                jsonInner.put("startLatitude", String.valueOf(mPoint.getPosition().latitude));
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
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        // prevent the screen going to sleep while app is on foreground
        findViewById(android.R.id.content).setKeepScreenOn(true);

        mIALocationManager = IALocationManager.create(this);
        mResourceManager = IAResourceManager.create(this);

        IARegion r = IARegion.floorPlan("208ae45e-8d22-4faa-bfb2-e245f956de3b");

        test(r);

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
        showInfo("Enter " + (region.getType() == IARegion.TYPE_VENUE
                ? "VENUE "
                : "FLOOR_PLAN ") + region.getId());
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
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .setNegativeButton(R.string.permission_button_deny, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MapsActivity.this,
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
    protected void onDestroy() {
        super.onDestroy();
        mIALocationManager.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
    public void onMapReady(GoogleMap map) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ensurePermissions();
        }
        else {
            map.setMyLocationEnabled(true);
        }

        map.getUiSettings().setMyLocationButtonEnabled(true);
        //map.setOnMapClickListener(mClickListener);
        mMap = map;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIALocationManager.removeLocationUpdates(mListener);
        mIALocationManager.registerRegionListener(mRegionListener);
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
