package com.dassault_systemes.infra.hoptim.hbase;

import com.dassault_systemes.infra.hoptim.log.CustomLogger;
import com.dassault_systemes.infra.hoptim.settings.SettingsFacade;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ERL1 on 6/7/2016.
 */
public class HBaseDriverV0 implements IHBaseDriver {
    private static HBaseDriverV0 instance;
    private HTable metaTable;
    private Configuration conf;
    private HBaseAdmin hbaseAdmin;
    private HBaseDump hBaseDump;

    /**
     * Private constructor. This just prevent from instanciating this class without going through the getInstance method.
     *
     * @throws IOException
     */
    private HBaseDriverV0() throws IOException {
        CustomLogger.debug(this, "Init HBaseFacade: START");
        this.conf = HBaseConfiguration.create();
        if(! SettingsFacade.getInstance().hbase.getZookeeperQuorum().equals("auto")) {
            conf.set("hbase.zookeeper.quorum", SettingsFacade.getInstance().hbase.getZookeeperQuorum());
        }
        CustomLogger.debug(this, "Init: START");
        hbaseAdmin = new HBaseAdmin(conf);
        CustomLogger.debug(this, "Init: DONE");
        metaTable = new HTable(conf, Bytes.toBytes("hbase:meta"));
        CustomLogger.debug(this, "Init HBaseFacade: DONE");
        hBaseDump = new HBaseDump();
    }

    /**
     * Gets an instance of this class if it exists, otherwise create this first instance and then return it.
     * (following the Singleton Design Pattern).
     *
     * @return The single instance of this class
     * @throws IOException
     */
    public static HBaseDriverV0 getInstance() throws IOException {
        if(instance == null) {
            instance = new HBaseDriverV0();
        }

        return instance;
    }

    public void moveRegion(String encodedRegionName, String destServerName) throws HBaseIOException, MasterNotRunningException, ZooKeeperConnectionException {
        CustomLogger.debug(this, "MOVE(" + encodedRegionName + ", " + destServerName + ")");
        try {
            hbaseAdmin.move(encodedRegionName.getBytes(), destServerName.getBytes());
            CustomLogger.debug(this, "Region move done: " + encodedRegionName);
        } catch (UnknownRegionException e) {
            CustomLogger.debug(this, "Unknown region " + encodedRegionName + "!");
        }
    }

    public boolean splitRegion(String encodedRegionName, byte[] splitKey) throws IOException, InterruptedException {
        if(splitKey != null && splitKey.length > 0) {
            hbaseAdmin.split(encodedRegionName.getBytes(), splitKey);
        } else {
            hbaseAdmin.split(encodedRegionName.getBytes());
        }
        return true;
    }

    public void splitTable(String tableName, byte[] splitKey) throws IOException, InterruptedException {
        if(splitKey != null && splitKey.length > 0) {
            hbaseAdmin.split(tableName.getBytes(), splitKey);
        } else {
            hbaseAdmin.split(tableName.getBytes());
        }
    }

    public String getRegionServerForRegion(HRegionInfo region) throws IOException {
        Scan scan = new Scan();
        Filter filterByRow = new RowFilter(CompareFilter.CompareOp.EQUAL, new BinaryPrefixComparator(region.getRegionName()));
        scan.setFilter(filterByRow);
        ResultScanner resultScanner = metaTable.getScanner(scan);

        try {
            return Bytes.toString(resultScanner.next().getValue("info".getBytes(), "server".getBytes()));
        } catch(IOException e) {
            //TODO : log exception or better handling
            return null;
        }
    }

    public ArrayList<RegionServerView> getRegionServers() throws IOException {
        ArrayList<RegionServerView> regionServersList = new ArrayList();
        final ClusterStatus clusterStatus = hbaseAdmin.getClusterStatus();
        int delta;

        int index = 0;
        for (ServerName serverName : clusterStatus.getServers()) {
            RegionServerView regionServerView = new RegionServerView();
            final ServerLoad serverLoad = clusterStatus.getLoad(serverName);

            regionServerView.id = index++;
            regionServerView.name = serverName.getServerName();
            regionServerView.writeRequestsCount = serverLoad.getWriteRequestsCount();
            regionServerView.readRequestsCount = serverLoad.getReadRequestsCount();

            delta = regionServerView.writeRequestsCount - hBaseDump.getDumpedWriteRequestsCountForRegionServer(regionServerView.name);
            regionServerView.writeRequestsDelta = (delta > 0)?delta:0;

            delta = regionServerView.readRequestsCount - hBaseDump.getDumpedReadRequestsCountForRegionServer(regionServerView.name);
            regionServerView.readRequestsDelta = (delta > 0)?delta:0;

            regionServerView.regions = getRegionsForRegionServer(serverName, serverLoad);

            regionServersList.add(regionServerView);
        }

        return regionServersList;
    }

    public ArrayList<RegionView> getRegionsForRegionServer(ServerName serverName, ServerLoad serverLoad) {
        ArrayList<RegionView> regionViewList = new ArrayList<RegionView>();
        long delta;

        for (Map.Entry<byte[], RegionLoad> entry : serverLoad.getRegionsLoad().entrySet()) {
            final String region = Bytes.toString(entry.getKey());

            // If system table, we just skip it
            if(! region.contains("hbase:")) {
                final RegionLoad regionLoad = entry.getValue();
                long storeFileSize = regionLoad.getStorefileSizeMB();

                // Format of a region key, we need to split by "," first and then by "."
                // region=ec2-52-9-163-184.us-west-1.compute.amazonaws.com-tsdb-uid,,1460646639107.7ff084bf2f7d654ee32b2ac3334f130f.
                String[] split1 = region.split(",");
                String tableName = split1[0];
                String[] split2 = split1[split1.length - 1].split("\\.");
                String regionId = split2[0];

                String encodedName = "none";
                if (split2.length >= 2) {
                    encodedName = split2[1];
                }

                RegionView regionView = new RegionView();
                regionView.sizeMB = (int) storeFileSize;
                // regionView.regionName = StringEscapeUtils.escapeJava(region);
                regionView.regionName = StringEscapeUtils.escapeJava(region).replaceAll("\"", "");
                //CustomLogger.debug(this, "===================================");
                //CustomLogger.debug(this, "regionName = "+StringEscapeUtils.escapeJava(region).replaceAll("\"", ""));
                regionView.id = new Long(regionId);
                regionView.encodedName = encodedName;
                regionView.tableName = StringEscapeUtils.escapeJava(tableName);
                regionView.currentRegionServer = serverName.getServerName();
                regionView.readRequestsCount = regionLoad.getReadRequestsCount();
                regionView.writeRequestsCount = regionLoad.getWriteRequestsCount();

                delta = regionView.writeRequestsCount - hBaseDump.getDumpedWriteRequestsCountForRegion(regionView.encodedName);
                regionView.writeRequestsDelta = (delta > 0)?delta:0;

                delta = regionView.readRequestsCount - hBaseDump.getDumpedReadRequestsCountForRegion(regionView.encodedName);
                regionView.readRequestsDelta = (delta > 0)?delta:0;

                regionViewList.add(regionView);
            }
        }

        return regionViewList;
    }

    public HashMap<String, ArrayList<RegionView>> getRegionsDistributionPerRegionServer() throws IOException {
        final ClusterStatus clusterStatus = hbaseAdmin.getClusterStatus();
        HashMap<String, ArrayList<RegionView>> regionServers = new HashMap();

        for (ServerName serverName : clusterStatus.getServers()) {
            final ServerLoad serverLoad = clusterStatus.getLoad(serverName);

            if (!regionServers.containsKey(serverName.getServerName())) {
                regionServers.put(
                        serverName.getServerName(),
                        getRegionsForRegionServer(serverName, serverLoad)
                );
            }
        }

        return regionServers;
    }

    public ArrayList<RegionView> getRegions() throws IOException {
        ArrayList<RegionView> regions = new ArrayList<RegionView>();
        HashMap<String, ArrayList<RegionView>> regionServers = getRegionsDistributionPerRegionServer();

        for (Map.Entry<String, ArrayList<RegionView>> entry : regionServers.entrySet()) {
            regions.addAll(entry.getValue());
        }

        return regions;
    }

    public ArrayList<TableView> getTables() throws IOException {
        ArrayList<TableView> tables = new ArrayList<TableView>();
        HTableDescriptor[] tableDescriptors = hbaseAdmin.listTables();

        for(HTableDescriptor tableDesc: tableDescriptors) {
            if(! tableDesc.getNameAsString().startsWith("hbase:")) {
                TableView tableView = new TableView();
                tableView.tableName = tableDesc.getNameAsString();
                tables.add(tableView);
            }
        }

        return tables;
    }

    public ArrayList<RegionView> getRegionsForTable(String tableName) throws IOException {
        ArrayList<RegionView> allRegions = getRegions();
        ArrayList<RegionView> regionsForTable = new ArrayList<RegionView>();

        for(RegionView region: allRegions) {
            if(region.tableName.equals(tableName)) {
                regionsForTable.add(region);
            }
        }

        return regionsForTable;
    }

    public RegionView getRegion(String encodedName) throws IOException {
        ArrayList<RegionView> allRegions = getRegions();

        for(RegionView region: allRegions) {
            if(region.encodedName.equals(encodedName)) return region;
        }

        return null;
    }

    public TableView getTable(String tableName) throws IOException {
        ArrayList<TableView> allTables = getTables();

        for(TableView table: allTables) {
            if(table.tableName.equals(tableName)) {
                table.tableRegions = getRegionsForTable(tableName);
                return table;
            }
        }

        return null;
    }

    public int getMemStoreFlushSizeMB() {
        return Integer.parseInt(conf.get("hbase.hregion.memstore.flush.size")) / (1024*1024);
    }

    public double getMemStoreUpperLimit() {
        return Double.parseDouble(conf.get("hbase.regionserver.global.memstore.upperLimit"));
    }

    public String getZookeeperQuorum() {
        return conf.get("hbase.zookeeper.quorum");
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
