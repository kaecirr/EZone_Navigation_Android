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
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.indooratlas.android.sdk.IARegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Kaelan Sinclair on 27/08/2017.
 */

public class Map implements OnMapReadyCallback {

    private static GoogleMap mMap;
    private static String pathResponse;
    private static Set<Polyline> polyline;
    private static final float HUE_IAGRN = 133.0f;

    private static MainActivity mActivity;
    private boolean mCameraPositionNeedsUpdating = true; // update on first location

    private Marker mMarker;
    private CircleOptions mMarkerErrorOptions;
    private Circle mMarkerError;
    private Marker mPoint;
    private Marker mPoint2;

    private static GroundOverlay focusedGroundOverlay = null;
    private String focusedBuilding;
    private int focusedFloor;

    private static HashMap<GroundOverlay, String> buildingOverlays;

    private static ArrayList<String> focusedBuildingFloorPlans;
    private static int groundReference;

    private JSONObject jsonInnerMapData;
    private JSONObject jsonInnerFloorPlan;
    private JSONObject jsonMapData;
    private JSONObject jsonFloorPlan;


    public Map(MainActivity mActivity) {
        this.mActivity = mActivity;;

        jsonInnerMapData = new JSONObject();
        try {
            jsonInnerMapData.put("startBuilding", "computerScience");
            jsonInnerMapData.put("startFloor", "second");
            jsonInnerMapData.put("startLongitude", "-31.97444473");
            jsonInnerMapData.put("startLatitude", "115.8599");
            jsonInnerMapData.put("endBuilding", "computerScience");
            jsonInnerMapData.put("endFloor", "second");
            jsonInnerMapData.put("endLongitude", "-31.97222274");
            jsonInnerMapData.put("endLatitude", "115.823");
            jsonInnerMapData.put("algorithm", "DJ");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonMapData = new JSONObject();
        try {
            jsonMapData.put("requestMessage", "");
            jsonMapData.put("mapDataRequest", jsonInnerMapData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonInnerFloorPlan = new JSONObject();
        try {
            jsonInnerFloorPlan.put("buildingName", "computerScience");
            //jsonInnerFloorPlan.put("floor", "2");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonFloorPlan = new JSONObject();
        try {
            jsonFloorPlan.put("requestMessage", "");
            jsonFloorPlan.put("floorPlan", jsonInnerFloorPlan);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pathResponse = "";
        polyline = new HashSet<Polyline>();
        buildingOverlays = new HashMap<GroundOverlay, String>();
        focusedBuildingFloorPlans = new ArrayList<String>();
        focusedBuilding = "";
        focusedFloor = 0;
        groundReference = 0;
    }

    public static GoogleMap getMap() {return mMap;}

    public GroundOverlay getFocusedGroundOverlay() {return focusedGroundOverlay;}

    public void setFocusedGroundOverlay(GroundOverlay newGroundOverlay) {focusedGroundOverlay = newGroundOverlay;}

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
                    jsonInnerMapData.put("startLongitude", String.valueOf(mMarker.getPosition().longitude));
                    jsonInnerMapData.put("startLatitude", String.valueOf(mMarker.getPosition().latitude));
                    jsonInnerMapData.put("endLongitude", String.valueOf(mPoint2.getPosition().longitude));
                    jsonInnerMapData.put("endLatitude", String.valueOf(mPoint2.getPosition().latitude));

                    jsonMapData.put("requestMessage", "");
                    jsonMapData.put("mapDataRequest", jsonInnerMapData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                BackendRequest t = new BackendRequest("path", jsonMapData.toString(), false);

                t.execute();
            }
        } else {
            // move existing markers position to received location
            mMarkerError.setCenter(latLng);
            mMarkerError.setRadius(accuracy);
            mMarker.setPosition(latLng);


            if (mPoint2 != null && mPoint2.isVisible()) {


                try {
                    jsonInnerMapData.put("startLongitude", String.valueOf(mMarker.getPosition().longitude));
                    jsonInnerMapData.put("startLatitude", String.valueOf(mMarker.getPosition().latitude));
                    jsonInnerMapData.put("endLongitude", String.valueOf(mPoint2.getPosition().longitude));
                    jsonInnerMapData.put("endLatitude", String.valueOf(mPoint2.getPosition().latitude));

                    jsonMapData.put("requestMessage", "");
                    jsonMapData.put("mapDataRequest", jsonInnerMapData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                BackendRequest t = new BackendRequest("path", jsonMapData.toString(), false);

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
        map.setOnGroundOverlayClickListener(mGroundOverlayClickListener);
        mMap = map;

        try {
            jsonFloorPlan.put("requestMessage", "initialCalling");
            jsonFloorPlan.put("floorPlan", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BackendRequest initial = new BackendRequest("floorPlan", jsonFloorPlan.toString(), true);

        initial.execute();
    }

    public static void mapInitialisation(String response) {
        Log.d("quickDebug", response);
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("floorPlanResponse")) {
                JSONObject jsonObject2 = jsonObject.getJSONObject("floorPlanResponse");
                if (jsonObject2.has("floorPlan"))  {

                    JSONArray floorPlans = jsonObject2.getJSONArray("floorPlan");
                    Log.d("wtf", floorPlans.toString());

                    for (int i = 0; i < floorPlans.length(); i++) {
                        JSONObject buildingGround = floorPlans.getJSONObject(i);

                        if (buildingGround.has("floorPlanID") && buildingGround.has("buildingName")) {
                            IARegion r = IARegion.floorPlan(buildingGround.getString("floorPlanID"));

                            GroundOverlay buildingOverlay = null;

                            mActivity.getTracker().test(r, buildingOverlay);

                            buildingOverlays.put(buildingOverlay, buildingGround.getString("building"));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void setFloorPlans(String response) {
        Log.d("quickDebug", response);
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("floorPlanResponse")) {
                JSONObject jsonObject2 = jsonObject.getJSONObject("floorPlanResponse");
                if (jsonObject2.has("floorPlan"))  {

                    JSONArray floorPlans = jsonObject2.getJSONArray("floorPlan");
                    Log.d("wtf", floorPlans.toString());

                    for (int i = 0; i < floorPlans.length(); i++) {
                        JSONObject buildingStore = floorPlans.getJSONObject(i);
                        if (buildingStore.has("floorPlanID") && buildingStore.has("floor")) {
                            String floorPlanID = buildingStore.getString("floorPlanID");
                            int floor = buildingStore.getInt("floor");
                            if (floor == 0)
                                groundReference = i;
                            focusedBuildingFloorPlans.add(floorPlanID);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void changeFloorPlans(String floorPlanID) {
        IARegion r = IARegion.floorPlan(floorPlanID);
        mActivity.getTracker().test(r, focusedGroundOverlay);
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    //credit: https://stackoverflow.com/questions/12665022/java-rotate-a-point-around-an-other-using-google-maps-coordinates
    private LatLng rotateCoordinate(LatLng centre, LatLng point, double degree) {
        double cLat = centre.latitude;
        double cLng = centre.longitude;
        double bearing = Math.toRadians(degree);
        double newLat = cLat + (Math.sin(bearing) * (point.longitude - cLng) * Math.abs(Math.cos(Math.toRadians(cLat))) + Math.cos(bearing) * (point.latitude - cLat));
        double newLng = cLng + (Math.cos(bearing) * (point.longitude - cLng) - Math.sin(bearing) * (point.latitude - cLat) / Math.abs(Math.cos(Math.toRadians(cLat))));
        return new LatLng(newLat, newLng);
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

                if (focusedGroundOverlay != null) {
                    LatLng rLatLng = rotateCoordinate(focusedGroundOverlay.getPosition(), latLng, focusedGroundOverlay.getBearing());
                    if (!focusedGroundOverlay.getBounds().contains(rLatLng)) {
                        focusedGroundOverlay.setTransparency(0.5f);
                        focusedGroundOverlay.setClickable(true);
                        if (focusedFloor != 0) changeFloorPlans(focusedBuildingFloorPlans.get(groundReference));
                        buildingOverlays.put(focusedGroundOverlay, focusedBuilding);
                        focusedGroundOverlay = null;
                        if (mPoint2 != null) mPoint2.setAlpha(0.5f);
                    }
                    else if (mPoint2 == null) {
                        mPoint2 = mMap.addMarker(new MarkerOptions().position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(HUE_IAGRN)));

                        if (mPoint2 != null && mPoint2.isVisible()) {
    /*
                            try {
                                jsonInner.put("startLongitude", String.valueOf(mPoint.getPosition().longitude));
                                jsonInner.put("startLatitude", String.valueOf(mPoint.getPosition().latitude));
                                jsonInner.put("endLongitude", String.valueOf(mPoint2.getPosition().longitude));
                                jsonInner.put("endLatitude", String.valueOf(mPoint2.getPosition().latitude));

                                json.put("requestMessage", "");
                                json.put("mapDataRequest", jsonInner);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("quickDebug", json.toString());
                            BackendRequest t = new BackendRequest("path", json.toString());

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
                                while (pol.hasNext()) pol.next().remove();
                                ;
                            }
                        } else {
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

                                    json.put("requestMessage", "");
                                    json.put("mapDataRequest", jsonInner);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("quickDebug", json.toString());
                                BackendRequest t = new BackendRequest("path", json.toString());

                                t.execute();
                                */
                            }
                        }
                    }
                }
            }
        }
    };

    public static void drawPolyline(String response) {
        Log.d("quickDebug", response);
        pathResponse = response;
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("mapDataResponse")) {
                JSONObject jsonObject2 = jsonObject.getJSONObject("mapDataResponse");
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
                        JSONObject sourceJSON = path.getJSONObject(i);
                        JSONObject targetJSON = path.getJSONObject(i + 1);
                        if (sourceJSON.has("floor") && targetJSON.has("floor")) {
                            String sourceFloor = sourceJSON.getString("floor");
                            String targetFloor = targetJSON.getString("floor");
                            if (sourceFloor.equals(targetFloor)) {
                                if (sourceJSON.has("latitude") && sourceJSON.has("longitude") && targetJSON.has("latitude") && targetJSON.has("longitude")) {
                                    Log.d("help", (String) sourceJSON.get("latitude"));
                                    Log.d("help2", (String) sourceJSON.get("longitude"));
                                    Log.d("help3", String.valueOf(Double.valueOf((String) targetJSON.get("latitude"))));
                                    LatLng source = new LatLng(Double.valueOf(sourceJSON.getString("latitude")), Double.valueOf(sourceJSON.getString("longitude")));
                                    LatLng target = new LatLng(Double.valueOf(targetJSON.getString("latitude")), Double.valueOf(targetJSON.getString("longitude")));
                                    final PolylineOptions rectOptions = new PolylineOptions();
                                    rectOptions.add(source, target).color(Color.RED);
                                    polyline.add(mMap.addPolyline(rectOptions));
                                    Polyline u = (Polyline) polyline.toArray()[0];
                                    Log.d("wtf2", String.valueOf(source.latitude));
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private GoogleMap.OnGroundOverlayClickListener mGroundOverlayClickListener = new GoogleMap.OnGroundOverlayClickListener() {
        @Override
        public void onGroundOverlayClick(GroundOverlay groundOverlay) {
            //Moving focus from one building directly to another
            if (focusedGroundOverlay != null && !groundOverlay.equals(focusedGroundOverlay)) {
                focusedGroundOverlay.setTransparency(0.5f);
                focusedGroundOverlay.setClickable(true);
                if (focusedFloor != 0) changeFloorPlans(focusedBuildingFloorPlans.get(groundReference));
                buildingOverlays.put(focusedGroundOverlay, focusedBuilding);
            }
            focusedGroundOverlay = groundOverlay;
            focusedGroundOverlay.setTransparency(0.0f);
            focusedGroundOverlay.setClickable(false);
            //Need to make request to get floorplan information
            if (mPoint2 != null) mPoint2.setAlpha(1);
            focusedBuilding = buildingOverlays.get(focusedGroundOverlay);
            focusedFloor = 0;
            buildingOverlays.remove(focusedGroundOverlay);

            try {

                jsonInnerFloorPlan.put("buildingName", focusedBuilding);
                //jsonInnerFloorPlan.put("floor", "2");

                jsonFloorPlan.put("requestMessage", "");
                jsonFloorPlan.put("floorPlan", jsonInnerFloorPlan);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            BackendRequest initial = new BackendRequest("floorPlan", jsonFloorPlan.toString(), false);

            initial.execute();

        }
    };

}
