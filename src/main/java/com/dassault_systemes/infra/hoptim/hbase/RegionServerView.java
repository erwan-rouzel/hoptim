package com.dassault_systemes.infra.hoptim.hbase;

import java.util.ArrayList;

/**
 * This encapsulate the important informations about a region server. This is an abstraction over the HBase API layer
 * to get only the informations in which we are interested.
 *
 * Created by ERL1 on 4/27/2016.
 */
public class RegionServerView {
    public ArrayList<RegionView> regions;
    public String name;
    public int diskSpaceCapacityGB;
    public int writeRequestsCount;
    public int readRequestsCount;
    public int writeRequestsDelta;
    public int readRequestsDelta;
    public long id;

    public RegionServerView(){
        regions = new ArrayList<RegionView>();
        diskSpaceCapacityGB = 500;
        writeRequestsCount = 0;
        readRequestsCount = 0;
    }

    public int getLoad() {
        int load = 0;
        for(RegionView rg: regions) {
            load += rg.sizeMB;
        }

        return load;
    }
}
