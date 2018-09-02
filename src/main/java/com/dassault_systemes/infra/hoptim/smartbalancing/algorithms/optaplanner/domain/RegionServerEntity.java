/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain;

import com.dassault_systemes.infra.hoptim.hbase.RegionServerView;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.examples.common.domain.AbstractPersistable;

@XStreamAlias("RegionServerEntity")
public class RegionServerEntity extends AbstractPersistable {
    private Long id;
    private RegionServerView regionServerView;
    private int diskSpace; // in gigahertz
    private int writeRequests; // in gigabyte RAM
    private int readRequests;
    private int cost; // in euro per month

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getDiskSpace() {
        return diskSpace;
    }

    public void setDiskSpace(int diskSpace) {
        this.diskSpace = diskSpace;
    }

    public int getWriteRequests() {
        return writeRequests;
    }

    public void setWriteRequests(int memory) {
        this.writeRequests = memory;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public int getMultiplicand() {
        return diskSpace * writeRequests;
    }

    public String getLabel() {
        return "Region Server " + id;
    }

    public RegionServerView getRegionServerView() {
        return regionServerView;
    }

    public void setRegionServerView(RegionServerView regionServerView) {
        this.regionServerView = regionServerView;
    }

    public int getReadRequests() {
        return readRequests;
    }

    public void setReadRequests(int readRequests) {
        this.readRequests = readRequests;
    }
}
