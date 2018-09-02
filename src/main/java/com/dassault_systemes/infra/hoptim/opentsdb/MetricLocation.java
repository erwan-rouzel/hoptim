package com.dassault_systemes.infra.hoptim.opentsdb;

import java.util.ArrayList;

/**
 * Represents the locations of metric (which can be spanned over several region servers).
 *
 * Created by ERL1 on 4/29/2016.
 */
public class MetricLocation {
    public ArrayList<String> regionServers;

    public MetricLocation() {
        regionServers = new ArrayList<String>();
    }
}
