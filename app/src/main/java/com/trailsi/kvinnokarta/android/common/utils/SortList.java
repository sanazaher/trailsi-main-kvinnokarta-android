package com.trailsi.kvinnokarta.android.common.utils;

import com.trailsi.kvinnokarta.android.model.Location;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortList {

    public static void sortLocations(List<Location> objects) {
        Collections.sort(objects, new LocationComparator());
    }

    public static class LocationComparator implements Comparator<Location> {
        public int compare(Location a, Location b) {
            return a.number - b.number;
        }
    }
}
