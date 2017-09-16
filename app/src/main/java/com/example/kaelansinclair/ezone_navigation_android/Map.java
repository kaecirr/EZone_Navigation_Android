package com.example.kaelansinclair.ezone_navigation_android;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Kaelan Sinclair on 27/08/2017.
 */

public class Map implements OnMapReadyCallback {

    private static GoogleMap mMap;
    private static Set<Polyline> polyline;
    private static final float HUE_IAGRN = 133.0f;

    private MainActivity mActivity;
    private boolean mCameraPositionNeedsUpdating = true; // update on first location

    private Marker mMarker;
    private CircleOptions mMarkerErrorOptions;
    private Circle mMarkerError;
    private Marker mPoint;
    private Marker mPoint2;

    private JSONObject jsonInner;

    private JSONObject json;

    public Map(MainActivity mActivity) {
        this.mActivity = mActivity;
        this.json = mActivity.json;
        this.jsonInner = mActivity.jsonInner;

        polyline = new HashSet<Polyline>();
    }

    public static GoogleMap getMap() {return mMap;}

    public void setCameraPositionNeedsUpdating(Boolean needsUpdating) {mCameraPositionNeedsUpdating = needsUpdating;}

    public void updateCameraPosition() {
        if (mMap != null && mMarker != null && mCameraPositionNeedsUpdating) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), 17.5f));
            mCameraPositionNeedsUpdating = false;
        }
    }

    public void updateLocation(LatLng latLng, double accuracy) {
        if (mMap == null) {
            // location received before map is initialized, ignoring update here
            return;
        }

        if (mMarker == null) {
            // first location, add marker
            // mMarkerOptions = new CircleOptions().center(latLng).radius(0.3).strokeColor(Color.argb(255, 8, 0, 255)).fillColor(Color.argb(255, 0, 170, 255)).zIndex(100);
            // mMarker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_circle)));

            Drawable circleDrawable = mActivity.getResources().getDrawable(R.drawable.marker_circle);
            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(circleDrawable);
            mMarker = mMap.addMarker(new MarkerOptions().position(latLng).icon(markerIcon).zIndex(100).anchor(0.5f,0.5f));

            mMarkerErrorOptions = new CircleOptions().center(latLng).radius(accuracy).strokeColor(Color.argb(255, 8, 0, 255)).strokeWidth(5).fillColor(Color.argb(128, 0, 170, 255)).zIndex(99);
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
            mMarkerError.setRadius(accuracy);
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

    @Override
    public void onMapReady(GoogleMap map) {
        if (ContextCompat.checkSelfPermission(mActivity.getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            mActivity.ensurePermissions();
        }
        else {
            map.setMyLocationEnabled(false);
        }

        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setOnMapClickListener(mClickListener);
        mMap = map;
    }

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

                    Drawable circleDrawable = mActivity.getResources().getDrawable(R.drawable.marker_circle);
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

}
