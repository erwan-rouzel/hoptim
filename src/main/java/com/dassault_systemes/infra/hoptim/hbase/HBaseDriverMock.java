package com.dassault_systemes.infra.hoptim.hbase;

import com.dassault_systemes.infra.hoptim.log.CustomLogger;
import com.dassault_systemes.infra.hoptim.opentsdb.OpenTSDBKey;
import org.apache.hadoop.hbase.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by ERL1 on 6/21/2016.
 */
public class HBaseDriverMock implements IHBaseDriver {
    private final static int NB_REGION_SERVERS = 16;
    private final static int REGION_SERVER_SIZE = 1000;
    private final static int NB_REGION_MAX_PER_RS = 10;
    private final static String[] tableNames = {
            "tsdb", "tsdb-uid", "tsdb-meta", "tsdb-tree",
            "tsdb1", "tsdb-uid1", "tsdb-meta1", "tsdb-tree1"
    };
    private static int numSplits;
    private static int totalRegionsCounter;
    private static HBaseDriverMock instance;
    private Random random;
    private ArrayList<RegionServerView> regionServerViews;
    private HBaseDump hBaseDump;

    private RegionServerView generateRegionServerView(int id) {
        RegionServerView rs = new RegionServerView();
        rs.name = "RS_" + id;
        rs.diskSpaceCapacityGB = REGION_SERVER_SIZE;
        rs.id = id;

        return rs;
    }

    private RegionView generateRegionView(int id) {
        long delta;
        RegionView rg = new RegionView();
        rg.regionName = "Region_" + id;
        rg.sizeMB = random.nextInt(REGION_SERVER_SIZE/2) + 1;
        rg.writeRequestsCount = random.nextInt(10000) + 1;
        rg.readRequestsCount = random.nextInt(10000) + 1;
        rg.tableName = tableNames[random.nextInt(tableNames.length)];
        rg.encodedName = "abcdef" + id;
        rg.id = id;

        return rg;
    }

    /**
     * Private constructor. This just prevent from instanciating this class without going through the getInstance method.
     *
     * @throws IOException
     */
    private HBaseDriverMock() throws IOException {
        random = new Random();
        totalRegionsCounter = 0;
        numSplits = 0;
        regionServerViews = new ArrayList<RegionServerView>();
        hBaseDump = new HBaseDump();

        for(int i = 0; i < NB_REGION_SERVERS; i++) {
            RegionServerView rs = generateRegionServerView(i);

            int numRegions = random.nextInt(NB_REGION_MAX_PER_RS) + 1;

            for(int j = 0; j < numRegions; j++) {
                if(rs.getLoad() < 0.75*REGION_SERVER_SIZE) {
                    CustomLogger.debug(this, "totalRegionsCounter=" + totalRegionsCounter);
                    RegionView rg = generateRegionView(totalRegionsCounter);
                    if(! rg.tableName.startsWith("hbase:")) {
                        rg.currentRegionServer = rs.name;
                        rs.regions.add(rg);
                        totalRegionsCounter++;
                    }
                }
            }

            regionServerViews.add(rs);
        }
    }

    /**
     * Gets an instance of this class if it exists, otherwise create this first instance and then return it.
     * (following the Singleton Design Pattern).
     *
     * @return The single instance of this class
     * @throws IOException
     */
    public static HBaseDriverMock getInstance() throws IOException {
        if(instance == null) {
            instance = new HBaseDriverMock();
        }

        return instance;
    }

    public void moveRegion(String encodedRegionName, String destServerName) throws HBaseIOException, MasterNotRunningException, ZooKeeperConnectionException {
        for(int i = 0; i < regionServerViews.size(); i++) {
            for(int j = 0; j < regionServerViews.get(i).regions.size(); j++) {
                if(regionServerViews.get(i).regions.get(j).encodedName.equals(encodedRegionName)) {
                    for(int k = 0; k < regionServerViews.size(); k++) {
                        if(regionServerViews.get(k).name.equals(destServerName)) {
                            CustomLogger.debug(this, "MOVED region " + encodedRegionName + " to " + destServerName);
                            regionServerViews.get(k).regions.add(regionServerViews.get(i).regions.get(j));
                            regionServerViews.get(i).regions.get(j).currentRegionServer = regionServerViews.get(k).name;
                            regionServerViews.get(i).regions.remove(j);
                        }
                    }
                }
            }
        }
    }

    public boolean splitRegion(String encodedRegionName, byte[] splitKey) throws IOException, InterruptedException {
        HashMap<Integer, RegionView> newRegionsPerRS = new HashMap<Integer, RegionView>();

        for(int i = 0;  i < regionServerViews.size(); i++) {
            RegionServerView rsSrc = regionServerViews.get(i);

            for (RegionView rg : rsSrc.regions) {
                if(rg.containsKey(splitKey)) {
                    RegionView rgSplit = new RegionView();
                    rgSplit.sizeMB = rg.sizeMB - rg.sizeMB/2;
                    rgSplit.encodedName = rg.encodedName + "_split_" + numSplits;
                    rgSplit.tableName = rg.tableName;
                    rgSplit.currentRegionServer = rg.currentRegionServer;
                    rgSplit.id = totalRegionsCounter;
                    rgSplit.regionName = rg.regionName + "_split_" + numSplits;
                    rgSplit.startKey = new OpenTSDBKey(splitKey);
                    rgSplit.endKey = rg.endKey;

                    newRegionsPerRS.put(i, rgSplit);

                    rg.endKey = new OpenTSDBKey(splitKey);
                    rg.sizeMB = rg.sizeMB/2;
                    numSplits++;
                }
            }
        }

        for(Map.Entry<Integer, RegionView> entry : newRegionsPerRS.entrySet()) {
            Integer key = entry.getKey();
            RegionView value = entry.getValue();

            regionServerViews.get(key).regions.add(value);
        }

        return true;
    }

    public void splitTable(String tableName, byte[] splitKey) throws IOException, InterruptedException {
        HashMap<Integer, RegionView> newRegionsPerRS = new HashMap<Integer, RegionView>();

        for(int i = 0;  i < regionServerViews.size(); i++) {
            RegionServerView rsSrc = regionServerViews.get(i);

            for (RegionView rg : rsSrc.regions) {
                if(rg.containsKey(splitKey)) {
                    RegionView rgSplit = new RegionView();
                    rgSplit.sizeMB = rg.sizeMB - rg.sizeMB/2;
                    rgSplit.encodedName = rg.encodedName + "_split_" + numSplits;
                    rgSplit.tableName = rg.tableName;
                    rgSplit.currentRegionServer = rg.currentRegionServer;
                    rgSplit.id = totalRegionsCounter;
                    rgSplit.regionName = rg.regionName + "_split_" + numSplits;
                    rgSplit.startKey = new OpenTSDBKey(splitKey);
                    rgSplit.endKey = rg.endKey;

                    newRegionsPerRS.put(i, rgSplit);

                    rg.endKey = new OpenTSDBKey(splitKey);
                    rg.sizeMB = rg.sizeMB/2;
                    numSplits++;
                }
            }
        }

        for(Map.Entry<Integer, RegionView> entry : newRegionsPerRS.entrySet()) {
            Integer key = entry.getKey();
            RegionView value = entry.getValue();

            regionServerViews.get(key).regions.add(value);
        }
    }

    public String getRegionServerForRegion(HRegionInfo region) throws IOException {
        for(RegionServerView rs: regionServerViews) {
            for(RegionView rg: rs.regions) {
                if(rg.encodedName.equals(region.getEncodedName())) {
                    return rs.name;
                }
            }
        }

        return null;
    }

    public ArrayList<RegionServerView> getRegionServers() throws IOException {
        return regionServerViews;
    }

    public ArrayList<RegionView> getRegionsForRegionServer(ServerName serverName, ServerLoad serverLoad) {
        for(RegionServerView rs: regionServerViews) {
            if(rs.name.equals(serverName)) {
                return rs.regions;
            }
        }
        return null;
    }

    public HashMap<String, ArrayList<RegionView>> getRegionsDistributionPerRegionServer() throws IOException {
        HashMap<String, ArrayList<RegionView>> regionDistrib = new HashMap<String, ArrayList<RegionView>>();
        int delta_rs = 0;
        long delta_rg = 0;

        for(RegionServerView rs: regionServerViews) {

            delta_rs = rs.writeRequestsCount - hBaseDump.getDumpedWriteRequestsCountForRegionServer(rs.name);
            rs.writeRequestsDelta = (delta_rs > 0)?delta_rs:0;

            delta_rs = rs.readRequestsCount - hBaseDump.getDumpedReadRequestsCountForRegionServer(rs.name);
            rs.readRequestsDelta = (delta_rs > 0)?delta_rs:0;

            for(RegionView rg: rs.regions) {
                delta_rg = rg.writeRequestsCount - hBaseDump.getDumpedWriteRequestsCountForRegion(rg.encodedName);
                rg.writeRequestsDelta = (delta_rg > 0)?delta_rg:0;

                delta_rg = rg.readRequestsCount - hBaseDump.getDumpedReadRequestsCountForRegion(rg.encodedName);
                rg.readRequestsDelta = (delta_rg > 0)?delta_rg:0;
            }

            regionDistrib.put(rs.name, rs.regions);
        }
        return regionDistrib;
    }

    public ArrayList<RegionView> getRegions() throws IOException {
        ArrayList<RegionView> regions = new ArrayList<RegionView>();

        for(RegionServerView rs: regionServerViews) {
            regions.addAll(rs.regions);
        }

        return regions;
    }

    public ArrayList<TableView> getTables() throws IOException {
        ArrayList<RegionView> regions = getRegions();
        ArrayList<TableView> tables = new ArrayList<TableView>();
        ArrayList<String> tableAdded = new ArrayList<String>();
        HashMap<String, Integer> tableIndexMap = new HashMap<String, Integer>();
        int index = 0;

        for(RegionView rg: regions) {
            if(! rg.tableName.startsWith("hbase:")) {
                if (!tableAdded.contains(rg.tableName)) {
                    TableView tableView = new TableView();
                    tableView.tableName = rg.tableName;
                    tableView.tableRegions.add(rg);
                    tables.add(tableView);
                    tableAdded.add(rg.tableName);
                    tableIndexMap.put(rg.tableName, index++);
                } else {
                    tables.get(tableIndexMap.get(rg.tableName)).tableRegions.add(rg);
                }
            }
        }

        return tables;
    }

    public ArrayList<RegionView> getRegionsForTable(String tableName) throws IOException {
        ArrayList<TableView> tables = getTables();

        for(TableView table: tables) {
            if(table.tableName.equals(tableName)) {
                return table.tableRegions;
            }
        }

        return new ArrayList<RegionView>();
    }

    public RegionView getRegion(String encodedName) throws IOException {
        ArrayList<RegionView> regionViews = getRegions();

        for(RegionView regionView: regionViews) {
            if(regionView.encodedName.equals(encodedName)) {
                return regionView;
            }
        }
        return null;
    }

    public TableView getTable(String tableName) throws IOException {
        ArrayList<TableView> tableViews = getTables();

        for(TableView tableView: tableViews) {
            if(tableView.tableName.equals(tableName)) {
                return tableView;
            }
        }
        return null;
    }

    public int getMemStoreFlushSizeMB() {
        return 128;
    }

    public double getMemStoreUpperLimit() {
        return 0.4;
    }

    public String getZookeeperQuorum() {
        String quorum = "";
        for(RegionServerView rs: regionServerViews) {
            quorum += rs.name + ",";
        }
        return quorum.substring(0, quorum.length() - 1);
    }

    public boolean updateDump() {
        return hBaseDump.updateDump();
    }

    public long latestDumpTimestamp() {
        return hBaseDump.latestDumpTimestamp();
    }

    public HashMap<String, ArrayList<RegionView>> getDumpedRegionsDistribution() throws IOException {
        return hBaseDump.getDumpedRegionsDistribution();
    }
}
