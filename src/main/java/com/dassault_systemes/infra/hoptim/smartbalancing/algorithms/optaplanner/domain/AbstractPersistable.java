package com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain;

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

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;

import java.io.Serializable;

public abstract class AbstractPersistable implements Serializable, Comparable<org.optaplanner.examples.common.domain.AbstractPersistable> {

    protected Long id;

    protected AbstractPersistable() {
    }

    protected AbstractPersistable(long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Used by the GUI to sort the {@link ConstraintMatch} list
     * by {@link ConstraintMatch#getJustificationList()}.
     * @param other never null
     * @return comparison
     */
    public int compareTo(org.optaplanner.examples.common.domain.AbstractPersistable other) {
        return new CompareToBuilder()
                .append(getClass().getName(), other.getClass().getName())
                .append(id, other.getId())
                .toComparison();
    }

    public String toString() {
        return getClass().getName().replaceAll(".*\\.", "") + "-" + id;
    }

}
