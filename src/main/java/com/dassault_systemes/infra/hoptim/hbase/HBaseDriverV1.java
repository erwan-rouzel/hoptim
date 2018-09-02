package com.dassault_systemes.infra.hoptim.hbase;

import org.apache.hadoop.hbase.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ERL1 on 6/7/2016.
 */
public class HBaseDriverV1 implements IHBaseDriver {
    public IHBaseDriver getInstance() throws IOException {
        return null;
    }

    public void moveRegion(String encodedRegionName, String destServerName) throws HBaseIOException, MasterNotRunningException, ZooKeeperConnectionException {

    }

    public boolean splitRegion(String regionName, byte[] splitKey) {
        return false;
    }

    public void splitTable(String tableName, byte[] splitKey) {

    }

    public String getRegionServerForRegion(HRegionInfo region) throws IOException {
        return null;
    }

    public ArrayList<RegionServerView> getRegionServers() throws IOException {
        return null;
    }

    public ArrayList<RegionView> getRegionsForRegionServer(ServerName serverName, ServerLoad serverLoad) {
        return null;
    }

    public HashMap<String, ArrayList<RegionView>> getRegionsDistributionPerRegionServer() throws IOException {
        return null;
    }

    public ArrayList<RegionView> getRegions() throws IOException {
        return null;
    }

    public ArrayList<TableView> getTables() throws IOException {
        return null;
    }

    public ArrayList<RegionView> getRegionsForTable(String tableName) throws IOException {
        return null;
    }

    public RegionView getRegion(String encodedName) throws IOException {
        return null;
    }

    public TableView getTable(String tableName) throws IOException {
        return null;
    }

    public int getMemStoreFlushSizeMB() {
        return 0;
    }

    public double getMemStoreUpperLimit() {
        return 0.0;
    }

    public String getZookeeperQuorum() {
        return null;
    }

    public boolean updateDump() { return false; }

    public long latestDumpTimestamp() { return 0; }

    public HashMap<String, ArrayList<RegionView>> getDumpedRegionsDistribution() throws IOException { return null; }
}
