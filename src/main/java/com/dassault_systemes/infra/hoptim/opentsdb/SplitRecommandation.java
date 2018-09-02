package com.dassault_systemes.infra.hoptim.opentsdb;

import com.dassault_systemes.infra.hoptim.hbase.HBaseFacade;
import com.dassault_systemes.infra.hoptim.settings.SettingsFacade;
import com.dassault_systemes.infra.hoptim.util.BytesHelper;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by ERL1 on 5/18/2016.
 */
public class SplitRecommandation {
    public int memStoreFlushSizeMB;
    public double memStoreUpperLimit;
    public int memAllocatedPerRegionServerMB;
    public int numColumnFamilies;
    public String startKey;
    public String endKey;
    public int numberOfKeys;
    public int numberOfRegionServers;
    public int maxNumberOfRegionsPerRegionServer;
    public int optimalTotalNumberOfRegions;
    public int currentTotalNumberOfRegions;
    public String prepsplitCommandLine;
    public int numberOfSplitsToMake;
    public double regionServerFillFactor;

    // These fields are not mapped to Json:
    private transient ArrayList<String> allMetrics;
    private transient OpenTSDBFacade openTSDBFacade;
    private transient HBaseFacade hbaseFacade;
    private transient SettingsFacade settingsFacade;
    private transient Configuration conf;

    public SplitRecommandation() throws IllegalAccessException, IOException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
        conf = HBaseConfiguration.create();
        openTSDBFacade = OpenTSDBFacade.getInstance();
        hbaseFacade = HBaseFacade.getInstance();
        settingsFacade = SettingsFacade.getInstance();
        memStoreFlushSizeMB = hbaseFacade.getMemStoreFlushSizeMB();
        memStoreUpperLimit = hbaseFacade.getMemStoreUpperLimit();
        memAllocatedPerRegionServerMB = Integer.parseInt(settingsFacade.hbase.getRegionServerMemory());
        numColumnFamilies = 1;
        regionServerFillFactor = settingsFacade.opentsdb.getRegionServerFillFactor();

        allMetrics = openTSDBFacade.getAllMetrics("");
        numberOfKeys = allMetrics.size();
        startKey = "\\x00\\x00\\x00\\x01";

        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(numberOfKeys);
        endKey =  BytesHelper.bytesToHex(b.array());

        numberOfRegionServers = hbaseFacade.getRegionServers().size();

        // (RS memory)*(memstore.upperLimit)/((memstore.flush.size)*(# column families))
        maxNumberOfRegionsPerRegionServer = (int) Math.round( (double) memAllocatedPerRegionServerMB * memStoreUpperLimit / (memStoreFlushSizeMB * (double) numColumnFamilies));

        optimalTotalNumberOfRegions = (int) Math.round((double) maxNumberOfRegionsPerRegionServer * regionServerFillFactor * numberOfRegionServers);

        prepsplitCommandLine = generatePresplitCommandLine();

        currentTotalNumberOfRegions = hbaseFacade.getRegions().size();

        numberOfSplitsToMake = optimalTotalNumberOfRegions - currentTotalNumberOfRegions;
    }

    private String generatePresplitCommandLine() {
        return "hbase org.apache.hadoop.hbase.util.RegionSplitter " +
                settingsFacade.opentsdb.getTsdbTable() +
                " UniformSplit -c " + optimalTotalNumberOfRegions +
                " -f t" +
                " --firstrow 1" +
                " --lastrow " + numberOfKeys;
    }
}
