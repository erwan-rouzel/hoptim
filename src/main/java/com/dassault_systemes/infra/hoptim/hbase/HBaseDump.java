package com.dassault_systemes.infra.hoptim.hbase;

import com.dassault_systemes.infra.hoptim.log.CustomLogger;
import com.dassault_systemes.infra.hoptim.settings.SettingsFacade;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by erl1 on 9/19/2016.
 */
class HBaseDump {
    private HashMap<String, ArrayList<RegionView>> dumpedRegionsDistribution;
    private long latestLoadedDumpTimestamp;
    private String dumpFileName;
    private String dumpUrl;
    private File dumpFile;

    public HBaseDump() throws IOException {
        dumpFileName = SettingsFacade.getInstance().hbase.getDumpFileName();
        dumpUrl = SettingsFacade.getInstance().hbase.getDumpUrl();
        dumpFile = new File(dumpFileName);
        loadDump();
    }

    public void loadDump() {
        try {
            Type REVIEW_TYPE = new TypeToken<HashMap<String,ArrayList<RegionView>>>() {
            }.getType();
            Gson gson = new Gson();
            String fileContent = new Scanner(dumpFile).useDelimiter("\\Z").next();
            fileContent = fileContent.substring(1, fileContent.length() - 1).replaceAll("\\\\\"", "\"");
            CustomLogger.debug(this, "===================================");
            CustomLogger.debug(this, "fileContent="+fileContent);
            //fileContent = fileContent.substring(1, fileContent.length() - 1);
            JsonReader reader = new JsonReader(new StringReader(fileContent));
            dumpedRegionsDistribution = gson.fromJson(reader, REVIEW_TYPE);
            // DST added these part in order to better debug in case of ...
            /*
            for (String key : dumpedRegionsDistribution.keySet()) {
                CustomLogger.debug(this, "0000000000000000000000000000000000000000");
                CustomLogger.debug(this, "regionServerName key in load dump : " + key);
            }
            for (Map.Entry<String, ArrayList<RegionView>> entry : dumpedRegionsDistribution.entrySet()) {
                CustomLogger.debug(this, "New Region Server Dump =========================");
                ArrayList<RegionView> tempregions = entry.getValue();
                for(RegionView regionView: tempregions) {
                    CustomLogger.debug(this, "1111111111111111111111111111111111111111111");
                    CustomLogger.debug(this, "Dump regionencodedName : " + regionView.encodedName);
                }
            }
            */

        } catch (FileNotFoundException e) {
            dumpedRegionsDistribution = new HashMap<String, ArrayList<RegionView>>();
            e.printStackTrace();
        }

        latestLoadedDumpTimestamp = latestDumpTimestamp();
    }

    public boolean updateDump() {
        try {
            URL url = null;
            url = new URL(dumpUrl);
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(dumpFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        } catch (UnsupportedEncodingException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        loadDump();
        return true;
    }

    public long latestDumpTimestamp() {
        if(dumpFile != null) {
            return dumpFile.lastModified();
        } else {
            return 0;
        }
    }

    public int getDumpedReadRequestsCountForRegionServer(String regionServerName) {
        ArrayList<RegionView> regionViews = dumpedRegionsDistribution.get(regionServerName);
        int result = 0;

        if(regionViews != null) {
            for (RegionView regionView : regionViews) {
                result += regionView.readRequestsCount;
            }
        }

        return result;
    }

    public int getDumpedWriteRequestsCountForRegionServer(String regionServerName) {
        ArrayList<RegionView> regionViews = dumpedRegionsDistribution.get(regionServerName);
        int result = 0;

        if(regionViews != null) {
            for (RegionView regionView : regionViews) {
                result += regionView.writeRequestsCount;
            }
        }

        return result;
    }

    public long getDumpedReadRequestsCountForRegion(String encodedName) {
        RegionView regionView = getRegion(encodedName);
        return (regionView != null)?regionView.readRequestsCount:0;
    }

    public long getDumpedWriteRequestsCountForRegion(String encodedName) {
        RegionView regionView = getRegion(encodedName);
        return (regionView != null)?regionView.writeRequestsCount:0;
    }

    public HashMap<String, ArrayList<RegionView>> getDumpedRegionsDistribution() {
        return dumpedRegionsDistribution;
    }

    private ArrayList<RegionView> getRegions() {
        ArrayList<RegionView> regions = new ArrayList<RegionView>();

        for (Map.Entry<String, ArrayList<RegionView>> entry : dumpedRegionsDistribution.entrySet()) {
            regions.addAll(entry.getValue());
        }

        return regions;
    }

    private RegionView getRegion(String encodedName) {
        for(RegionView regionView: getRegions()) {
            if(regionView.encodedName.equals(encodedName)) {
                // CustomLogger.debug(this, "===================================");
                // CustomLogger.debug(this, "Search regionencodedName : " + encodedName + " found " + regionView.encodedName);
                return regionView;
            }
        }
        // CustomLogger.debug(this, "+++++++++++++++++++++++++++++++++++++++++");
        // CustomLogger.debug(this, "regionencodedName : " + encodedName + " not found ");
        return null;
    }
}
