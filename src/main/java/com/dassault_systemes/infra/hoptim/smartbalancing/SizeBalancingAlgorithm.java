package com.dassault_systemes.infra.hoptim.smartbalancing;

import com.dassault_systemes.infra.hoptim.hbase.HBaseFacade;
import com.dassault_systemes.infra.hoptim.hbase.RegionView;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.dynamicprogramming.DynamicProgrammingPartitionProblemSolver;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ERL1 on 5/31/2016.
 */
public class SizeBalancingAlgorithm implements IBalancingAlgorithm {
    public SizeBalancingAlgorithm(String jsonConfig) {
        //Nothing as of now
    }

    /**
     * This methods compute the execution plan for doing a smart balancing. We are using here the "Partition Problem"
     * algorithm which allows, given a list of numbers, to produce the optimal partitionning of this list into N partitions
     * so that the sum of numbers into each partitions is as close as possible.
     *
     * See wikipedia article for reference :
     * https://en.wikipedia.org/wiki/Partition_problem
     *
     * In our case the list of numbers correspond to the size of regions and the N partitions correspond to the N
     * region servers.
     *
     * @return The computed execution plan
     * @throws IOException
     */
    public ExecutionPlan getExecutionPlan() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        ArrayList<RegionView> regionsList = new ArrayList<RegionView>();
        HashMap<Integer, String> regionsServersIndexMapping = new HashMap<Integer, String>();
        HashMap<String, ArrayList<RegionView>> currentRegionServersDistribution = HBaseFacade.getInstance().getRegionsDistributionPerRegionServer();
        HashMap<String, ArrayList<RegionView>> newRegionServersList = new HashMap<String, ArrayList<RegionView>>();

        /*
         We retrieve the list of region servers from getRegionsDistributionPerRegionServer method as a HashMap, which key is
         the region server name and value is the list of region on this region server :

         Region Server 1 => (region11, region12, ..., region 1n)
         Region Server 2 => (region21, region 22, ..., region 2m)
         etc.

         We need to make a mapping between the region server names and a numeric index inside regionsServersIndexMapping,
         because the partition problem algorithm uses numeric index only.

         We also keep a list of regions inside a flat list regionsList as this is what is needed for the PP algorithm.
        */
        int index = 0;
        for(Map.Entry<String, ArrayList<RegionView>> entry : currentRegionServersDistribution.entrySet()) {
            String key = entry.getKey();
            ArrayList<RegionView> value = entry.getValue();

            regionsServersIndexMapping.put(index, key);
            regionsList.addAll(value);
            index++;
        }

        ExecutionPlan plan = new ExecutionPlan();
        DynamicProgrammingPartitionProblemSolver solution = new DynamicProgrammingPartitionProblemSolver();

        /*
         We need to convert the ArrayList of regions to a primitive array as this is the input expected by the PP algorithm.
        */
        RegionView[] regionsListArray = new RegionView[regionsList.size()];
        regionsList.toArray(regionsListArray);

        /*
         This is where we actually run the algorithm over the list of regions and get the result inside an int array.
         The results correspond to the index in the list of regions where each partition ends.

         The algorithm does not provide last index but this is more convenient for us to parse the result
         to have this inside the same array. So we add the end index at the end of the result array.

         So our result array finally contains :
         (endIndexPartition_1, endIndexPartition_2, ..., endIndexPartition_N-1, endIndexPartition_N)
        */
        int[] result = solution.solve(regionsListArray, currentRegionServersDistribution.size());
        int[] resultWithEnd = new int[result.length + 1];
        System.arraycopy(result, 0, resultWithEnd, 0, result.length);
        resultWithEnd[result.length] = regionsListArray.length;


        /*
         Finally we parse the result array and build accordingly the move operations for the execution plan
         */
        int indexStart = 0;
        int indexEnd = 0;
        int numPartition = 0;
        for(int region: resultWithEnd) {
            int sum = 0;
            indexEnd = resultWithEnd[numPartition];
            for(int i = indexStart; i < indexEnd; i++) {
                String regionServerKey = regionsServersIndexMapping.get(numPartition);

                if(! newRegionServersList.containsKey(regionServerKey)) {
                    newRegionServersList.put(regionServerKey, new ArrayList<RegionView>());
                }

                newRegionServersList.get(regionServerKey).add(regionsListArray[i]);

                if(! regionsListArray[i].currentRegionServer.equals(regionServerKey)) {
                    RegionMove move = new RegionMove();
                    move.encodedRegionName = regionsListArray[i].encodedName;
                    move.fromRegionServer = regionsListArray[i].currentRegionServer;
                    move.toRegionServer = regionServerKey;
                    move.regionName = regionsListArray[i].regionName;

                    plan.operations.add(move);
                }
            }

            indexStart = resultWithEnd[numPartition];
            numPartition++;
        }

        plan.currentRegionBalancing = currentRegionServersDistribution;
        plan.newRegionBalancing = newRegionServersList;

        return plan;
    }
}
