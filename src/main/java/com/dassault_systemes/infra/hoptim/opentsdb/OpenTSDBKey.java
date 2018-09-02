package com.dassault_systemes.infra.hoptim.opentsdb;

import com.dassault_systemes.infra.hoptim.util.BytesHelper;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Represents in a more human friendly way the TSDB Keys, which respect this raw format of bytes :
 *
 * 00000150E22700000001000001000002000004
 * '----''------''----''----''----''----'
 * metric  time   tagk  tagv  tagk  tagv
 *
 * See doc. :
 * http://opentsdb.net/docs/build/html/user_guide/backends/hbase.html
 *
 * Created by ERL1 on 4/21/2016.
 */
public class OpenTSDBKey {
    public static OpenTSDBKey SMALLEST_KEY = new OpenTSDBKey(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
    public static OpenTSDBKey BIGGEST_KEY = new OpenTSDBKey(new byte[]{0x7F, 0x7F, 0x7F, 0x00, 0x00, 0x00, 0x00});
    public String rawKeyAsHex;
    public String metricAsHex;
    public String timeAsString;
    public String timeAsHex;

    /* These fields are marked as transient because we don't want to encode them in Json : */
    private transient byte[] rawKey;
    private transient byte[] metric;
    private transient byte[] time;
    private transient byte[][] tagk;
    private transient byte[][] tagv = new byte[3][];

    public OpenTSDBKey(byte[] rawKey) {
        this.rawKey = rawKey;
        metric = new byte[3];
        time = new byte[4];

        if(rawKey.length == 0) {
            Arrays.fill(metric, (byte)0);
            Arrays.fill(time, (byte)0);
            tagk = new byte[1][3];
            tagv = new byte[1][3];
            Arrays.fill(tagk[0], (byte)0);
            Arrays.fill(tagv[0], (byte)0);
        } else {
            metric = Arrays.copyOfRange(rawKey, 0, 3);
            time = Arrays.copyOfRange(rawKey, 3, 7);

            // To get length of tags, we remove 3 bytes of metric, 4 bytes of time
            // => This gives a multiple of 6 bytes (3 for tagk and 3 for tagv) so we divide by 6
            int numberOfTags = (rawKey.length - 7) / 6;

            tagk = new byte[numberOfTags][3];
            tagv = new byte[numberOfTags][3];

            for (int i = 0; i < numberOfTags; i++) {
                int indexTagk = 7 + 6 * i;
                int indexTagv = indexTagk + 3;
                tagk[i] = Arrays.copyOfRange(rawKey, indexTagk, indexTagk + 3);
                tagv[i] = Arrays.copyOfRange(rawKey, indexTagv, indexTagv + 3);
            }
        }

        rawKeyAsHex = getRawKeyAsHex();
        metricAsHex = getMetricAsHex();

        ByteBuffer timeWrapped = ByteBuffer.wrap(time); // big-endian by default
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        timeAsString = dateFormat.format(timeWrapped.getInt());

        timeAsHex = getTimeAsHex();
    }

    public boolean isNull() {
        return (rawKey.length == 0);
    }

    public byte[] getRawKeyAsBytes() {
        return rawKey;
    }

    public byte[] getMetricAsBytes() {
        return metric;
    }

    public byte[] getTimeAsBytes() {
        return time;
    }

    public byte[][] getTagkArrayAsBytes() {
        return tagk;
    }

    public byte[][] getTagvArrayAsBytes() {
        return tagv;
    }

    public String getRawKeyAsHex() {
        return BytesHelper.bytesToHex(rawKey);
    }

    public String getMetricAsHex() {
        return BytesHelper.bytesToHex(metric);
    }

    public String getTimeAsHex() {
        return BytesHelper.bytesToHex(time);
    }

    public ArrayList<OpenTSDBTagRaw> getTagsList() {
        ArrayList<OpenTSDBTagRaw> result = new ArrayList();

        for(int i = 0; i < tagk.length; i++) {
            result.add(new OpenTSDBTagRaw(tagk[i], tagv[i]));
        }

        return result;
    }
}
