package com.dassault_systemes.infra.hoptim.settings;

import com.dassault_systemes.infra.hoptim.log.CustomLogger;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by ERL1 on 5/25/2016.
 */
public class SettingsFacade {
    public OpenTSDBSettings opentsdb;
    public HBaseSettings hbase;
    private static SettingsFacade instance;

    private SettingsFacade() throws IOException {
        CustomLogger.debug(this, "Init: START");
        this.load();
        CustomLogger.debug(this, "Init: DONE");
    }

    public static SettingsFacade getInstance() throws IOException {
        if(instance == null) {
            instance = new SettingsFacade();
        }

        return instance;
    }

    public void load() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        opentsdb = mapper.readValue(new File(getSettingsFilePath("opentsdb-settings.yaml")), OpenTSDBSettings.class);
        hbase = mapper.readValue(new File(getSettingsFilePath("hbase-settings.yaml")), HBaseSettings.class);
    }

    private String getSettingsFilePath(String file) {
        try {
            String absolutePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
            absolutePath = absolutePath.replaceAll("%20"," "); // Surely need to do this here
            return absolutePath + "/" + file;
        } catch (Exception e) {
            return "";
        }
    }
}
