package com.dassault_systemes.infra.hoptim.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ERL1 on 8/3/2016.
 */
public class CustomLogger {
    public static void debug(Object emitter, String message) {
        System.out.println("[" + getCurrentTimestamp() + "] (" + emitter.getClass().getSimpleName() + ") " + message);
    }

    private static String getCurrentTimestamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}
