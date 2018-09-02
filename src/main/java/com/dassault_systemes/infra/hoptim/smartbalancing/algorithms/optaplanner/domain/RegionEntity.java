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

import com.dassault_systemes.infra.hoptim.hbase.RegionView;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain.solver.RegionServerStrengthComparator;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain.solver.RegionDifficultyComparator;

@PlanningEntity(difficultyComparatorClass = RegionDifficultyComparator.class)
@XStreamAlias("RegionEntity")
public class RegionEntity extends AbstractPersistable {
    private Long id;
    private RegionView regionView;
    private long requiredDiskSpace; // in MB
    private long requiredWriteRequests; // in number of requests / s
    private long requiredReadRequests; // in number of requests / s

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // Planning variables: changes during planning, between score calculations.
    private RegionServerEntity computer;

    public long getRequiredDiskSpace() {
        return requiredDiskSpace;
    }

    public void setRequiredDiskSpace(long requiredDiskSpace) {
        this.requiredDiskSpace = requiredDiskSpace;
    }

    public long getRequiredWriteRequests() {
        return requiredWriteRequests;
    }

    public void setRequiredWriteRequests(long requiredWriteRequests) {
        this.requiredWriteRequests = requiredWriteRequests;
    }

    @PlanningVariable(valueRangeProviderRefs = {"regionServerRange"},
            strengthComparatorClass = RegionServerStrengthComparator.class)
    public RegionServerEntity getRegionServer() {
        return computer;
    }

    public void setRegionServer(RegionServerEntity computer) {
        this.computer = computer;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public long getRequiredMultiplicand() {
        return requiredDiskSpace * requiredWriteRequests;
    }

    public String getLabel() {
        return "Region " + id;
    }

    public RegionView getRegionView() {
        return regionView;
    }

    public void setRegionView(RegionView regionView) {
        this.regionView = regionView;
    }

    public long getRequiredReadRequests() {
        return requiredReadRequests;
    }

    public void setRequiredReadRequests(long requiredReadRequests) {
        this.requiredReadRequests = requiredReadRequests;
    }
}
