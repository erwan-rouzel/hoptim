package com.dassault_systemes.infra.hoptim.hbase;

import com.dassault_systemes.infra.hoptim.opentsdb.OpenTSDBKey;
import com.dassault_systemes.infra.hoptim.util.BytesHelper;

import java.util.ArrayList;

/**
 * This encapsulate the important informations about a region. This is an abstraction over the HBase API layer
 * to get only the informations in which we are interested.
 *
 * Created by ERL1 on 4/19/2016.
 *
 *
 */
public class RegionView {
    public int sizeMB;
    public long id;
    public String encodedName;
    public String startMetricName;
    public String endMetricName;
    public OpenTSDBKey startKey;
    public OpenTSDBKey endKey;
    public String currentRegionServer;
    public String regionName;
    public String tableName;
    public ArrayList<String> metrics;
    public long writeRequestsCount;
    public long readRequestsCount;
    public long writeRequestsDelta;
    public long readRequestsDelta;

    public RegionView() {
        startKey = new OpenTSDBKey(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        endKey = new OpenTSDBKey(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        metrics = new ArrayList<String>();
    }

    public boolean containsKey(byte[] key) {
        byte[] startKeyAsBytes = startKey.getRawKeyAsBytes();
        byte[] endKeyAsBytes = endKey.getRawKeyAsBytes();

        return (BytesHelper.compare(startKeyAsBytes, key) <=0 && BytesHelper.compare(key, endKeyAsBytes) < 0);
    }
}
