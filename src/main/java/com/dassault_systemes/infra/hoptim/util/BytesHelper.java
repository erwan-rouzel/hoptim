package com.dassault_systemes.infra.hoptim.util;

import org.apache.hadoop.hbase.util.Bytes;

/**
 * Created by ERL1 on 4/21/2016.
 *
 * A simple class with static methods to manipulate bytes.
 */
public class BytesHelper {

    /**
     * Converts an array of bytes to its hexadecimal string representation.
     * @param bytes The array of bytes to convert
     * @return The hexadecimal representation
     */
    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        String finalResult = new String();
        for( char j = 0; j < hexChars.length; j += 2) {
            finalResult += "\\x" + hexChars[j] + hexChars[j + 1];
        }

        return finalResult;
    }

    /**
     * Convert an array of bytes to its representation as a string.
     * @param bytes The array of bytes to convert
     * @return The string representation
     */
    public static String bytesToString(byte[] bytes) {
        return Bytes.toString(bytes);
    }

    public static int compare(byte[] left, byte[] right) {
        for (int i = 0, j = 0; i < left.length && j < right.length; i++, j++) {
            int a = (left[i] & 0xff);
            int b = (right[j] & 0xff);
            if (a != b) {
                return a - b;
            }
        }
        return left.length - right.length;
    }
}
