package com.trailsi.malmo.android.controller.api;


import com.trailsi.malmo.android.common.Constant;
import com.trailsi.malmo.android.common.cache.CachedData;
import com.trailsi.malmo.android.common.db.DBManager;
import com.trailsi.malmo.android.common.utils.StringHelper;
import com.trailsi.malmo.android.common.utils.TimeHelper;
import com.trailsi.malmo.android.model.Audio;
import com.trailsi.malmo.android.model.AudioAds;
import com.trailsi.malmo.android.model.AudioType;
import com.trailsi.malmo.android.model.Document;
import com.trailsi.malmo.android.model.Language;
import com.trailsi.malmo.android.model.Location;
import com.trailsi.malmo.android.model.Place;
import com.trailsi.malmo.android.model.ResponseData;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static final String TAG = "ApiClient";

    public static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static ApiInterface apiService;

    public synchronized static ApiInterface getService() {
        if (apiService == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .cache(null)
                    .retryOnConnectionFailure(false)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS).build();

            Retrofit restAdapter = new Retrofit.Builder()
                    .baseUrl(Constant.ROOT_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiService = restAdapter.create(ApiInterface.class);
        }
        return apiService;
    }

    public static boolean downloadPlaceInfo() {
        Call<ResponseData> call = ApiClient.getService().getPlaceInfo(Constant.PLACE_UUID);
        Response<ResponseData> res = null;
        try {
            res = call.execute();
        } catch (Exception e) {
            call.cancel();
        }

        if (res == null || !res.isSuccessful()) {
            return false;
        }

        ResponseData responseData = res.body();
        if (responseData == null) {
            return false;
        }

        Place place = responseData.place;
        if (place == null) {
            return false;
        }

        CachedData.setString(CachedData.kPlaceMapImage, place.image);
        CachedData.setString(CachedData.kPlaceUUID, place.beacon_uuid);
        CachedData.setBoolean(CachedData.kPlaceUseGPS, place.use_gps);

        String topLeftLocation = place.top_left_location;
        if (!StringHelper.isEmpty(topLeftLocation)) {
            String[] split = topLeftLocation.replace(" ", "").split(",");
            if (split.length == 2 && StringHelper.isNumeric(split[0]) && StringHelper.isNumeric(split[1])) {
                float lat = Float.parseFloat(split[0]);
                float lng = Float.parseFloat(split[1]);
                CachedData.setDouble(CachedData.kPlaceTopLeftLatitude, lat);
                CachedData.setDouble(CachedData.kPlaceTopLeftLongitude, lng);
            }
        }

        String topRightLocation = place.top_right_location;
        if (!StringHelper.isEmpty(topRightLocation)) {
            String[] split = topRightLocation.replace(" ", "").split(",");
            if (split.length == 2 && StringHelper.isNumeric(split[0]) && StringHelper.isNumeric(split[1])) {
                float lat = Float.parseFloat(split[0]);
                float lng = Float.parseFloat(split[1]);
                CachedData.setDouble(CachedData.kPlaceTopRightLatitude, lat);
                CachedData.setDouble(CachedData.kPlaceTopRightLongitude, lng);
            }
        }

        String bottomRightLocation = place.bottom_right_location;
        if (!StringHelper.isEmpty(bottomRightLocation)) {
            String[] split = bottomRightLocation.replace(" ", "").split(",");
            if (split.length == 2 && StringHelper.isNumeric(split[0]) && StringHelper.isNumeric(split[1])) {
                float lat = Float.parseFloat(split[0]);
                float lng = Float.parseFloat(split[1]);
                CachedData.setDouble(CachedData.kPlaceBottomRightLatitude, lat);
                CachedData.setDouble(CachedData.kPlaceBottomRightLongitude, lng);
            }
        }

        String bottomLeftLocation = place.bottom_left_location;
        if (!StringHelper.isEmpty(bottomLeftLocation)) {
            String[] split = bottomLeftLocation.replace(" ", "").split(",");
            if (split.length == 2 && StringHelper.isNumeric(split[0]) && StringHelper.isNumeric(split[1])) {
                float lat = Float.parseFloat(split[0]);
                float lng = Float.parseFloat(split[1]);
                CachedData.setDouble(CachedData.kPlaceBottomLeftLatitude, lat);
                CachedData.setDouble(CachedData.kPlaceBottomLeftLongitude, lng);
            }
        }


        DBManager.getInstance().clearLocations();
        DBManager.getInstance().clearAudio();
        DBManager.getInstance().clearDocument();
        DBManager.getInstance().clearLanguage();
        DBManager.getInstance().clearAudioType();
        DBManager.getInstance().clearAudioAds();

        // Add Locations
        for (Location location : responseData.locations) {
            DBManager.getInstance().addLocation(location);

            // Add Audios
            for (Audio audio : location.audios) {
                long id = DBManager.getInstance().addAudio(audio);
                List<AudioAds> ads = audio.ads;
                if (ads != null) {
                    for (AudioAds ad : ads) {
                        ad.audio_id = id;
                        ad.position = TimeHelper.getTimeFromString(ad.audio_position);
                        DBManager.getInstance().addAudioAds(ad);
                    }
                }
            }
        }

        // Add Documents
        for (Document document : responseData.documents) {
            DBManager.getInstance().addDocument(document);
        }

        // Add Languages
        for (Language language : responseData.languages) {
            DBManager.getInstance().addLanguage(language);
        }

        // Add AudioTypes
        for (AudioType type : responseData.audio_types) {
            DBManager.getInstance().addAudioType(type);
        }

        // Add PlaceInfo
        if (responseData.info != null) {
            CachedData.setString(CachedData.kPlaceInfoTitle, responseData.info.title);
            CachedData.setString(CachedData.kPlaceInfoDescription, responseData.info.description);
            CachedData.setString(CachedData.kPlaceInfoImage, responseData.info.image);
        }

        CachedData.setBoolean(CachedData.kPlaceInfoDownloaded, true);
        return true;
    }
}
