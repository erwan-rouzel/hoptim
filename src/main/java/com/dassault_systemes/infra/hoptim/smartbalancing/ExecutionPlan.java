package com.dassault_systemes.infra.hoptim.smartbalancing;

import com.dassault_systemes.infra.hoptim.hbase.RegionView;
import com.dassault_systemes.infra.hoptim.log.CustomLogger;
import com.dassault_systemes.infra.hoptim.settings.SettingsFacade;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ERL1 on 5/10/2016.
 */
public class ExecutionPlan {
    public HashMap<String, ArrayList<RegionView>> currentRegionBalancing;
    public HashMap<String, ArrayList<RegionView>> newRegionBalancing;
    public ArrayList<ClusterOperation> operations;
    public boolean isScoreImplemented;
    public int scoreGainPercentage;
    private int nextOperationIndex;
    public int scoreBefore;
    public int scoreAfter;

    public ExecutionPlan() {
        currentRegionBalancing = new HashMap<String, ArrayList<RegionView>>();
        newRegionBalancing = new HashMap<String, ArrayList<RegionView>>();
        operations = new ArrayList<ClusterOperation>();
        nextOperationIndex = 0;
        scoreGainPercentage = 0;
        isScoreImplemented = false;
    }

    public void exectuteAll() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, InterruptedException, InvocationTargetException, NoSuchMethodException {
        for(ClusterOperation operation: operations) {
            operation.execute();
            nextOperationIndex++;
        }
    }
    public boolean exectuteNext() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException, InterruptedException, InvocationTargetException, NoSuchMethodException {
        CustomLogger.debug(this, "exectuteNext");
        if(nextOperationIndex < operations.size()) {
            operations.get(nextOperationIndex).execute();
            nextOperationIndex++;
            return true;
        } else {
            return false;
        }
    }

    public int getProgressionPourcentage() {
        return (nextOperationIndex == 0)?0:(100*nextOperationIndex / operations.size());
    }

    public boolean isWorthExecuting() throws IOException {
        int scoreGainThreshold = SettingsFacade.getInstance().opentsdb.getScoreGainThreshold();
        CustomLogger.debug(this, "Test if worth executing if scoreGainPercentage (" + scoreGainPercentage + ")  >= scoreGainThreshold (" + scoreGainThreshold + ")");
        return (!isScoreImplemented) || (scoreGainPercentage >= scoreGainThreshold);
    }
}
