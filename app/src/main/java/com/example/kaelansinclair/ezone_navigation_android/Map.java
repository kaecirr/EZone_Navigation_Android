package com.example.kaelansinclair.ezone_navigation_android;

import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
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
 * This class controls the Google Maps functionality and the associated UI/UX which are based off
 * of the Google Maps API. This includes the selection of floor plans, the drawing of Polylines for
 * paths and the placement and drawing of the map markers.
 */

public class Map implements OnMapReadyCallback {

    private static GoogleMap mMap;

    // Stores the path related information
    private static String pathResponse;
    private static Set<Polyline> polyline;

    private static MainActivity mActivity;
    private boolean mCameraPositionNeedsUpdating = true; // update on first location

    // Variables for the user marker
    private static Marker mMarker;
    private static IARegion mMarkerRegion;
    private static int mMarkerFloor;
    private CircleOptions mMarkerErrorOptions;
    private Circle mMarkerError;

    // Variables for the destination marker
    private static Marker mPoint2;
    private static int mPoint2Floor;
    private static String mPoint2Building;
    private static final float HUE_IAGRN = 133.0f;

    // Variables for the GroundOverlays and the focussing of floor plans and buildings
    private static GroundOverlay focusedGroundOverlay = null;
    private static String focusedBuilding;
    private static IARegion focusedRegion = null;
    private static int focusedFloor;
    private static boolean isFocused;
    private static HashMap<GroundOverlay, String> buildingOverlays;
    private static HashMap<Integer, String> focusedBuildingFloorPlans;

    // Based on if the floor plan needs to change to focus on the marker after selecting a search
    // result
    private static boolean focusOnMarker = false;

    // Variables that hold the room information for a focused building
    private static HashMap<Integer, ArrayList<Room>> roomMap;
    private static HashMap<Marker, Room> displayedRooms;
    private Room markedRoom;

    // Holds the list of rooms used for the on device search - Again this should be moved to an off
    // device search in the future
    private static ArrayList<Room> searchRooms;

    // Variables for the colour switching of the floor plan select buttons, which are either blue
    // or grey depending on whether the bottom or top floor has been reached
    private static int[][] states = {
            {android.R.attr.state_enabled},
            {android.R.attr.state_pressed}
    };
    private static int[] colorGrey = {Color.GRAY, Color.GRAY};
    private static int[] colorBlue = {Color.parseColor("#546bec"), Color.parseColor("#546bec")};

    // JSON objects to act as references for the requests to the server
    private JSONObject jsonInnerPathData;
    private JSONObject jsonInnerFloorPlan;
    private JSONObject jsonPathData;
    private JSONObject jsonFloorPlan;
    private JSONObject jsonRooms;
    private JSONObject jsonInnerRooms;

    /**
     * Constructor for the map class
     * @param mActivity the main activity
     */
    public Map(MainActivity mActivity) {
        this.mActivity = mActivity;

        // The following lines are setting up the default JSON object requests
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

        // Initialising the variables to be used in the class
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

    public int getFocusedFloor() {return focusedFloor;}

    public static String getFocusedBuilding() {return focusedBuilding;}

    public static GoogleMap getMap() {return mMap;}

    public GroundOverlay getFocusedGroundOverlay() {return focusedGroundOverlay;}

    public HashMap<Integer, String> getFocusedBuildingFloorPlans() {return focusedBuildingFloorPlans;}

    public boolean getIsFocused() {return isFocused;}

    public void setFocusedGroundOverlay(GroundOverlay newGroundOverlay) {focusedGroundOverlay = newGroundOverlay;}

    public void setFocusedFloor(int newFloor) {focusedFloor = newFloor;}

    public void setCameraPositionNeedsUpdating(Boolean needsUpdating) {mCameraPositionNeedsUpdating = needsUpdating;}

    public void updateCameraPosition() {
        if (mMap != null && mMarker != null && mCameraPositionNeedsUpdating) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), 25.0f));
            mCameraPositionNeedsUpdating = false;
        }
    }

    /**
     * Controls the updating of the marker representing the user on the map.
     * @param latLng the new position of the user.
     * @param accuracy the radial accuracy of the new position of the user in metres.
     * @param region the IndoorAtlas region (equivalent to a building level) the user is on.
     * @param floorLevel the number indicating the floor level the user is on
     */
    public void updateLocation(LatLng latLng, double accuracy, IARegion region, int floorLevel) {
        if (mMap == null) {
            // location received before map is initialized, ignoring update here
            return;
        }
        if (mMarker == null) {
            // first location, add marker
            Drawable circleDrawable = mActivity.getResources().getDrawable(R.drawable.marker_circle);
            BitmapDescriptor markerIcon = getMarkerIconFromDrawable(circleDrawable);
            mMarker = mMap.addMarker(new MarkerOptions().position(latLng).icon(markerIcon).zIndex(100).anchor(0.5f,0.5f));
            mMarkerRegion = region;
            mMarkerFloor = floorLevel;

            mMarkerErrorOptions = new CircleOptions().center(latLng).radius(accuracy).strokeColor(Color.argb(255, 8, 0, 255)).strokeWidth(5).fillColor(Color.argb(128, 0, 170, 255)).zIndex(99);
            mMarkerError = mMap.addCircle(mMarkerErrorOptions);

            // If there is a destination maker already present, draw a path between the user
            // maker and the destination marker
            if (mPoint2 != null && mPoint2.isVisible()) getPath();
        } else {
            // move existing markers position to received location
            mMarkerError.setCenter(latLng);
            mMarkerError.setRadius(accuracy);
            mMarker.setPosition(latLng);
            mMarkerRegion = region;
            mMarkerFloor = floorLevel;

            // Only draw redraw the path between the user marker and the destination marker if in
            // navigation mode
            if (mPoint2 != null && mPoint2.isVisible()) {

                if (mActivity.getNaviationMode()) getPath();
            }
        }

        // Change the opacity of the user marker depedning on if the user is focused on the region
        // the user marker is in
        if (focusedRegion != null) {
            if ((!focusedRegion.equals(mMarkerRegion) && isFocused) || !isFocused)
                mMarker.setAlpha(0.5f);
            else mMarker.setAlpha(1);
        }
        else {
            mMarker.setAlpha(0.5f);
        }

        // our camera position needs updating if location has significantly changed
        if (mCameraPositionNeedsUpdating) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 25.0f));
            mCameraPositionNeedsUpdating = false;
        }
    }

    public static ArrayList<Room> getSearchRooms() {
        return searchRooms;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        // Ensuring permissions are granted
        if (ContextCompat.checkSelfPermission(mActivity.getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            mActivity.ensurePermissions();
        }
        else {
            map.setMyLocationEnabled(false);
        }

        // Initialising the listeners for the Google Map
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setOnMapClickListener(mClickListener);
        map.setOnGroundOverlayClickListener(mGroundOverlayClickListener);
        map.setOnMarkerClickListener(mMarkerClickListener);
        mMap = map;

        // Initialising the buildings supported by the application
        try {
            jsonFloorPlan.put("requestMessage", "initialCalling");
            jsonFloorPlan.put("floorData", new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        BackendRequest initial = new BackendRequest("floorPlan", jsonFloorPlan.toString(), true);

        initial.execute();

        // Temporary code to get Computer Science buidling rooms so that on device search can be
        // done
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

        // Zoom over the Computer Science building
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-31.977646, 115.816227), 20.0f));
    }

    /**
     * Creates the GroundOverlays for all of the buildings supported by the application, based on
     * those gathered from the server. This initialises the map giving all the buildings the user
     * can focus on.
     * @param response the response from the server containing the supported building information.
     */
    public static void mapInitialisation(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("floorPlanData")) {
                JSONArray floorPlans = jsonObject.getJSONArray("floorPlanData");

                for (int i = 0; i < floorPlans.length(); i++) {
                    JSONObject buildingGround = floorPlans.getJSONObject(i);

                    if (buildingGround.has("floorPlanID") && buildingGround.has("buildingName")) {
                        IARegion r = IARegion.floorPlan(buildingGround.getString("floorPlanID"));
                        if (!buildingGround.getString("buildingName").equals("ECM")) {
                            GroundOverlay buildingOverlay = null;
                            mActivity.getTracker().setFetchFloorPlan(r, buildingOverlay, true, buildingGround.getString("buildingName"));
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * A function which gathers the rooms for the on device search implementation. This is temporary
     * and should be implemented on the backend server.
     * @param response the response from the server containing the room information.
     */
    public static void setRoomsInit(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("roomInfoData")) {
                JSONArray roomsArray = jsonObject.getJSONArray("roomInfoData");
                ArrayList<Room> searchRoomsHold = new ArrayList<Room>();
                for (int i = 0; i < roomsArray.length(); i++) {
                    JSONObject roomStore = roomsArray.getJSONObject(i);
                    if (roomStore.has("roomName") && roomStore.has("floor") && roomStore.has("roomDescription") && roomStore.has("latitude") && roomStore.has("longitude")) {
                        Room newRoom = new Room(roomStore.getString("roomName"), roomStore.getInt("floor"), roomStore.getString("roomDescription"), new LatLng(roomStore.getDouble("latitude"), roomStore.getDouble("longitude")));
                        searchRoomsHold.add(newRoom);
                    }
                }

                searchRooms = searchRoomsHold;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public HashMap<GroundOverlay, String> getBuildingOverlays() {return buildingOverlays;}

    /**
     * Gets the IndoorAtlas floor plan IDs to allow the retrieval of the floor plans from the
     * IndoorAtlas servers for the focused building.
     * @param response the floor plan IDs for the focused building from the server.
     */
    public static void setFloorPlans(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("floorPlanData")) {
                JSONArray floorPlans = jsonObject.getJSONArray("floorPlanData");
                for (int i = 0; i < floorPlans.length(); i++) {
                    JSONObject buildingStore = floorPlans.getJSONObject(i);
                    if (buildingStore.has("floorPlanID") && buildingStore.has("floor")) {
                        String floorPlanID = buildingStore.getString("floorPlanID");
                        int floor = buildingStore.getInt("floor");
                        if (floor == 0) {
                            focusedRegion = IARegion.floorPlan(floorPlanID);

                            // Make the user marker translucent if the focused floor isn't the same
                            // floor the user is on
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

        // Change floors if needed to focus on a destination marker that is on a different floor to
        // the focused floor after the user has selected a search item
        if (focusOnMarker) {
            int change;
            if (focusedFloor >= mPoint2Floor) change = mPoint2Floor - focusedFloor;
            else change = mPoint2Floor - focusedFloor;
            if (change != 0) changeFloor(change);
            focusOnMarker = false;
        }
    }

    /**
     * Initiates the process of changing floors by changing the focused regions and starting the
     * calls to have the floor plan changed.
     * @param change the number of floors to change, with a positive number being up and a negative
     *               number meaning down.
     */
    public static void changeFloor(int change) {
        // If within the range of floors the focused building has
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

            changeFABColour();

            // Get the rooms for the new floor level and display them
            displayRooms();
        }
    }

    /**
     * Sets the destination marker translucent or opaque depending on if the destination marker is
     * on the focused floor.
     */
    public static void mPoint2Dim() {
        if (mPoint2 != null) {
            if (mPoint2.isVisible() && focusedFloor == mPoint2Floor && focusedBuilding.equals(mPoint2Building)) mPoint2.setAlpha(1);
            else mPoint2.setAlpha(0.5f);
        }
    }

    /**
     * Changes the colours of the floating action buttons which control selecting the floor plans,
     * with the up button going grey if the top floor is reached, and the bottom button going grey
     * on the ground floor. Otherwise each button is blue.
     */
    public static void changeFABColour() {
        if (focusedFloor == focusedBuildingFloorPlans.size() - 1) {
            ColorStateList colorStateList2 = new ColorStateList(states, colorGrey);
            mActivity.getFabUp().setBackgroundTintList(colorStateList2);
        }
        else {
            ColorStateList colorStateList2 = new ColorStateList(states, colorBlue);
            mActivity.getFabUp().setBackgroundTintList(colorStateList2);
        }

        if (focusedFloor == 0) {
            ColorStateList colorStateList = new ColorStateList(states, colorGrey);
            mActivity.getFabDown().setBackgroundTintList(colorStateList);
        }
        else {
            ColorStateList colorStateList = new ColorStateList(states, colorBlue);
            mActivity.getFabDown().setBackgroundTintList(colorStateList);
        }
    }

    public static void setFocusedRegion(IARegion region) {focusedRegion = region;}

    /**
     * Calling the IndoorAtlas class for changing the floor plan image from the IndoorAtlas servers.
     * @param floorPlanID the IndoorAtlas floor plan ID.
     */
    private static void changeFloorPlans(String floorPlanID) {
        focusedRegion = IARegion.floorPlan(floorPlanID);
        mActivity.getTracker().setFetchFloorPlan(focusedRegion, focusedGroundOverlay, false, "");

        // Changes the displayed path to the destination marker (if it exists) based on which floor
        // is being focused on
        if (mPoint2 != null) {
            if (mPoint2.isVisible()) drawPolyline(pathResponse);
        }
    }

    /**
     * Focuses on the floor plan of a destination marker placed after selecting an item from search
     * results if not focused on a building.
     */
    public void focusOnFloorPlan() {
        if (!isFocused) { // If not focused on a building
            focusOnMarker = true;
            Iterator<GroundOverlay> overlays = buildingOverlays.keySet().iterator();
            GroundOverlay nextOverlay = null;
            while (overlays.hasNext()) { // Get the overlay to focus on
                nextOverlay = overlays.next();
                if (mPoint2Building.equals(buildingOverlays.get(nextOverlay))) break;
            }

            // Clear any UI items if looking at a different floor plan to the new one
            if (focusedGroundOverlay != null && !nextOverlay.equals(focusedGroundOverlay)) {
                removeDisplayedRooms();
                focusedGroundOverlay.setTransparency(0.5f);
                focusedGroundOverlay.setClickable(true);
                if (focusedFloor != 0) changeFloorPlans(focusedBuildingFloorPlans.get(0));
                buildingOverlays.put(focusedGroundOverlay, focusedBuilding);
            }

            //Set the new overlay variables
            focusedGroundOverlay = nextOverlay;
            focusedGroundOverlay.setTransparency(0.0f);
            focusedGroundOverlay.setClickable(false);
            // Need to make request to get floorplan information
            focusedBuilding = buildingOverlays.get(focusedGroundOverlay);
            focusedFloor = 0;
            buildingOverlays.remove(focusedGroundOverlay);

            // Get the floor plan ID and the rooms for the new focused floor
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

            retrieveRooms.execute();

            retrieveFloorPlans.execute();

            // Makes down icon grey since each building starts with the ground floor
            ColorStateList colorStateList = new ColorStateList(states, colorGrey);
            mActivity.getFabDown().setBackgroundTintList(colorStateList);

            ColorStateList colorStateList2 = new ColorStateList(states, colorBlue);
            mActivity.getFabUp().setBackgroundTintList(colorStateList2);

            mActivity.getFabUp().show();
            mActivity.getFabDown().show();

            isFocused = true;

        }
        else { // Otherwise change the floor level
            int change;
            if (focusedFloor >= mPoint2Floor) change = mPoint2Floor - focusedFloor;
            else change = mPoint2Floor - focusedFloor;
            changeFloor(change);
        }
    }

    /**
     * Stores the results of the gathered room data for a particular floor.
     * @param response the room data for a particular floor.
     */
    public static void setRooms(String response) {
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

    /**
     * Display the rooms for the current floor plan and clear and old rooms from previous floor
     * plans that may still be present.
     */
    public static void displayRooms() {
        removeDisplayedRooms();
        displayedRooms.clear();
        for (int i = 0; i < roomMap.get(focusedFloor).size(); i++) {
            Room room = roomMap.get(focusedFloor).get(i);
            BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromResource(android.R.drawable.presence_invisible);
            displayedRooms.put(mMap.addMarker(new MarkerOptions().position(room.getLatLng()).icon(markerIcon).zIndex(100).anchor(0.5f,0.5f)), room);
        }
    }

    /**
     * Remove displayed rooms.
     */
    public static void removeDisplayedRooms() {
        Iterator<Marker> keysIterator = displayedRooms.keySet().iterator();
        while (keysIterator.hasNext()) keysIterator.next().remove();
    }

    /**
     * Gets the marker icon for the user marker.
     * @param drawable the Drawable for the user marker.
     * @return the BitmapDescriptor of the user marker.
     */
    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Rotates the user coordinates to match the bounding box that represents the floor plan for
     * the GroundOverlay. This is because while the image of the floor plan is rotated, the bounding
     * box that defines it is not, meaning that the map click coordinates need to be rotated in
     * reverse the amount the floor plan was rotated to check if the click was within the bounds of
     * the GroundOverlay bounding box. See the Google Maps GroundOverlay documentation for more
     * details.
     * credit: https://stackoverflow.com/questions/12665022/java-rotate-a-point-around-an-other-using-google-maps-coordinates
     * @param centre the centre of the GroundOverlay.
     * @param point the point that was clicked on the map.
     * @param degree the angle that the floor plan was rotated.
     * @return the rotated coordinates to match the floor plan bounding box orientation.
     */
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
            if (mMap != null && !mActivity.getNaviationMode()) { // If the map is ready and not in navigation mode

                if (focusedGroundOverlay != null) { // Focused on a building
                    LatLng rLatLng = rotateCoordinate(focusedGroundOverlay.getPosition(), latLng, focusedGroundOverlay.getBearing());
                    // If clicked outside the bounds of the floor plan, remove focus from the
                    // floor plan
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
                        Iterator<Polyline> pol = polyline.iterator();
                        while (pol.hasNext()) pol.next().remove();
                        isFocused = false;

                        // Make any placed destination marker translucent if the building it is in
                        // does not have focus. This is the similar for the user marker
                        if (mPoint2 != null) {
                            if (mPoint2.isVisible()) mPoint2.setAlpha(0.5f);
                            if (mMarker != null) mMarker.setAlpha(0.5f);
                        }
                        mActivity.bottomMenuMarkerClose(false);
                    }
                    else if (mPoint2 == null) { // If in the bounds of the floor plan, and there is not destination marker, then place a destination marker
                        mPoint2 = mMap.addMarker(new MarkerOptions().position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(HUE_IAGRN)).zIndex(101));
                        mPoint2Floor = focusedFloor;
                        mPoint2Building = focusedBuilding;

                        mActivity.bottomMenuMarkerOpen("Latitude: " + latLng.latitude, "Longitude: " + latLng.longitude, "Floor: " + mPoint2Floor, "Building: " + focusedBuilding, false);

                        // Get a path if the destination marker is placed
                        if (mPoint2 != null && mPoint2.isVisible()) getPath();
                    }
                    else if (mPoint2 != null) {
                        if (mPoint2.isVisible()) { // If in the bounds of the floor plan, and there is a visible destination marker, then make the destination marker invisible, and remove any path
                            mPoint2.setVisible(false);
                            // mPoint.setPosition(latLng);
                            if (polyline != null) {
                                Iterator<Polyline> pol = polyline.iterator();
                                while (pol.hasNext()) pol.next().remove();
                            }
                            pathResponse = "";
                            markedRoom = null;
                            mActivity.bottomMenuMarkerClose(false);
                        } else { // If in the bounds of the floor plan, and there is an invisible destination marker, then make the destination marker visible
                            // move existing markers position to received location
                            mPoint2.setPosition(latLng);
                            mPoint2.setVisible(true);
                            mPoint2.setAlpha(1);
                            mPoint2Floor = focusedFloor;
                            mPoint2Building = focusedBuilding;
                            //mMap.setMyLocationEnabled(false);

                            mActivity.bottomMenuMarkerOpen("Latitude: " + latLng.latitude, "Longitude: " + latLng.longitude, "Floor: " + mPoint2Floor, "Building: " + focusedBuilding, false);

                            // Get a path if the destination marker is placed
                            if (mPoint2 != null && mPoint2.isVisible()) {
                                getPath();
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
            if(!mActivity.getNaviationMode()) { // If not in navigation mode
                if (!marker.equals(mPoint2) && !marker.equals(mMarker)) { // Does not apply to either the destination marker or the user marker
                    // Place the destination marker over the selected room marker
                    if (mPoint2 == null) { // If the destination marker doesn't exist
                        mPoint2 = mMap.addMarker(new MarkerOptions().position(marker.getPosition())
                                .icon(BitmapDescriptorFactory.defaultMarker(HUE_IAGRN)).zIndex(101));
                    } else {
                        mPoint2.setPosition(marker.getPosition());
                        if (!mPoint2.isVisible()) mPoint2.setVisible(true);
                        mPoint2.setAlpha(1);
                    }
                    mPoint2Floor = focusedFloor;
                    mPoint2Building = focusedBuilding;

                    // Remove any previous path
                    if (polyline != null) {
                        Iterator<Polyline> pol = polyline.iterator();
                        while (pol.hasNext()) pol.next().remove();
                    }
                    pathResponse = "";
                    markedRoom = displayedRooms.get(marker);
                    mActivity.bottomMenuMarkerOpen("Name: " + displayedRooms.get(marker).getName(), "Floor: " + displayedRooms.get(marker).getFloor(), "Building: " + focusedBuilding, "", true);

                    // Get a new path
                    getPath();
                }
            }
            return true;
        }
    };

    /**
     * Sets the destination marker with the same properties as selected room selected from the
     * search menu.
     * @param room the room selected from the search menu.
     */
    public void markerSearch(Room room) {
        if (mPoint2 == null) {
            mPoint2 = mMap.addMarker(new MarkerOptions().position(room.getLatLng())
                    .icon(BitmapDescriptorFactory.defaultMarker(HUE_IAGRN)).zIndex(101));
        }
        else {
            mPoint2.setPosition(room.getLatLng());
            if (!mPoint2.isVisible()) mPoint2.setVisible(true);
            mPoint2.setAlpha(1);
        }
        mPoint2Floor = room.getFloor();
        mPoint2Building = "ComputerScience";

        // Remove any previous path
        if (polyline != null) {
            Iterator<Polyline> pol = polyline.iterator();
            while (pol.hasNext()) pol.next().remove();
        }
        pathResponse = "";
        markedRoom = room;
        mActivity.bottomMenuMarkerOpen("Name: " + markedRoom.getName(), "Floor: " + markedRoom.getFloor(), "Building: " + focusedBuilding, "", true);

        // Draw a new path from the user marker to the destination marker
        getPath();
    }

    /**
     * Performs the request to the server to get a viable path from the user marker to the
     * destination marker.
     */
    public void getPath() {
        if (mMarker != null) {
            jsonInnerPathData = new JSONObject();
            try {
                jsonInnerPathData.put("startBuildingName", "ComputerScience");
                jsonInnerPathData.put("startFloor", Integer.toString(mMarkerFloor));
                jsonInnerPathData.put("startLongitude", Double.toString(mMarker.getPosition().longitude));
                jsonInnerPathData.put("startLatitude", Double.toString(mMarker.getPosition().latitude));
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
            BackendRequest t = new BackendRequest("path", jsonPathData.toString(), false);

            t.execute();
        }
    }

    /**
     * Draws the visible segment of the path between the user marker and the destination marker
     * based on the focused floor plan.
     * @param response a viable path between the user marker and the destination marker returned
     *                 from the server.
     */
    public static void drawPolyline(String response) {
        pathResponse = response;
        try {
            JSONObject jsonObject = new JSONObject(response);

            if (jsonObject.has("mapData")) {
                JSONObject jsonObject2 = jsonObject.getJSONObject("mapData");
                if (jsonObject2.has("path"))  {

                    JSONArray path = jsonObject2.getJSONArray("path");
                    if (polyline != null) {
                        Iterator<Polyline> pol = polyline.iterator();
                        while (pol.hasNext()) pol.next().remove();
                    }
                    polyline = new HashSet<Polyline>();
                    for (int i = 0; i < path.length() - 1; i++) {
                        JSONObject sourceJSON = path.getJSONObject(i);
                        JSONObject targetJSON = path.getJSONObject(i + 1);
                        if (sourceJSON.has("floor") && targetJSON.has("floor")) {
                            String sourceFloor = sourceJSON.getString("floor");
                            String targetFloor = targetJSON.getString("floor");
                            if (sourceFloor.equals(targetFloor) && Integer.parseInt(sourceFloor) == focusedFloor) {
                                if (sourceJSON.has("latitude") && sourceJSON.has("longitude") && targetJSON.has("latitude") && targetJSON.has("longitude")) {
                                    LatLng source = new LatLng(Double.valueOf(sourceJSON.getString("latitude")), Double.valueOf(sourceJSON.getString("longitude")));
                                    LatLng target = new LatLng(Double.valueOf(targetJSON.getString("latitude")), Double.valueOf(targetJSON.getString("longitude")));
                                    final PolylineOptions rectOptions = new PolylineOptions();
                                    rectOptions.add(source, target).color(Color.RED);
                                    polyline.add(mMap.addPolyline(rectOptions));
                                    Polyline u = (Polyline) polyline.toArray()[0];
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

    /**
     * Sets the bottom dialog menu (the sliding up menu) if a destination marker is placed
     */
    public void bottomDialogIfMarker() {
        if (mPoint2 != null && isFocused && focusedBuilding.equals(mPoint2Building)) {
            if (mPoint2.isVisible() && focusedBuilding.equals(mPoint2Building)) {
                if (focusedFloor == mPoint2Floor) mPoint2.setAlpha(1);
                if (markedRoom == null) mActivity.bottomMenuMarkerOpen("Latitude: " + mPoint2.getPosition().latitude, "Longitude: " + mPoint2.getPosition().longitude, "Floor: " + mPoint2Floor, "Building: " + focusedBuilding, false);
                else mActivity.bottomMenuMarkerOpen("Name: " + markedRoom.getName(), "Floor: " + markedRoom.getFloor(), "Building: " + focusedBuilding, "", true);
            }
        }
    }

    private GoogleMap.OnGroundOverlayClickListener mGroundOverlayClickListener = new GoogleMap.OnGroundOverlayClickListener() {
        @Override
        public void onGroundOverlayClick(GroundOverlay groundOverlay) {
            // Moving focus from one building directly to another
            if (!mActivity.getNaviationMode()) {
                if (focusedGroundOverlay != null && !groundOverlay.equals(focusedGroundOverlay)) {
                    Iterator<Marker> keysIterator = displayedRooms.keySet().iterator();
                    while (keysIterator.hasNext()) keysIterator.next().remove(); // Remove rooms
                    Iterator<Polyline> pol = polyline.iterator();
                    while (pol.hasNext()) pol.next().remove(); // Remove path
                    focusedGroundOverlay.setTransparency(0.5f);
                    focusedGroundOverlay.setClickable(true);
                    if (focusedFloor != 0) changeFloorPlans(focusedBuildingFloorPlans.get(0));
                    buildingOverlays.put(focusedGroundOverlay, focusedBuilding);
                }

                focusedGroundOverlay = groundOverlay;
                focusedGroundOverlay.setTransparency(0.0f);
                focusedGroundOverlay.setClickable(false);

                // Need to make request to get floorplan information and room data
                focusedBuilding = buildingOverlays.get(focusedGroundOverlay);
                focusedFloor = 0;
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

                // Makes down icon grey since each building starts with the ground floor
                ColorStateList colorStateList = new ColorStateList(states, colorGrey);
                mActivity.getFabDown().setBackgroundTintList(colorStateList);

                ColorStateList colorStateList2 = new ColorStateList(states, colorBlue);
                mActivity.getFabUp().setBackgroundTintList(colorStateList2);

                mActivity.getFabUp().show();
                mActivity.getFabDown().show();

                // Get a path if a destination marker is placed
                if (mPoint2 != null) {
                    if (mPoint2.isVisible() && mPoint2Building.equals(focusedBuilding)) getPath();
                }

                isFocused = true;

                bottomDialogIfMarker();
            }

        }
    };

}
