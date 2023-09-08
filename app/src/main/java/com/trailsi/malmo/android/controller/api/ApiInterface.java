package com.trailsi.malmo.android.controller.api;

import com.trailsi.malmo.android.model.ResponseData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiInterface {

    @GET("/api/v1/base/place/user/{uuid}")
    Call<ResponseData> getPlaceInfo(
            @Path("uuid") String uuid
    );
}