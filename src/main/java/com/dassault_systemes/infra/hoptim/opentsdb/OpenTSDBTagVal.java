package com.dassault_systemes.infra.hoptim.opentsdb;

/**
 * Represents a couple tag/value for OpenTSDB.
 *
 * Created by ERL1 on 4/21/2016.
 */
public class OpenTSDBTagVal {
    private String tagk;
    private String tagv;

    public OpenTSDBTagVal(String tagk, String tagv) {
        this.tagk = tagk;
        this.tagv = tagv;
    }

    public String getTagk() {
        return tagk;
    }

    public String getTagv() {
        return tagv;
    }

    @Override
    public String toString() {
        return "(" + getTagk() + ", " + getTagv() + ")";
    }
}
