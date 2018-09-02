package com.dassault_systemes.infra.hoptim.smartbalancing;

import com.dassault_systemes.infra.hoptim.hbase.HBaseFacade;
import com.dassault_systemes.infra.hoptim.hbase.RegionServerView;
import com.dassault_systemes.infra.hoptim.hbase.RegionView;
import com.dassault_systemes.infra.hoptim.log.CustomLogger;
import com.dassault_systemes.infra.hoptim.opentsdb.SplitRecommandation;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain.RegionBalance;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain.RegionEntity;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain.RegionServerEntity;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.score.RegionBalancingEasyScoreCalculator;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.internal.inject.Custom;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.domain.ScanAnnotatedClassesConfig;
import org.optaplanner.core.config.score.definition.ScoreDefinitionType;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ERL1 on 5/31/2016.
 */
public class OptaPlannerBalancingAlgorithm implements IBalancingAlgorithm {
    private int timeout;
    private int moveMax;
    private int moveWeight;
    private int sizeWeight;
    private int readWeight;
    private int writeWeight;
    private ArrayList<String> tablesSelection;

    public OptaPlannerBalancingAlgorithm(String jsonConfig) throws JSONException {
        //CustomLogger.debug(this, jsonConfig);
        tablesSelection = new ArrayList<String>();
        JSONObject obj = new JSONObject(jsonConfig);
        timeout = obj.getInt("timeout");
        moveMax = obj.getInt("move_max");
        moveWeight = obj.getInt("move_weight");
        sizeWeight = obj.getInt("size_weight");
        readWeight = obj.getInt("read_weight");
        writeWeight = obj.getInt("write_weight");

        JSONArray jsonTablesSelection = obj.getJSONArray("tables_selection");
        for(int i = 0; i < jsonTablesSelection.length(); i++) {
            tablesSelection.add(jsonTablesSelection.getString(i));
        }
    }

    public ExecutionPlan getExecutionPlan() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException {
        ArrayList<RegionView> regionsList = new ArrayList<RegionView>();
        HashMap<Integer, String> regionsServersIndexMapping = new HashMap<Integer, String>();
        HashMap<Integer, String> regionsIndexMapping = new HashMap<Integer, String>();
        HashMap<String, ArrayList<RegionView>> currentRegionsDistribution = HBaseFacade.getInstance().getRegionsDistributionPerRegionServer();
        ArrayList<RegionServerView> regionServersList = HBaseFacade.getInstance().getRegionServers();
        HashMap<String, ArrayList<RegionView>> newRegionServersList = new HashMap<String, ArrayList<RegionView>>();
        SplitRecommandation splitRecommandation = new SplitRecommandation();

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
        for(Map.Entry<String, ArrayList<RegionView>> entry : currentRegionsDistribution.entrySet()) {
            String key = entry.getKey();
            ArrayList<RegionView> value = entry.getValue();

            regionsServersIndexMapping.put(index, key);
            regionsList.addAll(value);
            index++;
        }

        ExecutionPlan plan = new ExecutionPlan();

        // Build the Solver
        // SolverFactory<RegionBalance> solverFactory = SolverFactory.createFromXmlResource("regionBalancingSolverConfig.xml");
        //
        // We don't user the file configuration for Opta Planner, but instead a custom configuration through API.
        //
        // See reference :
        // https://docs.jboss.org/drools/release/6.0.0.Beta2/optaplanner-docs/html/plannerConfiguration.html

        SolverConfig solverConfig = new SolverConfig();
        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig = new ScoreDirectorFactoryConfig();
        scoreDirectorFactoryConfig.setScoreDefinitionType(ScoreDefinitionType.HARD_SOFT);
        scoreDirectorFactoryConfig.setInitializingScoreTrend("ONLY_DOWN");
        scoreDirectorFactoryConfig.setEasyScoreCalculatorClass(RegionBalancingEasyScoreCalculator.class);
        solverConfig.setScoreDirectorFactoryConfig(scoreDirectorFactoryConfig);

        ScanAnnotatedClassesConfig scanAnnotatedClassesConfig = new ScanAnnotatedClassesConfig();
        ArrayList packageInclude = new ArrayList();
        packageInclude.add("com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner");
        scanAnnotatedClassesConfig.setPackageIncludeList(packageInclude);
        solverConfig.setScanAnnotatedClassesConfig(scanAnnotatedClassesConfig);

        TerminationConfig terminationConfig = new TerminationConfig();
        terminationConfig.setSecondsSpentLimit((long)timeout);
        solverConfig.setTerminationConfig(terminationConfig);

        Solver<RegionBalance> solver = solverConfig.buildSolver();
        RegionBalance unsolvedRegionBalance = new RegionBalance();
        unsolvedRegionBalance.setMoveMax(moveMax);
        unsolvedRegionBalance.setMoveWeight(moveWeight);
        unsolvedRegionBalance.setSizeWeight(sizeWeight);
        unsolvedRegionBalance.setReadWeight(readWeight);
        unsolvedRegionBalance.setWriteWeight(writeWeight);
        unsolvedRegionBalance.setTablesSelection(tablesSelection);

        ArrayList<RegionEntity> regionEntityList = new ArrayList();
        ArrayList<RegionServerEntity> regionServerEntityList = new ArrayList();

        for(RegionServerView regionServerView: regionServersList) {
            RegionServerEntity regionServerEntity = new RegionServerEntity();
            regionServerEntity.setId(regionServerView.id);
            regionServerEntity.setDiskSpace(regionServerView.diskSpaceCapacityGB);
            regionServerEntity.setWriteRequests(regionServerView.writeRequestsDelta);
            regionServerEntity.setReadRequests(regionServerView.readRequestsDelta);
            regionServerEntity.setRegionServerView(regionServerView);
            regionServerEntityList.add(regionServerEntity);

            for(RegionView region: regionServerView.regions) {
                RegionEntity regionEntity = new RegionEntity();
                regionEntity.setRegionServer(regionServerEntity);
                regionEntity.setId(region.id);
                regionEntity.setRequiredDiskSpace(region.sizeMB);
                regionEntity.setRequiredWriteRequests(region.writeRequestsDelta);
                regionEntity.setRequiredReadRequests(region.readRequestsDelta);
                regionEntity.setRegionView(region);
                regionEntityList.add(regionEntity);
            }
        }

        unsolvedRegionBalance.setRegionList(regionEntityList);
        unsolvedRegionBalance.setRegionServerList(regionServerEntityList);
        unsolvedRegionBalance.setMaxNumberOfRegionsPerRS(splitRecommandation.maxNumberOfRegionsPerRegionServer);

        RegionBalancingEasyScoreCalculator scoreCalculator = new RegionBalancingEasyScoreCalculator();

        // Solve the problem
        HardSoftScore scoreBefore = scoreCalculator.calculateScore(unsolvedRegionBalance);
        RegionBalance solvedRegionBalance = solver.solve(unsolvedRegionBalance);
        HardSoftScore scoreAfter = scoreCalculator.calculateScore(solvedRegionBalance);

        CustomLogger.debug(this, "SCORE_BEFORE=" + scoreBefore.getSoftScore());
        CustomLogger.debug(this, "SCORE_AFTER=" + scoreAfter.getSoftScore());

        StringBuilder displayString = new StringBuilder();
        for (RegionEntity regionEntity : solvedRegionBalance.getRegionList()) {
            RegionServerEntity regionServer = regionEntity.getRegionServer();
            RegionView regionView = regionEntity.getRegionView();

            String regionServerKey = regionServer.getRegionServerView().name;

            if(! newRegionServersList.containsKey(regionServerKey)) {
                newRegionServersList.put(regionServerKey, new ArrayList<RegionView>());
            }

            newRegionServersList.get(regionServerKey).add(regionView);

            if(! regionView.currentRegionServer.equals(regionServerKey)) {
                if(tablesSelection.contains(regionView.tableName)) {
                    RegionMove move = new RegionMove();
                    move.encodedRegionName = regionView.encodedName;
                    move.fromRegionServer = regionView.currentRegionServer;
                    move.toRegionServer = regionServerKey;
                    move.regionName = regionView.regionName;
                    plan.operations.add(move);
                }
            }
        }

        plan.scoreBefore = scoreBefore.getSoftScore();
        plan.scoreAfter = scoreAfter.getSoftScore();

        if(scoreAfter.getSoftScore() == 0) {
            CustomLogger.debug(this, "Computing score gain (score after is 0) -> use Integer.MAX_VALUE for score gain");
            plan.scoreGainPercentage = Integer.MAX_VALUE;
        } else {
            CustomLogger.debug(this, "Computing score gain (score after is not 0)");
            plan.scoreGainPercentage = (int) ((((double) scoreBefore.getSoftScore() - (double) scoreAfter.getSoftScore()) / (double) scoreAfter.getSoftScore()) * 100.0);
        }

        plan.currentRegionBalancing = currentRegionsDistribution;
        plan.newRegionBalancing = newRegionServersList;
        plan.isScoreImplemented = true;

        CustomLogger.debug(this, "SCORE_GAIN=" + plan.scoreGainPercentage);

        return plan;
    }

    private boolean isTableSelected(String tableName) {
        return tablesSelection.contains(tableName);
    }
}
