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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.buildin.hardsoft.HardSoftScoreDefinition;
import org.optaplanner.persistence.xstream.impl.score.XStreamScoreConverter;

@PlanningSolution
@XStreamAlias("RegionBalance")
public class RegionBalance extends AbstractPersistable implements Solution<HardSoftScore> {
    protected Long id;
    private int moveMax;
    private int moveWeight;
    private int sizeWeight;
    private int readWeight;
    private int writeWeight;
    private int maxNumberOfRegionsPerRS;
    private List<RegionServerEntity> regionServerList;
    private List<RegionEntity> regionList;
    private List<String> tablesSelection;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @XStreamConverter(value = XStreamScoreConverter.class, types = {HardSoftScoreDefinition.class})
    private HardSoftScore score;

    @ValueRangeProvider(id = "regionServerRange")
    public List<RegionServerEntity> getRegionServerList() {
        return regionServerList;
    }

    public void setRegionServerList(List<RegionServerEntity> regionServerList) {
        this.regionServerList = regionServerList;
    }

    @PlanningEntityCollectionProperty
    public List<RegionEntity> getRegionList() {
        return regionList;
    }

    public void setRegionList(List<RegionEntity> regionList) {
        this.regionList = regionList;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public Collection<? extends Object> getProblemFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(regionServerList);
        // Do not add the planning entity's (regionList) because that will be done automatically
        return facts;
    }

    public int getSizeWeight() {
        return sizeWeight;
    }

    public void setSizeWeight(int sizeWeight) {
        this.sizeWeight = sizeWeight;
    }

    public int getReadWeight() {
        return readWeight;
    }

    public void setReadWeight(int readWeight) {
        this.readWeight = readWeight;
    }

    public int getWriteWeight() {
        return writeWeight;
    }

    public void setWriteWeight(int writeWeight) {
        this.writeWeight = writeWeight;
    }

    public int getMaxNumberOfRegionsPerRS() {
        return maxNumberOfRegionsPerRS;
    }

    public void setMaxNumberOfRegionsPerRS(int maxNumberOfRegionsPerRS) {
        this.maxNumberOfRegionsPerRS = maxNumberOfRegionsPerRS;
    }

    public List<String> getTablesSelection() {
        return tablesSelection;
    }

    public void setTablesSelection(List<String> tablesSelection) {
        this.tablesSelection = tablesSelection;
    }

    public int getMoveWeight() {
        return moveWeight;
    }

    public void setMoveWeight(int moveWeight) {
        this.moveWeight = moveWeight;
    }

    public int getMoveMax() {
        return moveMax;
    }

    public void setMoveMax(int moveMax) {
        this.moveMax = moveMax;
    }
}
