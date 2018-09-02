package com.dassault_systemes.infra.hoptim.smartbalancing;

import java.io.IOException;

/**
 * Created by ERL1 on 5/25/2016.
 */
public class Nop extends ClusterOperation {
    public Nop() {
        super();
    }

    @Override
    public void execute() throws IOException {
        // Do nothing, just for testing purpose
    }
}
