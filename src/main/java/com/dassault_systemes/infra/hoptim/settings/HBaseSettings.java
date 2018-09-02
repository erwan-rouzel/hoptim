package com.dassault_systemes.infra.hoptim.settings;

/**
 * Created by ERL1 on 6/6/2016.
 */
public class HBaseSettings {
    private String regionServerMemory;
    private String driverClassName;
    private String zookeeperQuorum;
    private String restApiPort;
    private String dumpFileName;
    private String dumpUrl;
    private String version;
    private String name;

    public String getRegionServerMemory() {
        return regionServerMemory;
    }

    public void setRegionServerMemory(String regionServerMemory) {
        this.regionServerMemory = regionServerMemory;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getZookeeperQuorum() {
        return zookeeperQuorum;
    }

    public void setZookeeperQuorum(String zookeeperQuorum) {
        this.zookeeperQuorum = zookeeperQuorum;
    }

    public String getRestApiPort() {
        return restApiPort;
    }

    public void setRestApiPort(String restApiPort) {
        this.restApiPort = restApiPort;
    }

    public String getDumpFileName() {
        return dumpFileName;
    }

    public void setDumpFileName(String dumpFileName) {
        this.dumpFileName = dumpFileName;
    }

    public String getDumpUrl() {
        return dumpUrl;
    }

    public void setDumpUrl(String dumpUrl) {
        this.dumpUrl = dumpUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
