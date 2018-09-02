package com.dassault_systemes.infra.hoptim.taskmanager;

/**
 * Created by ERL1 on 5/23/2016.
 */
public abstract class Task implements Runnable {
    protected long period;
    protected long delay;
    protected int progression;
    protected String name;
    protected int totalExecutionDelay;

    public Task() {
        name = "";
        totalExecutionDelay = 0;
    }

    public Task(String name, int executionDelayInMinutes)
    {
        this.name = name;
        this.totalExecutionDelay = executionDelayInMinutes*60*1000;
    }

    public String getName() {
        return name;
    }

    public abstract void updateProgression();

    public abstract void run();
}
