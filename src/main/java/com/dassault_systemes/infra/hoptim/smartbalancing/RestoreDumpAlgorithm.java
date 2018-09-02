package com.dassault_systemes.infra.hoptim.smartbalancing;

import com.dassault_systemes.infra.hoptim.hbase.HBaseFacade;
import com.dassault_systemes.infra.hoptim.hbase.RegionView;
import com.dassault_systemes.infra.hoptim.log.CustomLogger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by erl1 on 9/21/2016.
 */
public class RestoreDumpAlgorithm implements IBalancingAlgorithm {
    public RestoreDumpAlgorithm(String jsonConfig) {
        //Nothing as of now
    }

    public ExecutionPlan getExecutionPlan() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        HashMap<String, ArrayList<RegionView>> currentRegionsDistribution = HBaseFacade.getInstance().getRegionsDistributionPerRegionServer();
        HashMap<String, ArrayList<RegionView>> dumpedRegionsDistribution = HBaseFacade.getInstance().getDumpedRegionsDistribution();
        ArrayList<RegionView> regionsList = new ArrayList<RegionView>();
        ExecutionPlan plan = new ExecutionPlan();
        HashMap<String, RegionView> dumpedRegionsList = new HashMap<String, RegionView>();

        for(Map.Entry<String, ArrayList<RegionView>> entry : dumpedRegionsDistribution.entrySet()) {
            for(RegionView regionView: entry.getValue()) {
                dumpedRegionsList.put(regionView.encodedName, regionView);
            }
        }

        for(Map.Entry<String, ArrayList<RegionView>> entry : currentRegionsDistribution.entrySet()) {
            String regionServerName = entry.getKey();
            ArrayList<RegionView> regionViews = entry.getValue();

            for (RegionView regionView: regionViews) {
                CustomLogger.debug(this, "regionView=" + regionView.encodedName);
                if(dumpedRegionsList.containsKey(regionView.encodedName)) {
                    CustomLogger.debug(this, "This region is in the dumped region list");
                    String previousRegionServerName = dumpedRegionsList.get(regionView.encodedName).currentRegionServer;
                    String currentRegionServerName = regionView.currentRegionServer;

                    CustomLogger.debug(this, "previousRegionServerName=" + previousRegionServerName);
                    CustomLogger.debug(this, "currentRegionServerName" + currentRegionServerName);

                    if (!currentRegionServerName.equals(previousRegionServerName)) {
                        CustomLogger.debug(this, "This region is having different hosting region server than in the dump: doing MOVE");
                        RegionMove move = new RegionMove();
                        move.encodedRegionName = regionView.encodedName;
                        move.fromRegionServer = currentRegionServerName;
                        move.toRegionServer = previousRegionServerName;
                        move.regionName = regionView.regionName;
                        plan.operations.add(move);
                    }
                }
            }
        }

        plan.currentRegionBalancing = currentRegionsDistribution;
        plan.newRegionBalancing = dumpedRegionsDistribution;
        plan.isScoreImplemented = false;

        return plan;
    }
}
