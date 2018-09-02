package com.dassault_systemes.infra.hoptim.opentsdb;

import java.util.ArrayList;

/**
 * Represents a metric.
 *
 * Created by ERL1 on 4/26/2016.
 */
public class Metric {
    public int id;
    public String name;
    public ArrayList<String> regionServers;
    public ArrayList<OpenTSDBTagVal> tags;

    public Metric() {
        regionServers = new ArrayList<String>();
        tags = new ArrayList<OpenTSDBTagVal>();
    }

    public byte[] getMetricIdAsBytes() {
        return new byte[]{(byte)(id >> 16), (byte)(id >> 8), (byte)(id >> 0)};
    }
}
