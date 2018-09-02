package com.dassault_systemes.infra.hoptim.opentsdb;

import com.dassault_systemes.infra.hoptim.util.BytesHelper;

/**
 * Represents the raw version (as bytes array) of a tag.
 *
 * Created by ERL1 on 4/21/2016.
 */
public class OpenTSDBTagRaw {
    private byte[] tagk;
    private byte[] tagv;

    public OpenTSDBTagRaw(byte[] tagk, byte[] tagv) {
        this.tagk = tagk;
        this.tagv = tagv;
    }

    public String getTagkAsHex() {
        return BytesHelper.bytesToHex(tagk);
    }

    public String getTagvAsHex() {
        return BytesHelper.bytesToHex(tagv);
    }

    public byte[] getTagk() {
        return tagk;
    }

    public byte[] getTagv() {
        return tagv;
    }

    @Override
    public String toString() {
        return "(" + getTagkAsHex() + ", " + getTagvAsHex() + ")";
    }
}
