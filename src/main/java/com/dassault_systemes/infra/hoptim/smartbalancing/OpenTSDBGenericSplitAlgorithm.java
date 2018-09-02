package com.dassault_systemes.infra.hoptim.smartbalancing;

import com.dassault_systemes.infra.hoptim.hbase.HBaseFacade;
import com.dassault_systemes.infra.hoptim.hbase.RegionView;
import com.dassault_systemes.infra.hoptim.hbase.SortUtils;
import com.dassault_systemes.infra.hoptim.log.CustomLogger;
import com.dassault_systemes.infra.hoptim.opentsdb.OpenTSDBFacade;
import com.dassault_systemes.infra.hoptim.opentsdb.OpenTSDBKey;
import com.dassault_systemes.infra.hoptim.opentsdb.SplitRecommandation;
import com.dassault_systemes.infra.hoptim.settings.SettingsFacade;
import com.dassault_systemes.infra.hoptim.util.BytesHelper;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.internal.inject.Custom;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ERL1 on 7/4/2016.
 */
public class OpenTSDBGenericSplitAlgorithm implements IBalancingAlgorithm {
    private ArrayList<byte[]> splitKeys;
    private OpenTSDBFacade openTSDBFacade;
    private HashMap<String, ArrayList<RegionView>> newRegionServersList;
    private ExecutionPlan executionPlan;
    private int numberOfSplitsToMake;
    private int totalSplitCredits;

    public OpenTSDBGenericSplitAlgorithm(String jsonConfig) throws JSONException {
        JSONObject obj = new JSONObject(jsonConfig);
        numberOfSplitsToMake = obj.getInt("number_of_splits");
    }

    public ExecutionPlan getExecutionPlan() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        HashMap<String, ArrayList<RegionView>> currentRegionServersDistribution = HBaseFacade.getInstance().getRegionsDistributionPerRegionServer();
        SplitRecommandation splitRecommandation = new SplitRecommandation();
        totalSplitCredits = splitRecommandation.numberOfSplitsToMake;
        HashMap<String, Double> vipMetrics = SettingsFacade.getInstance().opentsdb.getVipMetrics();
        HashMap<String, ArrayList<RegionView>> sortedCurrentRegionServersDistribution;

        newRegionServersList = new HashMap<String, ArrayList<RegionView>>();
        executionPlan = new ExecutionPlan();
        splitKeys = new ArrayList<byte[]>();
        openTSDBFacade = OpenTSDBFacade.getInstance();

        totalSplitCredits = numberOfSplitsToMake;

        sortedCurrentRegionServersDistribution = SortUtils.sortDescRegionServersBySize(currentRegionServersDistribution);

        for (Map.Entry<String, ArrayList<RegionView>> entry : sortedCurrentRegionServersDistribution.entrySet()) {
            if (totalSplitCredits <= 0) break;

            String regionServer = entry.getKey();
            ArrayList<RegionView> regions = entry.getValue();

            if (!newRegionServersList.containsKey(regionServer)) {
                newRegionServersList.put(regionServer, new ArrayList<RegionView>());
            }

            for (RegionView region : regions) {
                newRegionServersList.get(regionServer).add(region);

                if (region.tableName.equals(SettingsFacade.getInstance().opentsdb.getTsdbTable())) {
                    CustomLogger.debug(this, "Check Credit (totalSplitCredits=" + totalSplitCredits + ", region.metrics.size()=" + region.metrics.size());
                    if (totalSplitCredits > 0 && region.metrics.size() >= 2) {
                        CustomLogger.debug(this, "Adding split by metric");
                        String splitMetric = region.metrics.get((region.metrics.size() / 2));
                        splitByMetric(regionServer, region, splitMetric);
                    }
                }
            }
        }

        executionPlan.currentRegionBalancing = currentRegionServersDistribution;
        executionPlan.newRegionBalancing = newRegionServersList;

        return executionPlan;
    }

    private void splitByMetric( String regionServer, RegionView region, String metric) throws IOException {
        RegionView r = null;
        TableSplit split = new TableSplit();
        split.tableName = region.tableName;
        split.splitKey = openTSDBFacade.getFirstKeyForMetric(metric).getRawKeyAsBytes();
        split.splitKeyAsHex = BytesHelper.bytesToHex(split.splitKey);
        byte[] splitMetricId1 = Arrays.copyOfRange(split.splitKey, 0, 3);

        if(     BytesHelper.compare(split.splitKey, OpenTSDBKey.SMALLEST_KEY.getRawKeyAsBytes()) > 0
                && BytesHelper.compare(splitMetricId1, region.startKey.getMetricAsBytes()) != 0
                && !splitKeyAlreadyAdded(split.splitKey) ) {
            executionPlan.operations.add(split);
            splitKeys.add(split.splitKey);
            totalSplitCredits--;

            r = new RegionView();
            r.regionName = "[Split 1 because of metric " + metric + "]";
            r.encodedName = region.encodedName + "_split_" + splitKeys.size();
            r.tableName = region.tableName;
            r.startKey = new OpenTSDBKey(split.splitKey);
            r.endKey = OpenTSDBKey.BIGGEST_KEY;
            r.sizeMB = region.sizeMB / 2;
            r.startMetricName = openTSDBFacade.getMetricByKey(r.startKey.getRawKeyAsBytes()).name;
            r.endMetricName = openTSDBFacade.getMetricByKey(r.endKey.getRawKeyAsBytes()).name;
            r.metrics = openTSDBFacade.getMetricsForRange(r.startKey, r.endKey);

            newRegionServersList.get(regionServer).add(r);
            RegionView splittedRegionOrig = new RegionView();
            splittedRegionOrig.regionName = "[Splitted because of metric " + metric + "]";
            splittedRegionOrig.tableName = region.tableName;
            splittedRegionOrig.sizeMB = region.sizeMB / 2;
            splittedRegionOrig.endKey = new OpenTSDBKey(split.splitKey);
            splittedRegionOrig.startKey = region.startKey;
            splittedRegionOrig.startMetricName = openTSDBFacade.getMetricByKey(splittedRegionOrig.startKey.getRawKeyAsBytes()).name;
            splittedRegionOrig.endMetricName = openTSDBFacade.getMetricByKey(splittedRegionOrig.endKey.getRawKeyAsBytes()).name;
            splittedRegionOrig.metrics = openTSDBFacade.getMetricsForRange(splittedRegionOrig.startKey, splittedRegionOrig.endKey);

            newRegionServersList.get(regionServer).remove(region);
            newRegionServersList.get(regionServer).add(splittedRegionOrig);
        }
    }

    private boolean splitKeyAlreadyAdded(byte[] splitKey) {
        CustomLogger.debug(this, "splitKeyAlreadyAdded");
        for(byte[] key: splitKeys) {
            if(BytesHelper.compare(key, splitKey) == 0) {
                return true;
            }
        }

        return false;
    }
}
