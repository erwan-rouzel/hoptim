package com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.dynamicprogramming;

import com.dassault_systemes.infra.hoptim.hbase.RegionView;

/**
 * Simple interface used by the Partition Problem Solver algorithm.
 */
public interface ThePartitionProblemSolver {
	int[] solve(RegionView[] books, int k);
}
