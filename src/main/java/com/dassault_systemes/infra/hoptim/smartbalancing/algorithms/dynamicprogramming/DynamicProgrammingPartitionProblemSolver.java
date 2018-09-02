package com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.dynamicprogramming;


import com.dassault_systemes.infra.hoptim.hbase.RegionView;

/**
 * This is the actual implementation of the Partition Problem Solver using dynamic programming.
 */
public class DynamicProgrammingPartitionProblemSolver implements ThePartitionProblemSolver {

	public int[] solve(RegionView[] regionViews, int k) {
		assert k > 0 && regionViews.length >= k;

		// prefix sums: sum[k] = books[i..k]
		final int[] sum = new int[regionViews.length];
		
		sum[0] = regionViews[0].sizeMB;
		for (int i = 1; i < regionViews.length; i++) sum[i] = sum[i-1] + regionViews[i].sizeMB;

		// M[n<=length][m<=k], D[n<=length][m<=k]
		final int[][] M = new int[regionViews.length+1][k+1];
		final int[][] D = new int[regionViews.length+1][k+1];
		
		for (int n = 1; n <= regionViews.length; n++) M[n][1] = sum[n-1];
		for (int m = 1; m <= k; m++) M[1][m] = regionViews[0].sizeMB;

		for (int n = 2; n <= regionViews.length; n++) {
			for (int m = 2; m <= k; m++) {
				M[n][m] = Integer.MAX_VALUE;
				for (int x = 1; x < n; x++) {
					final int largest = Math.max(M[x][m-1], sum[n-1]-sum[x-1]);
					
					if (largest < M[n][m]) {
						M[n][m] = largest;
						D[n][m] = x;
					}
				}
			}
		}

		int[] dividers = new int[k-1];
		for (int m = k, n = regionViews.length; m > 1; m--)
			n = dividers[m - 2] = D[n][m];
		return dividers;
	}

	
}
