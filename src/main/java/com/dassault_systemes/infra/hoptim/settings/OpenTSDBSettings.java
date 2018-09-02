package com.dassault_systemes.infra.hoptim.settings;

import java.util.HashMap;

/**
 * Created by ERL1 on 6/6/2016.
 */
public class OpenTSDBSettings {
    private String driverClassName;
    private String tsdbTable;
    private String tsdbUidTable;
    private String apiUrl;
    private int scoreGainThreshold;
    private long schedulerPeriod;
    private long schedulerDelay;
    private HashMap<String, Double> vipMetrics;
    private double regionServerFillFactor;
    private int delayedExecutionTime;

    public String getTsdbTable() {
        return tsdbTable;
    }

    public void setTsdbTable(String tsdbTable) {
        this.tsdbTable = tsdbTable;
    }

    public String getTsdbUidTable() {
        return tsdbUidTable;
    }

    public void setTsdbUidTable(String tsdbUidTable) {
        this.tsdbUidTable = tsdbUidTable;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public HashMap<String, Double> getVipMetrics() {
        return vipMetrics;
    }

    public void setVipMetrics(HashMap<String, Double> vipMetrics) {
        this.vipMetrics = vipMetrics;
    }

    public double getRegionServerFillFactor() {
        return regionServerFillFactor;
    }

    public void setRegionServerFillFactor(double regionServerFillFactor) {
        this.regionServerFillFactor = regionServerFillFactor;
    }

    public int getDelayedExecutionTime() {
        return delayedExecutionTime;
    }

    public void setDelayedExecutionTime(int delayedExecutionTime) {
        this.delayedExecutionTime = delayedExecutionTime;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public int getScoreGainThreshold() {
        return scoreGainThreshold;
    }

    public void setScoreGainThreshold(int scoreGainThreshold) {
        this.scoreGainThreshold = scoreGainThreshold;
    }

    public long getSchedulerPeriod() {
        return schedulerPeriod;
    }

    public void setSchedulerPeriod(int schedulerPeriod) {
        this.schedulerPeriod = schedulerPeriod;
    }

    public long getSchedulerDelay() {
        return schedulerDelay;
    }

    public void setSchedulerDelay(long schedulerDelay) {
        this.schedulerDelay = schedulerDelay;
    }
}
