package com.dassault_systemes.infra.hoptim.hbase;

import java.util.*;

/**
 * Created by ERL1 on 7/4/2016.
 */
public class SortUtils {
    public static LinkedHashMap<String, ArrayList<RegionView>> sortDescRegionServersBySize(HashMap<String, ArrayList<RegionView>> regionServers) {
        LinkedHashMap<String, ArrayList<RegionView>> sortedRS = new LinkedHashMap<String, ArrayList<RegionView>>();
        TreeMap<Integer, String> sizes = new TreeMap<Integer, String>(Collections.<Integer>reverseOrder());

        for(Map.Entry<String, ArrayList<RegionView>> entry: regionServers.entrySet()) {
            String rsName = entry.getKey();
            ArrayList<RegionView> regions = entry.getValue();

            int sizeForRS = 0;
            for (RegionView region : regions) {
                sizeForRS += region.sizeMB;
            }

            int i = 0;
            while (sizes.containsKey(1000*sizeForRS + i)) i++;
            sizes.put(1000*sizeForRS + i, rsName);
        }

        for (Map.Entry<Integer, String> rs : sizes.entrySet()) {
            String rsName = rs.getValue();
            ArrayList<RegionView> rsRegions = regionServers.get(rsName);
            ArrayList<RegionView> sortedRegions = sortDescRegionsBySize(rsRegions);

            sortedRS.put(rsName, sortedRegions);
        }

        return sortedRS;
    }

    public static ArrayList<RegionView> sortDescRegionsBySize(ArrayList<RegionView> regions) {
        ArrayList<RegionView> sortedRegions = new ArrayList<RegionView>();
        TreeMap<Integer, RegionView> sizes = new TreeMap<Integer, RegionView>(Collections.<Integer>reverseOrder());

        for(RegionView region: regions) {
            int i = 0;
            while(sizes.containsKey(1000*region.sizeMB + i)) i++;
            sizes.put(1000*region.sizeMB + i, region);
        }

        for(Map.Entry<Integer, RegionView> entry: sizes.entrySet()) {
            RegionView region = entry.getValue();
            sortedRegions.add(region);
        }

        return sortedRegions;
    }
}
