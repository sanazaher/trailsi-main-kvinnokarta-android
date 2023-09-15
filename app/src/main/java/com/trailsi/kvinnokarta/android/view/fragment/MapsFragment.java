package com.trailsi.kvinnokarta.android.view.fragment;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.ImageSource;
import com.trailsi.kvinnokarta.android.R;
import com.trailsi.kvinnokarta.android.common.Constant;
import com.trailsi.kvinnokarta.android.common.cache.CachedData;
import com.trailsi.kvinnokarta.android.common.db.DBManager;
import com.trailsi.kvinnokarta.android.common.utils.StringHelper;
import com.trailsi.kvinnokarta.android.common.utils.TimeHelper;
import com.trailsi.kvinnokarta.android.model.Audio;
import com.trailsi.kvinnokarta.android.model.AudioAds;
import com.trailsi.kvinnokarta.android.model.AudioType;
import com.trailsi.kvinnokarta.android.model.Location;
import com.trailsi.kvinnokarta.android.view.activity.main.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, MapboxMap.OnMapClickListener {

    public final static String TAG = "MapsFragment";

    private MainActivity activity;

    private MapView mapView;
    private static final String SOURCE_IMAGE = "SOURCE_IMAGE";
    private static final String LAYER_IMAGE = "LAYER_IMAGE";
    private static final String SOURCE_ROUTE = "SOURCE_ROUTE";
    private static final String LAYER_ROUTE = "LAYER_ROUTE";
    private static final String SOURCE_IMAGE_LOCATION = "SOURCE_IMAGE_LOCATION";
    private static final String SOURCE_MARKER_GEOJSON = "SOURCE_POI_GEOJSON";
    private static final String SOURCE_IMAGE_MARKER = "MARKER_IMAGE_ID";
    private static final String LAYER_MARKER = "MARKER_LAYER_ID";
    private static final String MARKER_PROPERTY_ID = "MARKER_PROPERTY_ID";
    private static final String MARKER_PROPERTY_NAME = "MARKER_PROPERTY_NAME";

    private MapboxMap mMapboxMap;
    private SymbolManager symbolManager;
    private Symbol symbol;

    private MediaPlayer mPlayer;
    private ObjectAnimator anim;

    private List<Location> mLocations;
    private LatLng myLocation;
    private long mLastGPSTime;
    private int mLastPointNumber;
    private int mNewPointNumber;
    private List<AudioAds> mAds;

    private LinearLayout layoutPlayerPanel;
    private LinearLayout layoutLocation;
    private ImageView imgLocation;
    private TextView txtLocationName;
    private ImageView imgPlay;
    private SeekBar seekBar;
    private TextView txtStartTime, txtEndTime;
    private AdView adView;

    public MapsFragment() {
    }

    public MapsFragment(MainActivity activity) {

        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(activity, getString(R.string.mapbox_access_token));
        setHasOptionsMenu(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);



        initVariable();
        initUI(view, savedInstanceState);

        return view;
    }

    private void initVariable() {
        if (activity == null) {
            activity = (MainActivity) getActivity();
        }
        mLocations = new ArrayList<>();
        mPlayer = new MediaPlayer();
        mLastPointNumber = 0;
        mNewPointNumber = 0;
        mLastGPSTime = 0;
        mAds = new ArrayList<>();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initUI(View view, Bundle savedInstanceState) {

        adView = view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        layoutPlayerPanel = view.findViewById(R.id.layout_player_panel);
        layoutPlayerPanel.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        layoutLocation = view.findViewById(R.id.layout_location);
        imgLocation = view.findViewById(R.id.img_location_photo);
        txtLocationName = view.findViewById(R.id.txt_location_name);

        ImageView imgMyLocation = view.findViewById(R.id.img_my_location);
        imgPlay = view.findViewById(R.id.img_play);
        ImageView imgRefresh = view.findViewById(R.id.img_refresh);
        seekBar = view.findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        txtStartTime = view.findViewById(R.id.txt_start_time);
        txtEndTime = view.findViewById(R.id.txt_end_time);

        imgMyLocation.setOnClickListener(this);
        imgPlay.setOnClickListener(this);
        imgRefresh.setOnClickListener(this);

        anim = ObjectAnimator.ofFloat(imgRefresh, "rotation", 360, 0);
        anim.setDuration(2000);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ObjectAnimator.RESTART);

        hidePlayerPanel();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mapView.getMapAsync(MapsFragment.this);
                    }
                });
            }
        }, 300);
    }

    private void showPlayerPanel() {
        layoutPlayerPanel.setVisibility(View.VISIBLE);
    }

    private void hidePlayerPanel() {
        layoutPlayerPanel.setVisibility(View.GONE);
    }

    private void showPOIView(Location location) {
        layoutLocation.setVisibility(View.VISIBLE);
        File image = DBManager.getInstance().getRealFile(activity, location.image);
        if (image != null && image.exists()) {
            Glide.with(activity)
                    .load(image)
                    .into(imgLocation);
        }

        txtLocationName.setText(location.name);
    }

    private void hidePOIView() {
        layoutLocation.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.img_my_location) {
            showMyLocation(true);
        } else if (v.getId() == R.id.img_play) {
            if (mPlayer.isPlaying()) {
                pauseAudio();
            } else {
                playAudio();
            }
        } else if (v.getId() == R.id.img_refresh) {
            refresh();
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mMapboxMap = mapboxMap;
        mMapboxMap.getUiSettings().setCompassEnabled(true);
        mMapboxMap.getUiSettings().setCompassFadeFacingNorth(false);
        mMapboxMap.getUiSettings().setCompassMargins(0,150,0,0);
        activity.startSyncService(Constant.DOWNLOAD_FILE);
    }

    public void loadLocations() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Location> locations = new ArrayList<>();
                DBManager.getInstance().getLocations(locations);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (Location location : locations) {
                            // If type is List customized, we can check if there is audio for this tour
                            Audio audio = DBManager.getInstance().getAudio(location.number);
                            if (audio != null) {
                                mLocations.add(location);
                            }
                        }
                        updateView();
                    }
                });
            }
        }).start();
    }

    public void updateView() {
        Log.e(TAG, "Update View: " + (mMapboxMap == null));
        if (mMapboxMap == null) {
            return;
        }

        Style.Builder builder = new Style.Builder().fromUri("mapbox://styles/trailsialnarp/ckyk85b8ramhp15qq06dy5xqi");
        mMapboxMap.setStyle(builder, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        setUpStyle(style);
                    }
                }
        );
    }


    /**
     * Sets up all of the sources and layers needed for this example
     */
    public void setUpStyle(Style style) {
        setUpMap(style);
        setUpPlaceMarkers(style);
        setUpMyLocationMarker(style);
    }

    private void setUpMap(@NonNull Style loadedStyle) {
        double topLeftLat = CachedData.getDouble(CachedData.kPlaceTopLeftLatitude, 0);
        double topLeftLng = CachedData.getDouble(CachedData.kPlaceTopLeftLongitude, 0);
        double topRightLat = CachedData.getDouble(CachedData.kPlaceTopRightLatitude, 0);
        double topRightLng = CachedData.getDouble(CachedData.kPlaceTopRightLongitude, 0);
        double bottomRightLat = CachedData.getDouble(CachedData.kPlaceBottomRightLatitude, 0);
        double bottomRightLng = CachedData.getDouble(CachedData.kPlaceBottomRightLongitude, 0);
        double bottomLeftLat = CachedData.getDouble(CachedData.kPlaceBottomLeftLatitude, 0);
        double bottomLeftLng = CachedData.getDouble(CachedData.kPlaceBottomLeftLongitude, 0);

        double centerLat = (topLeftLat + bottomRightLat) / 2;
        double centerLng = (topLeftLng + bottomRightLng) / 2;

        LatLng CENTER = new LatLng(centerLat, centerLng);
        CameraPosition position = new CameraPosition.Builder()
                .target(CENTER)
                .zoom(14)
                .build();
        mMapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
        mMapboxMap.addOnMapClickListener(this);

        LatLng TOP_LEFT = new LatLng(topLeftLat, topLeftLng);
        LatLng TOP_RIGHT = new LatLng(topRightLat, topRightLng);
        LatLng BOTTOM_RIGHT = new LatLng(bottomRightLat, bottomRightLng);
        LatLng BOTTOM_LEFT = new LatLng(bottomLeftLat, bottomLeftLng);

        addMapImage(loadedStyle, TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT);

        AudioType audioType = DBManager.getInstance().getCurrentAudioType();
        if (audioType == null || StringHelper.isEmpty(audioType.geo_json)) {
            return;
        }
        try {
            loadedStyle.addSource(new GeoJsonSource(SOURCE_ROUTE, audioType.geo_json, new GeoJsonOptions()));
            LineLayer lineLayer = new LineLayer(LAYER_ROUTE, SOURCE_ROUTE);

            String strokeColor = "#B80C09";
            JSONObject jsonObject = new JSONObject(audioType.geo_json);
            if (jsonObject.has("features")) {
                JSONArray features = jsonObject.getJSONArray("features");
                if (features.length() > 0) {
                    JSONObject feature = features.getJSONObject(0);
                    if (feature.has("properties")) {
                        JSONObject properties = feature.getJSONObject("properties");
                        if (properties.has("stroke")) {
                            strokeColor = properties.getString("stroke");
                            Log.e(TAG, strokeColor);
                        }
                    }
                }
            }
            lineLayer.setProperties(PropertyFactory.lineColor(Color.parseColor(strokeColor)));
            lineLayer.setProperties(PropertyFactory.lineWidth(3.0F));
            loadedStyle.addLayer(lineLayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMapImage(@NonNull Style loadedStyle, LatLng TOP_LEFT, LatLng TOP_RIGHT, LatLng BOTTOM_RIGHT, LatLng BOTTOM_LEFT) {
        String placeMap = CachedData.getString(CachedData.kPlaceMapImage, "");
        if (StringHelper.isEmpty(placeMap)) {
            return;
        }
        File mapImage = DBManager.getInstance().getRealFile(activity, placeMap);
        if (mapImage == null) {
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(mapImage.getAbsolutePath());
        if (bitmap == null) {
            return;
        }

        LatLngQuad quad = new LatLngQuad(
                TOP_LEFT,
                TOP_RIGHT,
                BOTTOM_RIGHT,
                BOTTOM_LEFT
        );
        loadedStyle.addSource(new ImageSource(SOURCE_IMAGE, quad, bitmap));
        loadedStyle.addLayer(new RasterLayer(LAYER_IMAGE, SOURCE_IMAGE));
    }

    private void setUpPlaceMarkers(@NonNull Style loadedStyle) {
        List<Feature> featureList = new ArrayList<>();
        for (Location location : mLocations) {
            Feature feature = Feature.fromGeometry(Point.fromLngLat(location.longitude, location.latitude));
            feature.addStringProperty(MARKER_PROPERTY_ID, String.valueOf(location.number));
            feature.addStringProperty(MARKER_PROPERTY_NAME, location.name);
            featureList.add(feature);
        }

        loadedStyle.addSource(new GeoJsonSource(
                SOURCE_MARKER_GEOJSON,
                FeatureCollection.fromFeatures(featureList)
        ));
        loadedStyle.addImage(SOURCE_IMAGE_MARKER, BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_poi));
        loadedStyle.addLayer(new SymbolLayer(LAYER_MARKER, SOURCE_MARKER_GEOJSON)
                .withProperties(
                        textField(Expression.get(MARKER_PROPERTY_NAME)),
                        iconImage(SOURCE_IMAGE_MARKER),
                        iconAllowOverlap(true),
                        textAllowOverlap(true),
                        iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                        textAnchor(Property.TEXT_ANCHOR_CENTER)
                )
        );
    }

    private void setUpMyLocationMarker(@NonNull Style loadedStyle) {
        loadedStyle.addImage(SOURCE_IMAGE_LOCATION, BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_location));
        symbolManager = new SymbolManager(mapView, mMapboxMap, loadedStyle);
        symbolManager.setIconAllowOverlap(true);
        symbolManager.setTextAllowOverlap(true);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return handleClickIcon(mMapboxMap.getProjection().toScreenLocation(point));
    }

    private boolean handleClickIcon(PointF screenPoint) {
        List<Feature> features = mMapboxMap.queryRenderedFeatures(screenPoint, LAYER_MARKER);
        if (!features.isEmpty()) {
            String id = features.get(0).getStringProperty(MARKER_PROPERTY_ID);
            for (Location location : mLocations) {
                if (location.number == Integer.parseInt(id)) {
                    Log.e(TAG, "Clicked icon: " + id);
                    setInterestingPoint(location, true); // When click icon
                }
            }
            return true;
        } else {
            if (mPlayer == null || !mPlayer.isPlaying()) {
                hidePlayerPanel();
            }
            return false;
        }
    }

    /*
        If location is from GPS, always show my location, but it's from beacon, need to limit of 30 seconds.
        We can validate the beacon location is approximate, when there is no signal from GPS for 30 seconds.
        Always rely on GPS, Beacon is backup system
    */

    private void setMyLocation(LatLng latLng, boolean isFromGPS) {
        if (isFromGPS) {
            myLocation = latLng;
            mLastGPSTime = System.currentTimeMillis();
        } else {
            if ((System.currentTimeMillis() - mLastGPSTime) < 10 * 1000) {
                return;
            }
            myLocation = latLng;
        }
        showMyLocation(false);
    }

    private void showMyLocation(boolean isManual) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mMapboxMap == null) {
                    return;
                }
                if (symbolManager == null) {
                    return;
                }
                if (myLocation == null) {
                    return;
                }
                if (symbol == null) {
                    symbol = symbolManager.create(new SymbolOptions()
                            .withLatLng(myLocation)
                            .withIconImage(SOURCE_IMAGE_LOCATION)
                            .withDraggable(false));
                } else {
                    symbol.setLatLng(myLocation);
                    symbolManager.update(symbol);
                }

                if (isManual) {
                    moveMapCamera(myLocation);
                }
            }
        });
    }

    private void moveMapCamera(LatLng latLng) {
        if (mMapboxMap == null) {
            return;
        }
        CameraPosition position = new CameraPosition.Builder()
                .target(latLng)
                .zoom(15)
                .build();
        mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 2000);
        mMapboxMap.addOnMapClickListener(this);
    }

    // Get GPS real-time location
    public void setLocation(android.location.Location location) {
        setMyLocation(new LatLng(location.getLatitude(), location.getLongitude()), true);

        // Check nearby interesting point
        double minDistance = 100000;
        Location nearbyPoint = null;
        for (Location point : mLocations) {
            double distance = getDistanceMeters(point.latitude, point.longitude, location.getLatitude(), location.getLongitude());
            if (minDistance > distance) {
                minDistance = distance;
                nearbyPoint = point;
            }
        }

        Log.e(TAG, "Distance: " + minDistance + ", Point: " + (nearbyPoint == null ? 0 : nearbyPoint.number));

        // Check if it's in radius
        if (nearbyPoint != null && minDistance <= nearbyPoint.radius) {
            if (nearbyPoint.location_type != 0) {
                return;
            }
            setInterestingPoint(nearbyPoint, false); // From GPS
        }
    }

    private void setInterestingPoint(Location location, boolean repeated) {
        // If the POI is coming from location provider
        // We can skip to repeat
        if (!repeated && location.number == mLastPointNumber) {
            Log.e(TAG, "Repeated");
            return;
        }

        // If player is playing, in-app-purchase or downloading audios, skip it
        if ((mPlayer != null && mPlayer.isPlaying()) || activity.isLoading) {
            Log.e(TAG, "Playing, or loading");
            return;
        }

        mNewPointNumber = location.number;

        if (layoutLocation.getVisibility() == View.GONE) {
            layoutLocation.setVisibility(View.VISIBLE);
        }
        File image = DBManager.getInstance().getRealFile(activity, location.image);
        if (image != null && image.exists()) {
            imgLocation.setVisibility(View.VISIBLE);
            Glide.with(activity)
                    .load(image)
                    .into(imgLocation);
        } else {
            imgLocation.setVisibility(View.GONE);
        }

        txtLocationName.setText(location.name);
        LatLng latLng = new LatLng(location.latitude, location.longitude);
        moveMapCamera(latLng);

        showPlayerPanel();
        showPOIView(location);
        stopAudio();
        preparingAudio();
    }

    public void preparingAudio() {
        if (mNewPointNumber == 0) {
            return;
        }

        Audio audio = DBManager.getInstance().getAudio(mNewPointNumber);
        File file = activity.getAudioFile(audio);

        if (file == null) {
            return;
        }

        mAds.clear();
        if (audio.ads != null) {
            mAds.addAll(audio.ads);
        }
        try {
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(file.getAbsolutePath());
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    seekBar.setMax(mPlayer.getDuration());
                    txtEndTime.setText(TimeHelper.getTimeFromMilliseconds(mPlayer.getDuration()));

                    // Reset the last POI and flag
                    mLastPointNumber = mNewPointNumber;

                 //   activity.hideSnackBar();
                    playAudio();
                }
            });
            mPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playAudio() {
        anim.pause();

        mPlayer.start();
        timer.start();
        imgPlay.setImageResource(R.drawable.ic_pause);
    }

    public void pauseAudio() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        timer.cancel();
        imgPlay.setImageResource(R.drawable.ic_play);
    }

    private void stopAudio() {
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
        }
        timer.cancel();
        imgPlay.setImageResource(R.drawable.ic_play);
        seekBar.setMax(0);
        txtStartTime.setText(TimeHelper.getTimeFromMilliseconds(0));
        txtEndTime.setText(TimeHelper.getTimeFromMilliseconds(0));
    }

    private void refresh() {
        stopAudio();
        mLastPointNumber = 0;
    }

    // CountDownTimer
    CountDownTimer timer = new CountDownTimer(10000, 10) {

        public void onTick(long millisUntilFinished) {
            if (!mPlayer.isPlaying()) {
            //    activity.hideSnackBar();
                hidePOIView();
                imgPlay.setImageResource(R.drawable.ic_play);
                seekBar.setProgress(0);
                timer.cancel();
                txtStartTime.setText(TimeHelper.getTimeFromMilliseconds(0));
            }
            int currentPosition = mPlayer.getCurrentPosition();
            seekBar.setProgress(currentPosition);
            txtStartTime.setText(TimeHelper.getTimeFromMilliseconds(currentPosition));

            // Check AudioAds
            for (AudioAds ad : mAds) {
                if (ad.position == currentPosition / 1000) {
                 //   activity.showSnackBar(ad.url);
                    break;
                }
            }
        }

        public void onFinish() {
            timer.start();
        }

    };

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            if (mMapboxMap != null) {
                mMapboxMap.removeOnMapClickListener(this);
            }
            mapView.onDestroy();

            if (mPlayer != null && mPlayer.isPlaying()) {
                mPlayer.stop();
                mPlayer.release();
            }
            timer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private long getDistanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double l1 = Math.toRadians(lat1);
        double l2 = Math.toRadians(lat2);
        double g1 = Math.toRadians(lng1);
        double g2 = Math.toRadians(lng2);

        double dist = Math.acos(Math.sin(l1) * Math.sin(l2) + Math.cos(l1) * Math.cos(l2) * Math.cos(g1 - g2));
        if (dist < 0) {
            dist = dist + Math.PI;
        }

        return Math.round(dist * 6378100);
    }
}
