package com.trailsi.kvinnokarta.android.model;

import java.io.Serializable;
import java.util.List;

public class ResponseData implements Serializable {
    public Place place;
    public List<Location> locations;
    public List<Document> documents;
    public List<AudioType> audio_types;
    public List<Language> languages;
    public PlaceInfo info;
}
