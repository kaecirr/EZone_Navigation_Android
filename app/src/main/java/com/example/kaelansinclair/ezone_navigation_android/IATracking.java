package com.example.kaelansinclair.ezone_navigation_android;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
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

import java.util.Iterator;

/**
 * Contains most of the IndoorAtlas functionality. As much has been split from the rest of the code
 * as possible. This is to allow for easier integration of a new indoor positioning system if that
 * is required.
 */

public class IATracking {

    private static final int MAX_DIMENSION = 2048;
    private static final String TAG = "IADemo";

    private Map map;

    private IARegion mOverlayFloorPlan = null;

    //private GroundOverlay mGroundOverlay = null;
    private IATask<IAFloorPlan> mFetchFloorPlanTask;
    private IALocationManager mIALocationManager;
    private IAResourceManager mResourceManager;
    private Target mLoadTarget;
    private MainActivity mActivity;

    public IATracking(Map map, MainActivity mActivity) {

        this.map = map;
        this.mActivity = mActivity;

        mIALocationManager = IALocationManager.create(mActivity.getApplicationContext());
        mResourceManager = IAResourceManager.create(mActivity.getApplicationContext());
    }

    public IALocationManager getIALocationManager() {return mIALocationManager;}

    public IAResourceManager getResourceManager() {return mResourceManager;}

    public IARegion getOverlayFloorPlan() {return mOverlayFloorPlan;}

    private IALocationListener mListener = new IALocationListenerSupport() {

        @Override
        public void onLocationChanged(IALocation location) {
            // Update user location
            map.updateLocation(new LatLng(location.getLatitude(), location.getLongitude()), location.getAccuracy(), location.getRegion(), location.getFloorLevel());
       }

    };

    public IALocationListener getIALocationListener() {return mListener;}

    /**
     * Listener that changes overlay if needed
     */
    private IARegion.Listener mRegionListener = new IARegion.Listener() {

        @Override
        public void onEnterRegion(IARegion region) {
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {

                final String newId = region.getId();

                setFetchFloorPlan(region, map.getFocusedGroundOverlay(), false, map.getFocusedBuilding());
                Iterator<Integer> floorNums = map.getFocusedBuildingFloorPlans().keySet().iterator();
                while (floorNums.hasNext()) {
                    int num = floorNums.next();
                    if (map.getFocusedBuildingFloorPlans().get(num).equals(newId)) {
                        map.setFocusedFloor(num);
                        break;
                    }
                }

                map.setFocusedRegion(region);

                map.mPoint2Dim();
            }
        }

        @Override
        public void onExitRegion(IARegion region) {
            if (map.getFocusedGroundOverlay() != null) {
                // Indicate we left this floor plan but leave it there for reference
                // If we enter another floor plan, this one will be removed and another one loaded
                //map.getFocusedGroundOverlay().setTransparency(0.5f);
                // TODO: 2/11/2017
            }
        }

    };

    public IARegion.Listener getRegionListener() {return mRegionListener;}

    /**
     * Set up the required information to fetch a new floor plan.
     * @param region the new region to get a floor plan for.
     * @param groundOverlay the GroundOverlay object to be replaced.
     * @param initialise whether this is in initialisation mode (only on application startup).
     * @param building the name of the building the floor plan belongs to.
     */
    public void setFetchFloorPlan(IARegion region, GroundOverlay groundOverlay, boolean initialise, String building) {
        if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
            final String newId = region.getId();
            // Are we entering a new floor plan or coming back the floor plan we just left?
            if (groundOverlay == null || !region.equals(mOverlayFloorPlan)) {
                //map.setCameraPositionNeedsUpdating(true);
                if (groundOverlay != null) {
                    groundOverlay.remove();
                    groundOverlay = null;
                }
                mOverlayFloorPlan = region; // overlay will be this (unless error in loading)

                fetchFloorPlan(newId, initialise, building);
            } else {
                groundOverlay.setTransparency(0.0f);
            }
        }
    }

    /**
     * Sets bitmap of floor plan as ground overlay on Google Maps
     */
    private void setupGroundOverlay(IAFloorPlan floorPlan, Bitmap bitmap, boolean initialise, String building) {
        if (map.getFocusedGroundOverlay() != null) {
            map.getFocusedGroundOverlay().remove();
        }

        if (map.getMap() != null) {
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            IALatLng iaLatLng = floorPlan.getCenter();
            LatLng[] centres = {new LatLng(-31.97761953692513, 115.81618666648865), new LatLng(-31.977629775312284, 115.81619001924993), new LatLng(-31.977641435696277, 115.81617224961522)};
            LatLng center = new LatLng(iaLatLng.latitude, iaLatLng.longitude);
            center = centres[map.getFocusedFloor()];
            GroundOverlayOptions fpOverlay = new GroundOverlayOptions()
                    .image(bitmapDescriptor)
                    .position(center, floorPlan.getWidthMeters(), floorPlan.getHeightMeters())
                    .bearing(floorPlan.getBearing());

            map.setFocusedGroundOverlay(map.getMap().addGroundOverlay(fpOverlay));
            if (!map.getIsFocused()) {
                map.getFocusedGroundOverlay().setClickable(true);
                map.getFocusedGroundOverlay().setTransparency(0.5f);
            }
        }

        if (initialise) map.getBuildingOverlays().put(map.getFocusedGroundOverlay(), building);
    }

    /**
     * Download floor plan using Picasso library.
     */
    private void fetchFloorPlanBitmap(final IAFloorPlan floorPlan, final boolean initialise, final String building) {

        final String url = floorPlan.getUrl();

        if (mLoadTarget == null) {
            mLoadTarget = new Target() {

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    setupGroundOverlay(floorPlan, bitmap, initialise, building);
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

        RequestCreator request = Picasso.with(mActivity).load(url);

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
    private void fetchFloorPlan(String id, final boolean initialise, final String building) {

        // if there is already running task, cancel it
        cancelPendingNetworkCalls();

        final IATask<IAFloorPlan> task = mResourceManager.fetchFloorPlanWithId(id);

        task.setCallback(new IAResultCallback<IAFloorPlan>() {

            @Override
            public void onResult(IAResult<IAFloorPlan> result) {

                if (result.isSuccess() && result.getResult() != null) {
                    // retrieve bitmap for this floor plan metadata
                    fetchFloorPlanBitmap(result.getResult(), initialise, building);
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
        final Snackbar snackbar = Snackbar.make(mActivity.findViewById(android.R.id.content), text,
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
