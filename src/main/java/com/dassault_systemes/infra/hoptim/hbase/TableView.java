package com.dassault_systemes.infra.hoptim.hbase;

import java.util.ArrayList;

/**
 * Created by ERL1 on 5/11/2016.
 */
public class TableView {
    public String tableName;
    public ArrayList<RegionView> tableRegions;

    public TableView() {
        tableName = "";
        tableRegions = new ArrayList<RegionView>();
    }
}
