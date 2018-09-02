package com.dassault_systemes.infra.hoptim.taskmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.*;

/**
 * Created by ERL1 on 5/23/2016.
 */
public class CustomThreadPoolExecutor extends ScheduledThreadPoolExecutor {
    private ArrayList<Runnable> activeTasks;

    public CustomThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize);
        activeTasks = new ArrayList<Runnable>();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        activeTasks.add(r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        activeTasks.remove(r);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(command);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) throws NullPointerException {
        ScheduledFuture<?> scheduledFuture = super.scheduleAtFixedRate(command, initialDelay, period, unit);
        return scheduledFuture;
    }

    public ArrayList<Runnable> getActiveTasks() {
        ArrayList<Runnable> activeRealTasks = new ArrayList<Runnable>();
        for(Runnable task: activeTasks) {
            activeRealTasks.add((Runnable)JobDiscoverer.findRealTask(task));
        }
        return activeRealTasks;
    }
}
