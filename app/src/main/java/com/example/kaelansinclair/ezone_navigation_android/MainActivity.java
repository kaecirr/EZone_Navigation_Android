package com.example.kaelansinclair.ezone_navigation_android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.GroundOverlay;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import info.debatty.java.stringsimilarity.LongestCommonSubsequence;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String PREFS_NAME = "IsLoggedIn";
    private static int PRIVATE_MODE = 0;
    private static boolean first = true;

    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private EditText edtSeach;

    private Map map;

    private SlidingUpPanelLayout mBottomMenuLayout;
    private Button mNavigateButton;

    private FloatingActionButton fab;
    private FloatingActionButton fabUp;
    private FloatingActionButton fabDown;

    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private static final String TAG = "IADemo";

    private IATracking tracker;

    private boolean navigationMode = false;

    private boolean started = false;

    private int panelHeight = 0;

    private View mRootView;

    private Room entry;

    private boolean updateBottomSheet;

    ArrayList<Room> roomHold;

    public IATracking getTracker() {
        return tracker;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!started) {

            updateBottomSheet = true;

            map = new Map(this);
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("EZone Indoor Navigation");

            fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    map.setCameraPositionNeedsUpdating(true);
                    map.updateCameraPosition();
                }
            });

            fabUp = (FloatingActionButton) findViewById(R.id.fab_up);
            fabUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    map.changeFloor(1);
                }
            });

            fabDown = (FloatingActionButton) findViewById(R.id.fab_down);
            fabDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    map.changeFloor(-1);
                }
            });

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            //Commands to disable the drawer menu. If you need to edit the menu items, just comment out these two lines.
            toggle.setDrawerIndicatorEnabled(false);
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, PRIVATE_MODE);
            if (first) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("logged", false); // set it to false when the user is logged out
                editor.commit();
                first = false;
            }

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

            // prevent the screen going to sleep while app is on foreground
            findViewById(android.R.id.content).setKeepScreenOn(true);

            tracker = new IATracking(map, this);

            //IARegion r = IARegion.floorPlan("0dc8358c-9e1e-4afa-8adb-3bdfb7154a88");

            //GroundOverlay test1 = null;

            //tracker.test(r, test1, true, "compSciBuilding");

            mBottomMenuLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
            mBottomMenuLayout.setTouchEnabled(false);
            mBottomMenuLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            panelHeight = mBottomMenuLayout.getPanelHeight();

            mNavigateButton = (Button) findViewById(R.id.navigate_here);
            mNavigateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (navigationMode) {
                        navigationMode = false;
                        tracker.getIALocationManager().unregisterRegionListener(tracker.getRegionListener());
                        mNavigateButton.setText("NAVIGATE HERE");
                        map.changeFABColour();
                        fabDown.show();
                        fabUp.show();
                        mSearchAction.setVisible(true);
                        map.displayRooms();
                    }
                    else {
                        navigationMode = true;
                        tracker.getIALocationManager().registerRegionListener(tracker.getRegionListener());
                        mNavigateButton.setText("STOP NAVIGATION");
                        fabDown.hide();
                        fabUp.hide();
                        mSearchAction.setVisible(false);
                        map.removeDisplayedRooms();
                    }
                }
            });

            roomHold = new ArrayList<Room>();

            //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            mRootView = getWindow().getDecorView().getRootView();

            mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Rect measureRect = new Rect(); //you should cache this, onGlobalLayout can get called often
                    mRootView.getWindowVisibleDisplayFrame(measureRect);
                    // measureRect.bottom is the position above soft keypad
                    int keypadHeight = mRootView.getRootView().getHeight() - measureRect.bottom;

                    if (keypadHeight > 0 && updateBottomSheet && isSearchOpened) {

                        updateBottomSheet = false;

                        TypedValue tv = new TypedValue();
                        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                            int statusBarHeight = 0;
                            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                            if (resourceId > 0) {
                                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
                                mBottomMenuLayout.setPanelHeight((getWindow().getDecorView().getHeight() - keypadHeight) - (TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics()) + statusBarHeight));
                            }
                        }

                        // keyboard is opened
                        mBottomMenuLayout.setOverlayed(true);
                        mBottomMenuLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                        Log.d(TAG, "onGlobalLayout: ");
                    } else if (keypadHeight == 0 && !isSearchOpened && !updateBottomSheet) {

                        updateBottomSheet = true;
                        //store keyboard state to use in onBackPress if you need to
                        mBottomMenuLayout.setPanelHeight(0);
                        mBottomMenuLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

                        if (entry != null) {
                            map.markerSearch(entry);
                            map.focusOnFloorPlan(false);
                            entry = null;
                        }
                        else map.bottomDialogIfMarker();
                    } else if (keypadHeight == 0 && isSearchOpened && !updateBottomSheet) {
                        TypedValue tv = new TypedValue();
                        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                            int statusBarHeight = 0;
                            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                            if (resourceId > 0) {
                                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
                                mBottomMenuLayout.setPanelHeight((getWindow().getDecorView().getHeight()) - (TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics()) + statusBarHeight));
                            }
                        }

                        updateBottomSheet = true;
                    }
                }
            });

            started = true;
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

        //if (map.getMap() == null) {
        Log.d("bugger, bugger", "onResume: ");
        SupportMapFragment mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        mapFragment.getMapAsync(map);
       // }

        tracker.getIALocationManager().requestLocationUpdates(IALocationRequest.create(), tracker.getIALocationListener());
        if (navigationMode) tracker.getIALocationManager().registerRegionListener(tracker.getRegionListener());

        map.updateCameraPosition();

        updateBottomSheet = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        tracker.getIALocationManager().removeLocationUpdates(tracker.getIALocationListener());
        if (navigationMode) tracker.getIALocationManager().unregisterRegionListener(tracker.getRegionListener());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.getIALocationManager().destroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        }else if(isSearchOpened) {
            handleMenuSearch();
            bottomMenuMarkerClose(false);
            map.bottomDialogIfMarker();
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

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        updateBottomSheet = true;

    }

    protected void handleMenuSearch() {
        final ActionBar action = getSupportActionBar(); //get the actionbar
        final ListView myLayout = (ListView) findViewById(R.id.scroll_linear);
        myLayout.setAdapter(new ArrayAdapter<Room>(getApplicationContext(), android.R.layout.simple_list_item_1, new ArrayList<Room>()));

        if (isSearchOpened) { //test if the search is open

            isSearchOpened = false;

            //hides the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edtSeach.getWindowToken(), 0);

            action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
            action.setDisplayShowTitleEnabled(true); //show the title in the action bar

            //add the search icon in the action bar
            mSearchAction.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_search));
        } else { //open the search entry

            isSearchOpened = true;

            bottomMenuMarkerClose(true);

            action.setDisplayShowCustomEnabled(true); //enable it to display a
            // custom view in the action bar.
            action.setCustomView(R.layout.search_bar);//add the custom view
            action.setDisplayShowTitleEnabled(false); //hide the title

            edtSeach = (EditText) action.getCustomView().findViewById(R.id.edtSearch); //the text editor
            edtSeach.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //mBottomMenuLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    if (s.length() != 0) {
                        roomHold = map.getSearchRooms();
                        final String chars = s.toString();
                        Collections.sort(roomHold, new Comparator<Room>() {
                            @Override
                            public int compare(Room o1, Room o2) {
                                LongestCommonSubsequence lcs = new LongestCommonSubsequence();
                                double d1 = lcs.distance(o1.getName(), chars);
                                double d2 = lcs.distance(o2.getName(), chars);
                                return Double.compare(d1, d2);
                            }
                        });

                        Log.d(TAG, "onTextChanged: " + roomHold.size());

                        ArrayAdapter<Room> adapter = new ArrayAdapter<Room>(getApplicationContext(), android.R.layout.simple_list_item_1, roomHold.subList(0, 10));
                        myLayout.setAdapter(adapter);
                        Log.d(TAG, "onTextChanged: " + myLayout.getAdapter().getCount());

                        myLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                isSearchOpened = false;
                                entry = (Room) parent.getItemAtPosition(position);
                                updateBottomSheet = false;
                                myLayout.setAdapter(new ArrayAdapter<Room>(getApplicationContext(), android.R.layout.simple_list_item_1, new ArrayList<Room>()));
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(edtSeach.getWindowToken(), 0);
                                action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
                                action.setDisplayShowTitleEnabled(true); //show the title in the action bar

                                //add the search icon in the action bar
                                mSearchAction.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_search));
                            }
                        });
                    }
                    else {
                        myLayout.setAdapter(new ArrayAdapter<Room>(getApplicationContext(), android.R.layout.simple_list_item_1, new ArrayList<Room>()));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            //this is a listener to do a search when the user clicks on search button
            edtSeach.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        isSearchOpened = false;
                        entry = (Room) myLayout.getAdapter().getItem(0);
                        myLayout.setAdapter(new ArrayAdapter<Room>(getApplicationContext(), android.R.layout.simple_list_item_1, new ArrayList<Room>()));
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(edtSeach.getWindowToken(), 0);
                        action.setDisplayShowCustomEnabled(false); //disable a custom view inside the actionbar
                        action.setDisplayShowTitleEnabled(true); //show the title in the action bar

                        //add the search icon in the action bar
                        mSearchAction.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_search));
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
        }
    }

    public boolean getNaviationMode() {return navigationMode;}

    public void bottomMenuMarkerOpen(String text1, String text2, String text3, String text4, boolean room) {
       // if (mBottomMenuLayout.getPanelState().equals(SlidingUpPanelLayout.PanelState.HIDDEN)) {
            Log.d(TAG, "bottomMenuMarkerOpen: " + panelHeight);
            mBottomMenuLayout.setOverlayed(false);
            mBottomMenuLayout.setPanelHeight(panelHeight);
            mBottomMenuLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        //}
        TextView name = (TextView) findViewById(R.id.name);
        name.setVisibility(View.VISIBLE);
        name.setText(text1);

        TextView roomNumber = (TextView) findViewById(R.id.room_number);
        roomNumber.setVisibility(View.VISIBLE);
        roomNumber.setText(text2);

        TextView floor = (TextView) findViewById(R.id.floor);
        floor.setVisibility(View.VISIBLE);
        floor.setText(text3);

        TextView building = (TextView) findViewById(R.id.building);
        building.setVisibility(View.VISIBLE);
        building.setText(text4);

        Button navigateHere = (Button) findViewById(R.id.navigate_here);
        navigateHere.setVisibility(View.VISIBLE);

        if(room) {
            Button readMore = (Button) findViewById(R.id.read_more);
            //readMore.setVisibility(View.VISIBLE);
        }
    }

    public void bottomMenuMarkerClose(boolean search) {
        if (!search) {
            mBottomMenuLayout.setPanelHeight(0);
            mBottomMenuLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        }

        TextView name = (TextView) findViewById(R.id.name);
        name.setVisibility(View.GONE);

        TextView roomNumber = (TextView) findViewById(R.id.room_number);
        roomNumber.setVisibility(View.GONE);

        TextView floor = (TextView) findViewById(R.id.floor);
        floor.setVisibility(View.GONE);

        TextView building = (TextView) findViewById(R.id.building);
        building.setVisibility(View.GONE);

        Button readMore = (Button) findViewById(R.id.read_more);
        readMore.setVisibility(View.GONE);

        Button navigateHere = (Button) findViewById(R.id.navigate_here);
        navigateHere.setVisibility(View.GONE);
    }

    public FloatingActionButton getFabUp() {return fabUp;}

    public FloatingActionButton getFabDown() {return fabDown;}

    public SlidingUpPanelLayout getBottomMenuLayout() {return mBottomMenuLayout;}

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
            else startActivity(new Intent(MainActivity.this, LoginActivity.class));
        } else if (id == R.id.nav_favourites) {
        } else if (id == R.id.nav_recentlocations) {
        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_about) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Checks that we have access to required information, if not ask for users permission.
     */
    public void ensurePermissions() {

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
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
                            }
                        })
                        .setNegativeButton(R.string.permission_button_deny, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this,
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
}
