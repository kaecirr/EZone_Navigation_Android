package com.example.kaelansinclair.ezone_navigation_android;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

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

    private static Marker mMarker;
    private static IARegion mMarkerRegion;
    private static int mMarkerFloor;
    private CircleOptions mMarkerErrorOptions;
    private Circle mMarkerError;
    private Marker mPoint;
    private Marker mPoint2;
    private int mPoint2Floor;
    private String mPoint2Building;

    private Room markedRoom;

    private static GroundOverlay focusedGroundOverlay = null;
    private String focusedBuilding;
    private static IARegion focusedRegion = null;
    private static int focusedFloor;
    private static boolean isFocused;

    private static HashMap<GroundOverlay, String> buildingOverlays;

    private static HashMap<Integer, String> focusedBuildingFloorPlans;

    private static HashMap<Integer, ArrayList<Room>> roomMap;
    private static HashMap<Marker, Room> displayedRooms;

    public static ArrayList<Room> getSearchRooms() {
        return searchRooms;
    }

    private static ArrayList<Room> searchRooms;

    private JSONObject jsonInnerPathData;
    private JSONObject jsonInnerFloorPlan;
    private JSONObject jsonPathData;
    private JSONObject jsonFloorPlan;
    private JSONObject jsonRooms;
    private JSONObject jsonInnerRooms;


    public Map(MainActivity mActivity) {
        this.mActivity = mActivity;

        jsonInnerPathData = new JSONObject();
        try {
            jsonInnerPathData.put("startBuildingName", "computerScience");
            jsonInnerPathData.put("startFloor", "second");
            jsonInnerPathData.put("startLongitude", "-31.97444473");
            jsonInnerPathData.put("startLatitude", "115.8599");
            jsonInnerPathData.put("endBuildingName", "computerScience");
            jsonInnerPathData.put("endFloor", "second");
            jsonInnerPathData.put("endLongitude", "-31.97222274");
            jsonInnerPathData.put("endLatitude", "115.823");
            jsonInnerPathData.put("algorithm", "DJ");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonPathData = new JSONObject();
        try {
            jsonPathData.put("requestMessage", "");
            jsonPathData.put("pathData", jsonInnerPathData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonInnerFloorPlan = new JSONObject();
        try {
            jsonInnerFloorPlan.put("buildingName", "ComputerScience");
            //jsonInnerFloorPlan.put("floor", "2");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonFloorPlan = new JSONObject();
        try {
            jsonFloorPlan.put("requestMessage", "");
            jsonFloorPlan.put("floorData", jsonInnerFloorPlan);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonInnerRooms = new JSONObject();
        try {
            jsonInnerRooms.put("buildingName", "ComputerScience");
            //jsonInnerFloorPlan.put("floor", "2");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonRooms = new JSONObject();
        try {
            jsonRooms.put("requestMessage", "");
            jsonRooms.put("roomData", jsonInnerRooms);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pathResponse = "";
        polyline = new HashSet<Polyline>();
        buildingOverlays = new HashMap<GroundOverlay, String>();
        focusedBuildingFloorPlans = new HashMap<Integer, String>();
        roomMap = new HashMap<Integer, ArrayList<Room>>();
        displayedRooms = new HashMap<Marker, Room>();
        focusedBuilding = "";
        focusedFloor = 0;
        isFocused = false;
        mPoint2Floor = 0;
        mPoint2Building = "";

        searchRooms = new ArrayList<Room>();
    }

    public static GoogleMap getMap() {return mMap;}

    public GroundOverlay getFocusedGroundOverlay() {return focusedGroundOverlay;}

    public boolean getIsFocused() {return isFocused;}

    public void setFocusedGroundOverlay(GroundOverlay newGroundOverlay) {focusedGroundOverlay = newGroundOverlay;}

    public void setCameraPositionNeedsUpdating(Boolean needsUpdating) {mCameraPositionNeedsUpdating = needsUpdating;}

    public void updateCameraPosition() {
        if (mMap != null && mMarker != null && mCameraPositionNeedsUpdating) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), 25.0f));
            mCameraPositionNeedsUpdating = false;
        }
    }

    public void updateLocation(LatLng latLng, double accuracy, IARegion region, int floorLevel) {
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
            mMarkerRegion = region;
            mMarkerFloor = floorLevel;

            mMarkerErrorOptions = new CircleOptions().center(latLng).radius(accuracy).strokeColor(Color.argb(255, 8, 0, 255)).strokeWidth(5).fillColor(Color.argb(128, 0, 170, 255)).zIndex(99);
            mMarkerError = mMap.addCircle(mMarkerErrorOptions);

            if (mPoint2 != null && mPoint2.isVisible()) {

//                try {
//                    jsonInnerPathData.put("startLongitude", String.valueOf(mMarker.getPosition().longitude));
//                    jsonInnerPathData.put("startLatitude", String.valueOf(mMarker.getPosition().latitude));
//                    jsonInnerPathData.put("endLongitude", String.valueOf(mPoint2.getPosition().longitude));
//                    jsonInnerPathData.put("endLatitude", String.valueOf(mPoint2.getPosition().latitude));
//
//                    jsonPathData.put("requestMessage", "");
//                //    jsonPathData.put("mapDataRequest", jsonInnerPathData);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                BackendRequest t = new BackendRequest("path", jsonPathData.toString(), false);
//
//               // t.execute();
            }
        } else {
            // move existing markers position to received location
            mMarkerError.setCenter(latLng);
            mMarkerError.setRadius(accuracy);
            mMarker.setPosition(latLng);
            mMarkerRegion = region;
            mMarkerFloor = floorLevel;

            if (mPoint2 != null && mPoint2.isVisible()) {


//                try {
//                //    jsonInnerPathData.put("startLongitude", String.valueOf(mMarker.getPosition().longitude));
//                    jsonInnerPathData.put("startLatitude", String.valueOf(mMarker.getPosition().latitude));
//                    jsonInnerPathData.put("endLongitude", String.valueOf(mPoint2.getPosition().longitude));
//                    jsonInnerPathData.put("endLatitude", String.valueOf(mPoint2.getPosition().latitude));
//
//                    jsonPathData.put("requestMessage", "");
//                //    jsonPathData.put("mapDataRequest", jsonInnerPathData);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//                BackendRequest t = new BackendRequest("path", jsonPathData.toString(), false);

              //  t.execute();
            }
        }

        if (focusedRegion != null) {
            if ((!focusedRegion.equals(mMarkerRegion) && isFocused) || !isFocused)
                mMarker.setAlpha(0.5f);
            else mMarker.setAlpha(1);
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
        map.setOnMarkerClickListener(mMarkerClickListener);
        mMap = map;

        try {
            jsonFloorPlan.put("requestMessage", "initialCalling");
            jsonFloorPlan.put("floorData", new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("test", jsonFloorPlan.toString());
        BackendRequest initial = new BackendRequest("floorPlan", jsonFloorPlan.toString(), true);

        initial.execute();

        // Temporary code to get comp sci rooms so that on device search can be done.
        try {
            jsonInnerRooms.put("buildingName", "ComputerScience");

            jsonRooms.put("requestMessage", "");
            JSONArray roomsArray = new JSONArray();
            roomsArray.put(jsonInnerRooms);
            jsonRooms.put("roomData", roomsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        BackendRequest retrieveRooms = new BackendRequest("rooms", jsonRooms.toString(), true);

        retrieveRooms.execute();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-31.977646, 115.816227), 20.0f));
    }

    public static void mapInitialisation(String response) {
        Log.d("quickDebug", response);
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("floorPlanData")) {
                JSONArray floorPlans = jsonObject.getJSONArray("floorPlanData");
                Log.d("wtf", floorPlans.toString());

                for (int i = 0; i < floorPlans.length(); i++) {
                    JSONObject buildingGround = floorPlans.getJSONObject(i);

                    if (buildingGround.has("floorPlanID") && buildingGround.has("buildingName")) {
                        IARegion r = IARegion.floorPlan(buildingGround.getString("floorPlanID"));
                        if (!buildingGround.getString("buildingName").equals("ECM")) {
                            GroundOverlay buildingOverlay = null;
                            mActivity.getTracker().test(r, buildingOverlay, true, buildingGround.getString("buildingName"));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void setRoomsInit(String response) {
        Log.d("quickDebug", response);
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("roomInfoData")) {
                JSONArray roomsArray = jsonObject.getJSONArray("roomInfoData");

                for (int i = 0; i < roomsArray.length(); i++) {
                    JSONObject roomStore = roomsArray.getJSONObject(i);
                    if (roomStore.has("roomName") && roomStore.has("floor") && roomStore.has("roomDescription") && roomStore.has("latitude") && roomStore.has("longitude")) {
                        Room newRoom = new Room(roomStore.getString("roomName"), roomStore.getInt("floor"), roomStore.getString("roomDescription"), new LatLng(roomStore.getDouble("latitude"), roomStore.getDouble("longitude")));
                        searchRooms.add(newRoom);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public HashMap<GroundOverlay, String> getBuildingOverlays() {return buildingOverlays;}

    public static void setFloorPlans(String response) {
        Log.d("quickDebug", response);
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("floorPlanData")) {
                JSONArray floorPlans = jsonObject.getJSONArray("floorPlanData");
                Log.d("wtf", floorPlans.toString());
                for (int i = 0; i < floorPlans.length(); i++) {
                    JSONObject buildingStore = floorPlans.getJSONObject(i);
                    if (buildingStore.has("floorPlanID") && buildingStore.has("floor")) {
                        String floorPlanID = buildingStore.getString("floorPlanID");
                        int floor = buildingStore.getInt("floor");
                        if (floor == 0) {
                            focusedRegion = IARegion.floorPlan(floorPlanID);

                            if (mMarkerRegion != null) {
                                if (!focusedRegion.equals(mMarkerRegion))
                                    mMarker.setAlpha(0.5f);
                                else mMarker.setAlpha(1);
                            }
                        }
                        focusedBuildingFloorPlans.put(floor, floorPlanID);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void changeFloor(int change) {
        if (focusedFloor + change >= 0 && focusedFloor + change < focusedBuildingFloorPlans.size()) {
            focusedFloor += change;
            focusedRegion = IARegion.floorPlan(focusedBuildingFloorPlans.get(focusedFloor));

            if (mMarkerRegion != null) {
                if (!focusedRegion.equals(mMarkerRegion))
                    mMarker.setAlpha(0.5f);
                else mMarker.setAlpha(1);
            }

            changeFloorPlans(focusedBuildingFloorPlans.get(focusedFloor));
            if (mPoint2 != null) {
                if (mPoint2.isVisible() && focusedFloor == mPoint2Floor && focusedBuilding.equals(mPoint2Building)) mPoint2.setAlpha(1);
                else mPoint2.setAlpha(0.5f);
            }

            displayRooms();
        }
    }

    private void changeFloorPlans(String floorPlanID) {
        focusedRegion = IARegion.floorPlan(floorPlanID);
        mActivity.getTracker().test(focusedRegion, focusedGroundOverlay, false, "");
        if (mPoint2 != null) {
            if (mPoint2.isVisible()) drawPolyline(pathResponse);
        }
    }

    public static void setRooms(String response) {
        Log.d("quickDebug", response);
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("roomInfoData")) {
                JSONArray roomsArray = jsonObject.getJSONArray("roomInfoData");

                for (int i = 0; i < roomsArray.length(); i++) {
                    JSONObject roomStore = roomsArray.getJSONObject(i);
                    if (roomStore.has("roomName") && roomStore.has("floor") && roomStore.has("roomDescription") && roomStore.has("latitude") && roomStore.has("longitude")) {
                        Room newRoom = new Room(roomStore.getString("roomName"), roomStore.getInt("floor"), roomStore.getString("roomDescription"), new LatLng(roomStore.getDouble("latitude"), roomStore.getDouble("longitude")));
                        if (!roomMap.containsKey(newRoom.getFloor())) roomMap.put(newRoom.getFloor(), new ArrayList<Room>());
                        roomMap.get(newRoom.getFloor()).add(newRoom);
                    }
                }
                displayRooms();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void displayRooms() {
        Iterator<Marker> keysIterator = displayedRooms.keySet().iterator();
        while (keysIterator.hasNext()) keysIterator.next().remove();
        displayedRooms.clear();
        for (int i = 0; i < roomMap.get(focusedFloor).size(); i++) {
            Room room = roomMap.get(focusedFloor).get(i);
            BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromResource(android.R.drawable.presence_invisible);
            displayedRooms.put(mMap.addMarker(new MarkerOptions().position(room.getLatLng()).icon(markerIcon).zIndex(100).anchor(0.5f,0.5f)), room);
        }
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
                        Iterator<Marker> keysIterator = displayedRooms.keySet().iterator();
                        while (keysIterator.hasNext()) keysIterator.next().remove();
                        focusedGroundOverlay.setTransparency(0.5f);
                        focusedGroundOverlay.setClickable(true);
                        if (focusedFloor != 0) changeFloorPlans(focusedBuildingFloorPlans.get(0));
                        buildingOverlays.put(focusedGroundOverlay, focusedBuilding);
                        focusedGroundOverlay = null;
                        focusedBuildingFloorPlans.clear();
                        mActivity.getFabUp().hide();
                        mActivity.getFabDown().hide();
                        isFocused = false;
                        if (mPoint2 != null) {
                            if (mPoint2.isVisible()) mPoint2.setAlpha(0.5f);
                            if (mMarker != null) mMarker.setAlpha(0.5f);
                        }
                        mActivity.bottomMenuMarkerClose(false);
                    }
                    else if (mPoint2 == null) {
                        mPoint2 = mMap.addMarker(new MarkerOptions().position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(HUE_IAGRN)).zIndex(101));
                        mPoint2Floor = focusedFloor;
                        mPoint2Building = focusedBuilding;

                        mActivity.bottomMenuMarkerOpen("Latitude: " + latLng.latitude, "Longitude: " + latLng.longitude, "Floor: " + mPoint2Floor, "Building: " + focusedBuilding, false);

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
                            }
                            pathResponse = "";
                            markedRoom = null;
                            mActivity.bottomMenuMarkerClose(false);
                        } else {
                            // move existing markers position to received location
                            mPoint2.setPosition(latLng);
                            mPoint2.setVisible(true);
                            mPoint2.setAlpha(1);
                            mPoint2Floor = focusedFloor;
                            mPoint2Building = focusedBuilding;
                            //mMap.setMyLocationEnabled(false);

                            mActivity.bottomMenuMarkerOpen("Latitude: " + latLng.latitude, "Longitude: " + latLng.longitude, "Floor: " + mPoint2Floor, "Building: " + focusedBuilding, false);

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

    private GoogleMap.OnMarkerClickListener mMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {

            if (!marker.equals(mPoint2) && !marker.equals(mMarker)) {
                if (mPoint2 == null) {
                    mPoint2 = mMap.addMarker(new MarkerOptions().position(marker.getPosition())
                            .icon(BitmapDescriptorFactory.defaultMarker(HUE_IAGRN)).zIndex(101));
                }
                else {
                    mPoint2.setPosition(marker.getPosition());
                    if (!mPoint2.isVisible()) mPoint2.setVisible(true);
                    mPoint2.setAlpha(1);
                }
                mPoint2Floor = focusedFloor;
                mPoint2Building = focusedBuilding;

                if (polyline != null) {
                    Iterator<Polyline> pol = polyline.iterator();
                    while (pol.hasNext()) pol.next().remove();
                }
                pathResponse = "";
                markedRoom = displayedRooms.get(marker);
                mActivity.bottomMenuMarkerOpen("Name: " + displayedRooms.get(marker).getName(), "Floor: " + displayedRooms.get(marker).getFloor(), "Building: " + focusedBuilding, "", true);

            }
            return true;
        }
    };

    public void getPath() {
        jsonInnerPathData = new JSONObject();
        try {
            jsonInnerPathData.put("startBuildingName", "ComputerScience");
            jsonInnerPathData.put("startFloor", Integer.toString(focusedFloor));
            jsonInnerPathData.put("startLongitude", Double.toString(mPoint2.getPosition().longitude));
            jsonInnerPathData.put("startLatitude", Double.toString(mPoint2.getPosition().latitude));
            jsonInnerPathData.put("endBuildingName", mPoint2Building);
            jsonInnerPathData.put("endFloor", Integer.toString(mPoint2Floor));
            jsonInnerPathData.put("endLongitude", Double.toString(mPoint2.getPosition().longitude));
            jsonInnerPathData.put("endLatitude", Double.toString(mPoint2.getPosition().latitude));
            jsonInnerPathData.put("algorithm", "DJ");

            jsonPathData.put("requestMessage", "");
            jsonPathData.put("pathData", jsonInnerPathData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("thing", jsonPathData.toString());
        BackendRequest t = new BackendRequest("path", jsonPathData.toString(), false);

        t.execute();
    }

    public static void drawPolyline(String response) {
        Log.d("quickDebug", response);
        pathResponse = response;
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("mapData")) {
                JSONObject jsonObject2 = jsonObject.getJSONObject("mapData");
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
                            if (sourceFloor.equals(targetFloor) && Integer.parseInt(sourceFloor) == focusedFloor) {
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
                Iterator<Marker> keysIterator = displayedRooms.keySet().iterator();
                while (keysIterator.hasNext()) keysIterator.next().remove();
                focusedGroundOverlay.setTransparency(0.5f);
                focusedGroundOverlay.setClickable(true);
                if (focusedFloor != 0) changeFloorPlans(focusedBuildingFloorPlans.get(0));
                buildingOverlays.put(focusedGroundOverlay, focusedBuilding);
            }

            focusedGroundOverlay = groundOverlay;
            focusedGroundOverlay.setTransparency(0.0f);
            focusedGroundOverlay.setClickable(false);
            //Need to make request to get floorplan information
            focusedBuilding = buildingOverlays.get(focusedGroundOverlay);
            focusedFloor = 0;
            Log.d("building", focusedBuilding);
            Log.d("building2", mPoint2Building);
            if (mPoint2 != null) {
                if (mPoint2.isVisible() && focusedBuilding.equals(mPoint2Building)) {
                    if (focusedFloor == mPoint2Floor) mPoint2.setAlpha(1);

                    if (markedRoom == null) mActivity.bottomMenuMarkerOpen("Latitude: " + mPoint2.getPosition().latitude, "Longitude: " + mPoint2.getPosition().longitude, "Floor: " + mPoint2Floor, "Building: " + focusedBuilding, false);
                    else mActivity.bottomMenuMarkerOpen("Name: " + markedRoom.getName(), "Floor: " + markedRoom.getFloor(), "Building: " + focusedBuilding, "", true);

                }
            }
            buildingOverlays.remove(focusedGroundOverlay);

            try {

                jsonInnerFloorPlan.put("buildingName", focusedBuilding);

                jsonFloorPlan.put("requestMessage", "");
                JSONArray floorArray = new JSONArray();
                floorArray.put(jsonInnerFloorPlan);
                jsonFloorPlan.put("floorData", floorArray);

                jsonInnerRooms.put("buildingName", focusedBuilding);

                jsonRooms.put("requestMessage", "");
                JSONArray roomsArray = new JSONArray();
                roomsArray.put(jsonInnerRooms);
                jsonRooms.put("roomData", roomsArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            BackendRequest retrieveFloorPlans = new BackendRequest("floorPlan", jsonFloorPlan.toString(), false);

            BackendRequest retrieveRooms = new BackendRequest("rooms", jsonRooms.toString(), false);

            retrieveFloorPlans.execute();

            retrieveRooms.execute();

          //  focusedBuildingFloorPlans.add("0dc8358c-9e1e-4afa-8adb-3bdfb7154a88");
          //  focusedBuildingFloorPlans.add("6ee5ef62-e5e9-499e-9f8a-3f2c9c6e2d91");
           // focusedBuildingFloorPlans.add("208ae45e-8d22-4faa-bfb2-e245f956de3b");

            mActivity.getFabUp().show();
            mActivity.getFabDown().show();

            isFocused = true;

        }
    };

}
