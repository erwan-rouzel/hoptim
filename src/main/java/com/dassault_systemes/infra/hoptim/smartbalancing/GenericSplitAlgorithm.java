package com.dassault_systemes.infra.hoptim.smartbalancing;

import com.dassault_systemes.infra.hoptim.hbase.HBaseFacade;
import com.dassault_systemes.infra.hoptim.hbase.RegionView;
import com.dassault_systemes.infra.hoptim.hbase.SortUtils;
import com.dassault_systemes.infra.hoptim.opentsdb.OpenTSDBFacade;
import com.dassault_systemes.infra.hoptim.opentsdb.SplitRecommandation;
import com.dassault_systemes.infra.hoptim.util.BytesHelper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by ERL1 on 6/7/2016.
 */
public class GenericSplitAlgorithm implements IBalancingAlgorithm {
    private OpenTSDBFacade openTSDBFacade;
    private HashMap<String, ArrayList<RegionView>> newRegionServersList;
    private ExecutionPlan executionPlan;
    private int totalSplitCredits;
    private int numberOfSplitsToMake;
    private int numSplits;
    private ArrayList<String> tablesSelection;

    public GenericSplitAlgorithm(String jsonConfig) throws JSONException {
        tablesSelection = new ArrayList<String>();
        JSONObject obj = new JSONObject(jsonConfig);
        numberOfSplitsToMake = obj.getInt("number_of_splits");

        JSONArray jsonTablesSelection = obj.getJSONArray("tables_selection");
        for(int i = 0; i < jsonTablesSelection.length(); i++) {
            tablesSelection.add(jsonTablesSelection.getString(i));
        }
    }

    public ExecutionPlan getExecutionPlan() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        HashMap<String, ArrayList<RegionView>> currentRegionServersDistribution = HBaseFacade.getInstance().getRegionsDistributionPerRegionServer();
        SplitRecommandation splitRecommandation = new SplitRecommandation();
        HashMap<String, ArrayList<RegionView>> sortedCurrentRegionServersDistribution;

        totalSplitCredits = numberOfSplitsToMake;
        newRegionServersList = new HashMap<String, ArrayList<RegionView>>();
        executionPlan = new ExecutionPlan();
        openTSDBFacade = OpenTSDBFacade.getInstance();
        numSplits = 0;

        sortedCurrentRegionServersDistribution = SortUtils.sortDescRegionServersBySize(currentRegionServersDistribution);

        for (Map.Entry<String, ArrayList<RegionView>> entry : sortedCurrentRegionServersDistribution.entrySet()) {
            String regionServer = entry.getKey();
            ArrayList<RegionView> regions = entry.getValue();

            if (!newRegionServersList.containsKey(regionServer)) {
                newRegionServersList.put(regionServer, new ArrayList<RegionView>());
            }

            for (RegionView region : regions) {

                if(isTableSelected(region.tableName)) {
                    newRegionServersList.get(regionServer).add(region);

                    if (totalSplitCredits > 0) {
                        split(regionServer, region);
                    }
                }
            }
        }

        executionPlan.currentRegionBalancing = currentRegionServersDistribution;
        executionPlan.newRegionBalancing = newRegionServersList;

        return executionPlan;
    }

    private boolean isTableSelected(String tableName) {
        return tablesSelection.contains(tableName);
    }

    private void split( String regionServer, RegionView region) throws IOException {
        RegionView r;
        RegionSplit split = new RegionSplit();
        split.regionEncodedName = region.encodedName;
        split.splitKey = TableSplit.AUTO_SPLIT_KEY;
        split.splitKeyAsHex = BytesHelper.bytesToHex(TableSplit.AUTO_SPLIT_KEY);

        executionPlan.operations.add(split);
        totalSplitCredits--;
        numSplits++;

        r = new RegionView();
        r.regionName = "[Split 1 (generic)]";
        r.encodedName = region.encodedName + "_split_" + numSplits;
        r.tableName = region.tableName;
        r.sizeMB = region.sizeMB / 2;

        newRegionServersList.get(regionServer).add(r);
        RegionView splittedRegionOrig = new RegionView();
        splittedRegionOrig.regionName = "[Splitted (generic)]";
        splittedRegionOrig.tableName = region.tableName;
        splittedRegionOrig.sizeMB = region.sizeMB / 2;

        newRegionServersList.get(regionServer).remove(region);
        newRegionServersList.get(regionServer).add(splittedRegionOrig);
    }
}
