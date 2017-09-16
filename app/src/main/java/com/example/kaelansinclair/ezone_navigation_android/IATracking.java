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

/**
 * Created by Kaelan Sinclair on 27/08/2017.
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

            Log.d(TAG, "new location received with coordinates: " + location.getLatitude()
                    + "," + location.getLongitude());

            map.updateLocation(new LatLng(location.getLatitude(), location.getLongitude()), location.getAccuracy());
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
                // Are we entering a new floor plan or coming back the floor plan we just left?
                if (map.getFocusedGroundOverlay() == null || !region.equals(mOverlayFloorPlan)) {
                    map.setCameraPositionNeedsUpdating(true);
                    if (map.getFocusedGroundOverlay() != null) {
                        map.setCameraPositionNeedsUpdating(false);
                        map.getFocusedGroundOverlay().remove();
                        map.setFocusedGroundOverlay(null);
                    }
                    mOverlayFloorPlan = region; // overlay will be this (unless error in loading)
                    fetchFloorPlan(newId, map.getFocusedGroundOverlay());
                } else {
                    map.getFocusedGroundOverlay().setTransparency(0.0f);
                }
            }
            // showInfo("Enter " + (region.getType() == IARegion.TYPE_VENUE
            //       ? "VENUE "
            //     : "FLOOR_PLAN ") + region.getId());
        }

        @Override
        public void onExitRegion(IARegion region) {
            if (map.getFocusedGroundOverlay() != null) {
                // Indicate we left this floor plan but leave it there for reference
                // If we enter another floor plan, this one will be removed and another one loaded
                map.getFocusedGroundOverlay().setTransparency(0.5f);
            }
            // showInfo("Enter " + (region.getType() == IARegion.TYPE_VENUE
            //       ? "VENUE "
            //     : "FLOOR_PLAN ") + region.getId());
        }

    };

    public IARegion.Listener getRegionListener() {return mRegionListener;}

    public void test(IARegion region, GroundOverlay groundOverlay) {
        if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
            final String newId = region.getId();
            // Are we entering a new floor plan or coming back the floor plan we just left?
            if (groundOverlay == null || !region.equals(mOverlayFloorPlan)) {
                map.setCameraPositionNeedsUpdating(true);
                if (groundOverlay != null) {
                    groundOverlay.remove();
                    groundOverlay = null;
                }
                mOverlayFloorPlan = region; // overlay will be this (unless error in loading)

                fetchFloorPlan(newId, groundOverlay);
            } else {
                groundOverlay.setTransparency(0.0f);
            }
        }
        // showInfo("Enter " + (region.getType() == IARegion.TYPE_VENUE
        //       ? "VENUE "
        //     : "FLOOR_PLAN ") + region.getId());
    }

    /**
     * Sets bitmap of floor plan as ground overlay on Google Maps
     */
    private void setupGroundOverlay(IAFloorPlan floorPlan, Bitmap bitmap, GroundOverlay groundOverlay) {

        Log.d(TAG, "removeBlah2");
        if (groundOverlay != null) {
            Log.d(TAG, "friggenHeck");
            groundOverlay.remove();
        }

        if (map.getMap() != null) {
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
            IALatLng iaLatLng = floorPlan.getCenter();
            LatLng center = new LatLng(iaLatLng.latitude, iaLatLng.longitude);
            GroundOverlayOptions fpOverlay = new GroundOverlayOptions()
                    .image(bitmapDescriptor)
                    .position(center, floorPlan.getWidthMeters(), floorPlan.getHeightMeters())
                    .bearing(floorPlan.getBearing());

            groundOverlay = map.getMap().addGroundOverlay(fpOverlay);
            if (!map.getIsFocused()) {
                groundOverlay.setClickable(true);
            }
            groundOverlay.setTransparency(0.5f);
        }
    }

    /**
     * Download floor plan using Picasso library.
     */
    private void fetchFloorPlanBitmap(final IAFloorPlan floorPlan, final GroundOverlay groundOverlay) {

        final String url = floorPlan.getUrl();

        if (mLoadTarget == null) {
            mLoadTarget = new Target() {

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Log.d(TAG, "onBitmap loaded with dimensions: " + bitmap.getWidth() + "x"
                            + bitmap.getHeight());
                    setupGroundOverlay(floorPlan, bitmap, groundOverlay);
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
    private void fetchFloorPlan(String id, final GroundOverlay groundOverlay) {

        // if there is already running task, cancel it
        cancelPendingNetworkCalls();

        final IATask<IAFloorPlan> task = mResourceManager.fetchFloorPlanWithId(id);

        task.setCallback(new IAResultCallback<IAFloorPlan>() {

            @Override
            public void onResult(IAResult<IAFloorPlan> result) {

                if (result.isSuccess() && result.getResult() != null) {
                    // retrieve bitmap for this floor plan metadata
                    fetchFloorPlanBitmap(result.getResult(), groundOverlay);
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
