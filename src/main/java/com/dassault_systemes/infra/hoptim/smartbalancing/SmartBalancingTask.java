package com.dassault_systemes.infra.hoptim.smartbalancing;

import com.dassault_systemes.infra.hoptim.hbase.HBaseFacade;
import com.dassault_systemes.infra.hoptim.log.CustomLogger;
import com.dassault_systemes.infra.hoptim.taskmanager.Task;
import org.glassfish.jersey.internal.inject.Custom;

/**
 * Created by ERL1 on 5/23/2016.
 */
public class SmartBalancingTask extends Task {
    private ExecutionPlan plan;
    private IBalancingAlgorithm balancingAlgorithm;
    private long lastScoreGainChecked;
    private boolean isWorthExecuting;
    private long numberOfChecks;
    private boolean isPeriodic;

    public SmartBalancingTask(String name, int executionDelayInMinutes, IBalancingAlgorithm balancingAlgorithm, long delay, long period)
    {
        this(name, executionDelayInMinutes, balancingAlgorithm);
        this.period = period;
        this.delay = delay;
        this.isPeriodic = true;
    }

    public SmartBalancingTask(String name, int executionDelayInSeconds, IBalancingAlgorithm balancingAlgorithm)
    {
        this.name = name;
        this.totalExecutionDelay = executionDelayInSeconds*1000;
        this.balancingAlgorithm = balancingAlgorithm;
        this.period = 0;
        this.delay = 0;
        this.isPeriodic = false;
    }

    public String getName() {
        return name;
    }

    public void updateProgression() {
        this.progression = plan.getProgressionPourcentage();
    }

    public void run()
    {
        try
        {
            CustomLogger.debug(this, "Run task: " + this.getName());
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            final int sleepPerOperation;

            plan = balancingAlgorithm.getExecutionPlan();
            lastScoreGainChecked = plan.scoreGainPercentage;
            isWorthExecuting = plan.isWorthExecuting();

            if(isWorthExecuting) {
                CustomLogger.debug(this, "Task is worth executing");
                numberOfChecks = 0;

                if (plan.operations.size() > 0) {
                    sleepPerOperation = totalExecutionDelay / plan.operations.size();
                } else {
                    //TODO : mettre cette valeur par défaut en settings
                    sleepPerOperation = 10*1000;
                }

                CustomLogger.debug(this, "Executing plan each step one by one with sleepPerOperation=" + sleepPerOperation);
                while (plan.exectuteNext()) {
                    updateProgression();
                    CustomLogger.debug(this, "Sleeping for " + sleepPerOperation/1000 + " seconds");
                    Thread.sleep(sleepPerOperation);
                }
            } else {
                CustomLogger.debug(this, "Task is not worth executing");
                numberOfChecks++;
            }
        }
        catch (Exception e)
        {
            //TODO : debug exception
            e.printStackTrace();
        }
    }
}
