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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by ERL1 on 6/7/2016.
 */
public class OpenTSDBVipSplitAlgorithm implements IBalancingAlgorithm {
    private ArrayList<byte[]> splitKeys;
    private OpenTSDBFacade openTSDBFacade;
    private HashMap<String, ArrayList<RegionView>> newRegionServersList;
    private ExecutionPlan executionPlan;
    private HashMap<String, Integer> splitCreditForMetric;
    private int totalSplitCredits;

    public OpenTSDBVipSplitAlgorithm(String jsonConfig) {
        //Nothing as of now
    }

    public ExecutionPlan getExecutionPlan() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        HashMap<String, ArrayList<RegionView>> currentRegionServersDistribution = HBaseFacade.getInstance().getRegionsDistributionPerRegionServer();
        SplitRecommandation splitRecommandation = new SplitRecommandation();
        totalSplitCredits = splitRecommandation.numberOfSplitsToMake;
        HashMap<String, Double> vipMetrics = SettingsFacade.getInstance().opentsdb.getVipMetrics();
        HashMap<String, ArrayList<RegionView>> sortedCurrentRegionServersDistribution;

        splitCreditForMetric = new HashMap();
        newRegionServersList = new HashMap<String, ArrayList<RegionView>>();
        executionPlan = new ExecutionPlan();
        splitKeys = new ArrayList<byte[]>();
        openTSDBFacade = OpenTSDBFacade.getInstance();

        for(Map.Entry<String, Double> entry: vipMetrics.entrySet()) {
            String metric = entry.getKey();
            double weight = entry.getValue();
            int creditForMetric = 0;

            if(totalSplitCredits > 0) {
                creditForMetric = (int) Math.round(((double) totalSplitCredits) * weight);
            }

            CustomLogger.debug(this, "splitCreditForMetric " + metric + " => " + creditForMetric);
            splitCreditForMetric.put(metric, creditForMetric);
            totalSplitCredits -= creditForMetric;
        }

        sortedCurrentRegionServersDistribution = SortUtils.sortDescRegionServersBySize(currentRegionServersDistribution);

        CustomLogger.debug(this, "totalSplitCredits=" + totalSplitCredits);
        for (Map.Entry<String, ArrayList<RegionView>> entry : sortedCurrentRegionServersDistribution.entrySet()) {
            if (totalSplitCredits <= 0) break;

            String regionServer = entry.getKey();
            ArrayList<RegionView> regions = entry.getValue();

            if (!newRegionServersList.containsKey(regionServer)) {
                newRegionServersList.put(regionServer, new ArrayList<RegionView>());
            }

            for (RegionView region : regions) {
                CustomLogger.debug(this, "region=" + region.regionName);
                newRegionServersList.get(regionServer).add(region);

                if (region.tableName.equals(SettingsFacade.getInstance().opentsdb.getTsdbTable())) {
                    CustomLogger.debug(this, "table=" + region.tableName);
                    ArrayList<String> metricsForRegion = openTSDBFacade.getMetricsForRegion(region.encodedName, "*");

                    for (Map.Entry<String, Integer> entry2 : splitCreditForMetric.entrySet()) {
                        String vipMetric = entry2.getKey();

                        if (metricsForRegion.contains(vipMetric) && splitCreditForMetric.get(vipMetric) > 0) {
                            CustomLogger.debug(this, "isolateMetric for " + vipMetric);
                            isolateMetric(regionServer, region, vipMetric);
                        }
                    }
                }
            }
        }

        executionPlan.currentRegionBalancing = currentRegionServersDistribution;
        executionPlan.newRegionBalancing = newRegionServersList;

        return executionPlan;
    }

    private void isolateMetric( String regionServer, RegionView region, String metric) throws IOException {
        RegionView r1 = null;
        RegionView r2 = null;
        TableSplit split1 = new TableSplit();
        split1.tableName = region.tableName;
        split1.splitKey = openTSDBFacade.getFirstKeyForMetric(metric).getRawKeyAsBytes();
        split1.splitKeyAsHex = BytesHelper.bytesToHex(split1.splitKey);
        byte[] splitMetricId1 = Arrays.copyOfRange(split1.splitKey, 0, 3);

        if(     BytesHelper.compare(split1.splitKey, OpenTSDBKey.SMALLEST_KEY.getRawKeyAsBytes()) > 0
                && BytesHelper.compare(splitMetricId1, region.startKey.getMetricAsBytes()) != 0
                && !splitKeyAlreadyAdded(split1.splitKey) ) {
                executionPlan.operations.add(split1);
                splitKeys.add(split1.splitKey);
                splitCreditForMetric.put(metric, splitCreditForMetric.get(metric) - 1);

                r1 = new RegionView();
                r1.regionName = "[Split 1 because of VIP metric " + metric + "]";
                r1.encodedName = region.encodedName + "_split_" + splitKeys.size();
                r1.tableName = region.tableName;
                r1.startKey = new OpenTSDBKey(split1.splitKey);
                r1.endKey = OpenTSDBKey.BIGGEST_KEY;
                r1.sizeMB = region.sizeMB / 3;
                //r1.startMetricName = openTSDBFacade.getMetricByKey(r1.startKey.getRawKeyAsBytes()).name;
                //r1.endMetricName = openTSDBFacade.getMetricByKey(r1.endKey.getRawKeyAsBytes()).name;
                r1.metrics = openTSDBFacade.getMetricsForRange(r1.startKey, r1.endKey);
        }

        TableSplit split2 = new TableSplit();
        split2.tableName = region.tableName;
        split2.splitKey = openTSDBFacade.getLastKeyForMetric(metric).getRawKeyAsBytes();
        split2.splitKeyAsHex = BytesHelper.bytesToHex(split2.splitKey);
        byte[] splitMetricId2 = Arrays.copyOfRange(split2.splitKey, 0, 3);

        if(     BytesHelper.compare(split2.splitKey, OpenTSDBKey.BIGGEST_KEY.getRawKeyAsBytes()) < 0
                && BytesHelper.compare(splitMetricId2, region.endKey.getMetricAsBytes()) != 0
                && !splitKeyAlreadyAdded(split2.splitKey) ) {
                executionPlan.operations.add(split2);
                splitKeys.add(split2.splitKey);
                splitCreditForMetric.put(metric, splitCreditForMetric.get(metric) - 1);

                r2 = new RegionView();
                r2.regionName = "[Split 2 because of VIP metric " + metric + "]";
                r2.encodedName = region.encodedName + "_split_" + splitKeys.size();
                r2.tableName = region.tableName;
                r2.startKey = new OpenTSDBKey(split2.splitKey);
                r2.endKey = new OpenTSDBKey(region.endKey.getRawKeyAsBytes());
                //r2.startMetricName = openTSDBFacade.getMetricByKey(r2.startKey.getRawKeyAsBytes()).name;
                //r2.endMetricName = openTSDBFacade.getMetricByKey(r2.endKey.getRawKeyAsBytes()).name;
                r2.metrics = openTSDBFacade.getMetricsForRange(r2.startKey, r2.endKey);
                r2.sizeMB = region.sizeMB / 3;

                if(r1 != null) {
                    r1.endKey = r2.startKey;
                    r1.startMetricName = openTSDBFacade.getMetricByKey(r1.startKey.getRawKeyAsBytes()).name;
                    r1.endMetricName = openTSDBFacade.getMetricByKey(r1.endKey.getRawKeyAsBytes()).name;
                    r1.metrics = openTSDBFacade.getMetricsForRange(r1.startKey, r1.endKey);
                }
        }

        if(r1 != null) {
            newRegionServersList.get(regionServer).add(r1);
        }

        if(r2 != null) {
            newRegionServersList.get(regionServer).add(r2);
        }

        if(split1 != null) {
            RegionView splittedRegionOrig = new RegionView();
            splittedRegionOrig.regionName = "[Splitted because of VIP metric " + metric + "]";
            splittedRegionOrig.tableName = region.tableName;
            splittedRegionOrig.sizeMB = region.sizeMB / 3;
            splittedRegionOrig.endKey = new OpenTSDBKey(split1.splitKey);
            splittedRegionOrig.startKey = region.startKey;
            splittedRegionOrig.startMetricName = openTSDBFacade.getMetricByKey(splittedRegionOrig.startKey.getRawKeyAsBytes()).name;
            splittedRegionOrig.endMetricName = openTSDBFacade.getMetricByKey(splittedRegionOrig.endKey.getRawKeyAsBytes()).name;
            splittedRegionOrig.metrics = openTSDBFacade.getMetricsForRange(splittedRegionOrig.startKey, splittedRegionOrig.endKey);

            newRegionServersList.get(regionServer).remove(region);
            newRegionServersList.get(regionServer).add(splittedRegionOrig);
        }
    }

    private boolean splitKeyAlreadyAdded(byte[] splitKey) {
        for(byte[] key: splitKeys) {
            if(BytesHelper.compare(key, splitKey) == 0) {
                return true;
            }
        }

        return false;
    }
}
