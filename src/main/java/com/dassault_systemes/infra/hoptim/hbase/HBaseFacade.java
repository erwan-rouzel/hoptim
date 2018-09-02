package com.dassault_systemes.infra.hoptim.hbase;

import com.dassault_systemes.infra.hoptim.settings.SettingsFacade;
import org.apache.hadoop.hbase.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This is the main entry point to this package (following the Facade Design Pattern).
 *
 * Created by ERL1 on 4/15/2016.
 */
public class HBaseFacade implements IHBaseDriver {
    private static HBaseFacade instance;
    IHBaseDriver hBaseDriver;

    /**
     * Private constructor. This just prevent from instanciating this class without going through the getInstance method.
     *
     * @throws IOException
     */
    private HBaseFacade() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        String hbaseDriverClassName = SettingsFacade.getInstance().hbase.getDriverClassName();
        Class<?> clazz = Class.forName(hbaseDriverClassName);
        Method method = clazz.getMethod("getInstance");
        hBaseDriver = (IHBaseDriver) method.invoke(null);
    }

    /**
     * Gets an instance of this class if it exists, otherwise create this first instance and then return it.
     * (following the Singleton Design Pattern).
     *
     * @return The single instance of this class
     * @throws IOException
     */
    public static HBaseFacade getInstance() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
        if(instance == null) {
            instance = new HBaseFacade();
        }

        return instance;
    }

    public void moveRegion(String encodedRegionName, String destServerName) throws HBaseIOException, MasterNotRunningException, ZooKeeperConnectionException {
        hBaseDriver.moveRegion(encodedRegionName, destServerName);
    }

    public boolean splitRegion(String encodedRegionName, byte[] splitKey) throws IOException, InterruptedException {
        return hBaseDriver.splitRegion(encodedRegionName, splitKey);
    }

    public void splitTable(String tableName, byte[] splitKey) throws IOException, InterruptedException {
        hBaseDriver.splitTable(tableName, splitKey);
    }

    public String getRegionServerForRegion(HRegionInfo region) throws IOException {
        return hBaseDriver.getRegionServerForRegion(region);
    }

    public ArrayList<RegionServerView> getRegionServers() throws IOException {
        return hBaseDriver.getRegionServers();
    }

    public ArrayList<RegionView> getRegionsForRegionServer(ServerName serverName, ServerLoad serverLoad) {
        return hBaseDriver.getRegionsForRegionServer(serverName, serverLoad);
    }

    public HashMap<String, ArrayList<RegionView>> getRegionsDistributionPerRegionServer() throws IOException {
        return hBaseDriver.getRegionsDistributionPerRegionServer();
    }

    public ArrayList<RegionView> getRegions() throws IOException {
        return hBaseDriver.getRegions();
    }

    public ArrayList<TableView> getTables() throws IOException {
        return hBaseDriver.getTables();
    }

    public ArrayList<RegionView> getRegionsForTable(String tableName) throws IOException {
       return hBaseDriver.getRegionsForTable(tableName);
    }

    public RegionView getRegion(String encodedName) throws IOException {
        return hBaseDriver.getRegion(encodedName);
    }

    public TableView getTable(String tableName) throws IOException {
        return hBaseDriver.getTable(tableName);
    }

    public int getMemStoreFlushSizeMB() {
        return hBaseDriver.getMemStoreFlushSizeMB();
    }

    public double getMemStoreUpperLimit() {
        return hBaseDriver.getMemStoreUpperLimit();
    }

    public String getZookeeperQuorum() {
        return hBaseDriver.getZookeeperQuorum();
    }

    public boolean updateDump() { return hBaseDriver.updateDump();}

    public long latestDumpTimestamp() { return hBaseDriver.latestDumpTimestamp();}

    public HashMap<String, ArrayList<RegionView>> getDumpedRegionsDistribution() throws IOException {
        return hBaseDriver.getDumpedRegionsDistribution();
    }
}
