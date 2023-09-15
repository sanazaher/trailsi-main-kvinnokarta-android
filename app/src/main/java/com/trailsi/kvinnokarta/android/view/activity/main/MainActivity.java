package com.trailsi.kvinnokarta.android.view.activity.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.billingclient.api.BillingClient;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.trailsi.kvinnokarta.android.App;
import com.trailsi.kvinnokarta.android.BuildConfig;
import com.trailsi.kvinnokarta.android.R;
import com.trailsi.kvinnokarta.android.common.Constant;
import com.trailsi.kvinnokarta.android.common.cache.CachedData;
import com.trailsi.kvinnokarta.android.common.db.DBManager;
import com.trailsi.kvinnokarta.android.common.utils.DeviceUtil;
import com.trailsi.kvinnokarta.android.common.utils.DialogUtil;
import com.trailsi.kvinnokarta.android.common.utils.StringHelper;
import com.trailsi.kvinnokarta.android.controller.service.SyncService;
import com.trailsi.kvinnokarta.android.model.Audio;
import com.trailsi.kvinnokarta.android.model.LocalFile;
import com.trailsi.kvinnokarta.android.view.activity.aboutus.AboutUsActivity;
import com.trailsi.kvinnokarta.android.view.activity.base.BaseActivity;
import com.trailsi.kvinnokarta.android.view.fragment.DownloadsFragment;
import com.trailsi.kvinnokarta.android.view.fragment.MapsFragment;
import com.trailsi.kvinnokarta.android.view.fragment.PlacesFragment;
import com.trailsi.kvinnokarta.android.view.fragment.SettingsFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends BaseActivity implements LocationListener {

    public static final String TAG = "MainActivity";

    public enum ACTIVE_VIEW {MAP, PLACES, DOWNLOADS, SETTING}

    public BottomNavigationView navigation;
    public Fragment activeFragment;

    public boolean isLoading; // Loading purchases, downloading files
    private int startPosition; // Fragment position for transition

    private SyncReceiver syncReceiver;
    private BillingClient billingClient;
    private AlertDialog adsDialog;
    private InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        adMobInitialize();
        initVariable();
        initUI();
        syncReceiver = new SyncReceiver();
        registerReceiver(syncReceiver, new IntentFilter(Constant.ACTION_FILE_DOWNLOAD_STATUS));

        replaceFragment(ACTIVE_VIEW.MAP);
    }
    private void adMobInitialize(){
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this,"ca-app-pub-3497718794369186/3484632016", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The interstitialAd reference will be null until
                        // an ad is loaded.
                        interstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        interstitialAd = null;
                    }
                });

    }


    private void initVariable() {
        startPosition = 0;
        isLoading = false;
    }

    private void initUI() {
        setToolBar(getString(R.string.map), R.drawable.ic_map, false);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private ACTIVE_VIEW getActiveFragment() {
        if (activeFragment instanceof MapsFragment) return ACTIVE_VIEW.MAP;
        if (activeFragment instanceof PlacesFragment) return ACTIVE_VIEW.PLACES;
        if (activeFragment instanceof DownloadsFragment) return ACTIVE_VIEW.DOWNLOADS;
        if (activeFragment instanceof SettingsFragment) return ACTIVE_VIEW.SETTING;
        return null;
    }

    public void replaceFragment(final ACTIVE_VIEW nActive) {
        if (getActiveFragment() == nActive)
            return;

        new Handler().post(() -> {
            int newPosition = 0;
            switch (nActive) {
                case MAP:
                    newPosition = 0;
                    activeFragment = new MapsFragment(MainActivity.this);
                    break;

                case PLACES:
                    newPosition = 1;
                    activeFragment = new PlacesFragment(MainActivity.this);
                    break;

                case DOWNLOADS:
                    newPosition = 2;
                    activeFragment = new DownloadsFragment(MainActivity.this);
                    if (interstitialAd != null) {
                        interstitialAd.show(MainActivity.this);
                    }
                    else {
                        Log.d("TAG", "The interstitial ad wasn't ready yet.");
                    }
                    break;

                case SETTING:
                    newPosition = 3;
                    activeFragment = new SettingsFragment(MainActivity.this);
                    break;

                default:
                    break;
            }

            loadFragment(newPosition);
        });
    }

    private void loadFragment(int newPosition) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (newPosition == startPosition) {
            transaction.replace(R.id.llFragmentContainer, activeFragment);
        } else if (newPosition > startPosition) {
            transaction
                    .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left)
                    .replace(R.id.llFragmentContainer, activeFragment);
        } else {
            transaction
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
                    .replace(R.id.llFragmentContainer, activeFragment);
        }
        transaction.commitAllowingStateLoss();
        showActionBar();
        startPosition = newPosition;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        int itemId = item.getItemId();
        if (itemId == R.id.action_map) {
            replaceFragment(ACTIVE_VIEW.MAP);
            return true;
        } else if (itemId == R.id.action_places) {
            replaceFragment(ACTIVE_VIEW.PLACES);
            return true;
        } else if (itemId == R.id.action_downloads) {
            replaceFragment(ACTIVE_VIEW.DOWNLOADS);
            return true;
        } else if (itemId == R.id.action_settings) {
            replaceFragment(ACTIVE_VIEW.SETTING);
            return true;
        }
        return false;
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void showActionBar() {
        String title = getString(R.string.map);
        int iconId = R.drawable.ic_map;
        if (getActiveFragment() != null) {
            switch (getActiveFragment()) {
                case MAP:
                    title = getString(R.string.map);
                    iconId = R.drawable.ic_map;
                    break;

                case PLACES:
                    title = getString(R.string.places);
                    iconId = R.drawable.ic_place;
                    break;

                case DOWNLOADS:
                    title = getString(R.string.downloads);
                    iconId = R.drawable.ic_download;
                    break;

                case SETTING:
                    title = getString(R.string.settings);
                    iconId = R.drawable.ic_setting;
                    break;
            }
        }

        setToolBar(title, iconId, false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            gotoAboutUs();
        }

        return super.onOptionsItemSelected(item);
    }

    private void gotoAboutUs() {
        Intent intent = new Intent(this, AboutUsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public void startSyncService(String type) {
        showDownloadProgressAlert(0);

        App.startService(type);
    }

    private class SyncReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;

            int cmd = intent.getIntExtra("command", -1);
            int percent = intent.getIntExtra("percent", 0);
            String error = intent.getStringExtra("error");
            Log.e(TAG, "CMD: " + cmd + ", Percent: " + percent + ", Error: " + error);
            switch (cmd) {
                case SyncService.STATUS_START:
                case SyncService.STATUS_DOWNLOAD:
                    showDownloadProgressAlert(percent);
                    break;

                case SyncService.STATUS_ERROR:
                    if (!StringHelper.isEmpty(error)) {
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                    }
                    break;

                case SyncService.STATUS_FINISH_FILE:
                    fileDownloadFinished();
                    break;

                case SyncService.STATUS_FINISH_AUDIO:
                    audioDownloadFinished();
                    break;
            }
        }
    }

    private void fileDownloadFinished() {
        isLoading = false;
        hideProgressDialog();
        if (activeFragment instanceof MapsFragment) {
            ((MapsFragment) activeFragment).loadLocations();
        }
        if (activeFragment instanceof PlacesFragment) {
            ((PlacesFragment) activeFragment).loadLocations();
        }
        startLocationService();
    }

    private void audioDownloadFinished() {
        isLoading = false;
        hideProgressDialog();
        if (activeFragment instanceof MapsFragment) {
            ((MapsFragment) activeFragment).preparingAudio();
        }
        if (activeFragment instanceof PlacesFragment) {
            ((PlacesFragment) activeFragment).preparingAudio();
        }
    }

    public void startLocationService() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (lm == null) {
            return;
        }
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            DialogUtil.ShowAlert(this, getString(R.string.location_disabled), getString(R.string.location_disabled_msg),
                    object -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
            return;
        }
        if (!BuildConfig.DEBUG) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 10, this); // TODO on Emulator
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (activeFragment instanceof MapsFragment) {
                ((MapsFragment) activeFragment).setLocation(location);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(syncReceiver);
        syncReceiver = null;
    }

    public File getAudioFile(Audio audio) {
        if (audio == null) {
            Toast.makeText(this, R.string.no_audio_found, Toast.LENGTH_SHORT).show();
            return null;
        }

        if (StringHelper.isEmpty(audio.audio)) {
            Toast.makeText(this, R.string.no_audio_found, Toast.LENGTH_SHORT).show();
            return null;
        }
        File file = DBManager.getInstance().getRealFile(this, audio.audio);
        if (file != null && file.exists()) {
            return file;
        }

        isLoading = true;
        startSyncService(Constant.DOWNLOAD_AUDIO);
        return null;
    }

    /*    String selectedType = CachedData.getString(CachedData.kAudioType, "");
        PurchaseResult result = DBManager.getInstance().getPurchaseResultByTour(selectedType);
        if (result != null && result.purchased && !StringHelper.isEmpty(result.purchase_token)) {

        }

        // Ask Free Preview with count
        int usedCount = CachedData.getCountForCurrentType();
        int remainCount = 2 - usedCount;
        if (remainCount > 0) {
            DialogUtil.ShowAskPurchase(this, remainCount, new Notify() {
                @Override
                public void onAccept(Object object) {
                    boolean isPurchase = (boolean) object;
                    checkInAppPurchase(isPurchase);
                }

                @Override
                public void onCancel(Object object) {
                    downloadOneAudio(audio.audio);
                }
            });
            return null;
        }

        DialogUtil.ShowNoFreePurchase(this, new Notify() {
            @Override
            public void onAccept(Object object) {
                boolean isPurchase = (boolean) object;
                checkInAppPurchase(isPurchase);
            }

            @Override
            public void onCancel(Object object) {
                isLoading = false;
            }
        });
        return null;
    }

     */

    private void downloadOneAudio(String audio) {
        showProgressDialog();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e(TAG, "Downloading: " + audio);
                    URL url = new URL(audio);
                    URLConnection c = url.openConnection();

                    String localFileName = DeviceUtil.getUUID();
                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), localFileName);
                    file.mkdirs();
                    if (file.exists()) {
                        file.delete();
                    }
                    file.createNewFile();

                    InputStream is = c.getInputStream();
                    FileOutputStream fos = new FileOutputStream(file);

                    byte[] buffer = new byte[1024];
                    int count;

                    while ((count = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, count);
                    }
                    fos.flush();
                    fos.close();
                    is.close();

                    LocalFile localFile = new LocalFile(audio, localFileName);
                    DBManager.getInstance().addLocalFile(localFile);

                    // Update counts
                    CachedData.increaseCountForType();
                } catch (IOException fnfe) {
                    fnfe.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        audioDownloadFinished();
                    }
                });
            }
        }).start();
    }
}
/*
    public void checkInAppPurchase(boolean isPurchase) {
        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.e(TAG, "onBillingSetupFinished:" + billingResult.getDebugMessage());
                    if (isPurchase) {
                        queryAvailableProducts();
                    } else {
                        fetchingPurchases();
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.e(TAG, "onBillingServiceDisconnected");
                isLoading = false;
                Toast.makeText(MainActivity.this, "Something went wrong to connect Google Play In-App purchase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchingPurchases() {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {
                purchaseUpdated(billingResult, list);
            }
        });
    }

    private void queryAvailableProducts() {
        List<String> skuList = new ArrayList<>();
        List<AudioType> audioTypes = new ArrayList<>();
        DBManager.getInstance().getAudioTypes(audioTypes);
        for (AudioType type : audioTypes) {
            if (!StringHelper.isEmpty(type.android_product_id)) {
                skuList.add(type.android_product_id);
            }
        }
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP);
        billingClient.querySkuDetailsAsync(params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(@NonNull BillingResult billingResult,
                                                     List<SkuDetails> skuDetailsList) {
                        isLoading = false;
                        if (skuDetailsList.size() > 0) {
                            AudioType audioType = DBManager.getInstance().getCurrentAudioType();
                            for (SkuDetails skuDetails : skuDetailsList) {
                                if (skuDetails.getSku().equals(audioType.android_product_id)) {
                                    purchaseFlow(skuDetails);
                                }
                            }
                        }
                    }
                });
    }

    private void purchaseFlow(SkuDetails skuDetails) {
        isLoading = true;

        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        int responseCode = billingClient.launchBillingFlow(this, billingFlowParams).getResponseCode();
        Log.e(TAG, "Purchase flow response code: " + responseCode);
    }

    private final PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
            purchaseUpdated(billingResult, list);
        }
    };

    private void purchaseUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        isLoading = false;
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (list != null && list.size() > 0) {
                for (Purchase purchase : list) {
                    handlePurchase(purchase);
                }
            } else {
                showMessage("There is no purchase. Please try again later.");
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            showMessage("You canceled the purchase.");
        } else {
            showMessage("Something went wrong to handle purchase. Please contact to support.");
        }
    }

    private void showMessage(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handlePurchase(Purchase purchase) {
        Log.e(TAG, purchase.toString());
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            PurchaseResult result = new PurchaseResult();
            result.type = CachedData.getString(CachedData.kAudioType, "");
            result.purchased = true;
            result.purchase_token = purchase.getPurchaseToken();
            DBManager.getInstance().addPurchaseResult(result);

            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, new AcknowledgePurchaseResponseListener() {
                    @Override
                    public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
                        Log.e(TAG, "AcknowledgePurchaseResponseListener: " + billingResult.getResponseCode());
                    }
                });
            }

            DialogUtil.ShowAlert(
                    MainActivity.this,
                    getString(R.string.app_name),
                    "Purchase succeeded. Audios for current tour will be downloaded automatically.",
                    new NotifyAccept() {
                        @Override
                        public void onAccept(Object object) {
                            startSyncService(Constant.DOWNLOAD_AUDIO);
                        }
                    }
            );
        }
    }

    public void showSnackBar(String url) {
        if (this.adsDialog != null) {
            return;
        }
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dlg_snack, null);
        adsDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_Dialog))
                .setView(promptsView)
                .setCancelable(false)
                .create();

        if (adsDialog.getWindow() != null) {
            adsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        AppCompatButton btnYes = promptsView.findViewById(R.id.btn_yes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        AppCompatButton btnNo = promptsView.findViewById(R.id.btn_no);
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeFragment instanceof MapsFragment) {
                    ((MapsFragment) activeFragment).playAudio();
                }
                if (activeFragment instanceof PlacesFragment) {
                    ((PlacesFragment) activeFragment).playAudio();
                }
                hideSnackBar();
            }
        });
        adsDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activeFragment instanceof MapsFragment) {
                    ((MapsFragment) activeFragment).pauseAudio();
                }
                if (activeFragment instanceof PlacesFragment) {
                    ((PlacesFragment) activeFragment).pauseAudio();
                }
            }
        }, 1000);
    }

    public void hideSnackBar() {
        if (this.adsDialog != null) {
            this.adsDialog.dismiss();
            this.adsDialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.adsDialog != null) {
            hideSnackBar();
            if (activeFragment instanceof MapsFragment) {
                ((MapsFragment) activeFragment).playAudio();
            }
            if (activeFragment instanceof PlacesFragment) {
                ((PlacesFragment) activeFragment).playAudio();
            }
        }
    }



 */