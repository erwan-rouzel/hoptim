package com.dassault_systemes.infra.hoptim.hbase;

import org.apache.hadoop.hbase.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ERL1 on 6/7/2016.
 */
public interface IHBaseDriver {
    void moveRegion(String encodedRegionName, String destServerName) throws HBaseIOException, MasterNotRunningException, ZooKeeperConnectionException;
    boolean splitRegion(String encodedRegionName,byte[] splitKey) throws IOException, InterruptedException;
    void splitTable(String tableName, byte[] splitKey) throws IOException, InterruptedException;
    String getRegionServerForRegion(HRegionInfo region) throws IOException;
    ArrayList<RegionServerView> getRegionServers() throws IOException;
    ArrayList<RegionView> getRegionsForRegionServer(ServerName serverName, ServerLoad serverLoad);
    HashMap<String, ArrayList<RegionView>> getRegionsDistributionPerRegionServer() throws IOException;
    ArrayList<RegionView> getRegions() throws IOException;
    ArrayList<TableView> getTables() throws IOException;
    ArrayList<RegionView> getRegionsForTable(String tableName) throws IOException;
    RegionView getRegion(String encodedName) throws IOException;
    TableView getTable(String tableName) throws IOException;
    int getMemStoreFlushSizeMB();
    double getMemStoreUpperLimit();
    String getZookeeperQuorum();
    boolean updateDump();
    long latestDumpTimestamp();
    HashMap<String, ArrayList<RegionView>> getDumpedRegionsDistribution() throws IOException;
}
