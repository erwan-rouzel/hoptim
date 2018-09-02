/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.score;

import com.dassault_systemes.infra.hoptim.hbase.RegionView;
import com.dassault_systemes.infra.hoptim.log.CustomLogger;
import com.dassault_systemes.infra.hoptim.smartbalancing.RegionMove;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain.RegionEntity;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain.RegionBalance;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain.RegionServerEntity;

import java.util.ArrayList;

public class RegionBalancingEasyScoreCalculator implements EasyScoreCalculator<RegionBalance> {

    /**
     * A very simple implementation. The double loop can easily be removed by using Maps.
     */
    public HardSoftScore calculateScore(RegionBalance regionBalance) {
        int hardScore = 0;
        int softScore = 0;
        int numRegionServers = regionBalance.getRegionServerList().size();
        int numberOfRegionsForRS = 0;
        int numberOfMoves = calcultateNumberOfMoves(regionBalance);
        int maxNumberOfMoves = calcultateMaxNumberOfMoves(regionBalance);

        long[] diskSpaceUsageByRS = new long[numRegionServers];
        long[] readRequestsByRS = new long[numRegionServers];
        long[] writeRequestsByRS = new long[numRegionServers];
        long maxDiskSpace = 0;
        long maxReadRequests = 0;
        long maxWriteRequests = 0;
        boolean tooMuchRegionsForRS = false;
        boolean tooMuchMoves = false;

        tooMuchMoves = (numberOfMoves > regionBalance.getMoveMax());
        //CustomLogger.debug(this, "numberOfMoves=" + numberOfMoves);
        //CustomLogger.debug(this, "regionBalance.getMoveMax()=" + regionBalance.getMoveMax());
        //CustomLogger.debug(this, "maxNumberOfMoves=" + maxNumberOfMoves);

        for (RegionServerEntity regionServer : regionBalance.getRegionServerList()) {
            for (RegionEntity region : regionBalance.getRegionList()) {
                if(regionBalance.getTablesSelection().contains(region.getRegionView().tableName)) {
                    maxDiskSpace = Math.max(maxDiskSpace, region.getRequiredDiskSpace());
                    maxReadRequests = Math.max(maxReadRequests, region.getRequiredReadRequests());
                    maxWriteRequests = Math.max(maxWriteRequests, region.getRequiredWriteRequests());
                }
            }
        }

        maxDiskSpace = (maxDiskSpace > 0)?maxDiskSpace:1;
        maxReadRequests = (maxReadRequests > 0)?maxReadRequests:1;
        maxWriteRequests = (maxWriteRequests > 0)?maxWriteRequests:1;

        for (RegionServerEntity regionServer : regionBalance.getRegionServerList()) {
            int i = regionServer.getId().intValue();
            numberOfRegionsForRS = 0;

            // Calculate usage
            for (RegionEntity region : regionBalance.getRegionList()) {
                if (regionServer.equals(region.getRegionServer())) {
                    if(regionBalance.getTablesSelection().contains(region.getRegionView().tableName)) {
                        diskSpaceUsageByRS[i] += region.getRequiredDiskSpace();
                        readRequestsByRS[i] += region.getRequiredReadRequests();
                        writeRequestsByRS[i] += region.getRequiredWriteRequests();
                        numberOfRegionsForRS++;
                    }
                }
            }

            if(i > 0) {
                tooMuchRegionsForRS = tooMuchRegionsForRS || (numberOfRegionsForRS > regionBalance.getMaxNumberOfRegionsPerRS());
                softScore -= regionBalance.getMoveWeight()  * 1000 * numberOfMoves                                                 / maxNumberOfMoves;
                softScore -= regionBalance.getSizeWeight()  * 1000 * Math.abs( diskSpaceUsageByRS[i] - diskSpaceUsageByRS[i - 1] ) / maxDiskSpace;
                softScore -= regionBalance.getReadWeight()  * 1000 * Math.abs( readRequestsByRS[i]   - readRequestsByRS  [i - 1] ) / maxReadRequests;
                softScore -= regionBalance.getWriteWeight() * 1000 * Math.abs( writeRequestsByRS[i]  - writeRequestsByRS [i - 1] ) / maxWriteRequests;
            }
        }

        hardScore -= (tooMuchRegionsForRS)?1000:0;
        hardScore -= (tooMuchMoves)?1000:0;

        return HardSoftScore.valueOf(hardScore, softScore);
    }

    private int calcultateMaxNumberOfMoves(RegionBalance solvedRegionBalance) {
        int totalNumberOfRegions = 0;
        for (RegionEntity regionEntity : solvedRegionBalance.getRegionList()) {
            totalNumberOfRegions++;
        }
        return totalNumberOfRegions;
    }

    private int calcultateNumberOfMoves(RegionBalance solvedRegionBalance) {
        int numberOfMoves = 0;

        for (RegionEntity regionEntity : solvedRegionBalance.getRegionList()) {
            RegionServerEntity regionServer = regionEntity.getRegionServer();
            RegionView regionView = regionEntity.getRegionView();

            String regionServerKey = regionServer.getRegionServerView().name;

            if(! regionView.currentRegionServer.equals(regionServerKey)) {
                if(solvedRegionBalance.getTablesSelection().contains(regionView.tableName)) {
                    numberOfMoves++;
                }
            }
        }

        return numberOfMoves;
    }
}
