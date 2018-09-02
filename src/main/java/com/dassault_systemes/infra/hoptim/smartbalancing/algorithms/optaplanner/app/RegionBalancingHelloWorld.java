/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

package com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.app;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain.RegionBalance;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain.RegionServerEntity;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain.RegionEntity;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.persistence.RegionBalancingGenerator;

public class RegionBalancingHelloWorld {

    public static void main(String[] args) {
        // Build the Solver
        SolverFactory<RegionBalance> solverFactory = SolverFactory.createFromXmlResource("regionBalancingSolverConfig.xml");
        Solver<RegionBalance> solver = solverFactory.buildSolver();

        // Load a problem with 400 computers and 1200 processes
        RegionBalance unsolvedRegionBalance = new RegionBalancingGenerator().createRegionBalance(6, 20);

        // Solve the problem
        RegionBalance solvedRegionBalance = solver.solve(unsolvedRegionBalance);

        // Display the result
        System.out.println("\nSolved regionBalance with 6 region servers and 20 regions:\n"
                + toDisplayString(solvedRegionBalance));
    }

    public static String toDisplayString(RegionBalance regionBalance) {
        StringBuilder displayString = new StringBuilder();
        for (RegionEntity regionView : regionBalance.getRegionList()) {
            RegionServerEntity regionServer = regionView.getRegionServer();
            displayString.append("  ").append(regionView.getLabel()).append(" -> ")
                    .append(regionServer == null ? null : regionServer.getLabel()).append("\n");
        }
        return displayString.toString();
    }

}
