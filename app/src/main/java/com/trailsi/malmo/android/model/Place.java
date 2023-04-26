package com.trailsi.malmo.android.model;

import java.io.Serializable;
import java.util.List;

public class Place implements Serializable {
    public int id;
    public String name;
    public String description;
    public String url;
    public String uuid;
    public boolean use_gps;
    public String image;
    public String top_left_location;
    public String top_right_location;
    public String bottom_left_location;
    public String bottom_right_location;
    public String beacon_uuid;
}
