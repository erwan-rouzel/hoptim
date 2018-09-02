/*
 * The Algorithm Design Manual - Dynamic Programming - The Partition Problem
 */
package com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.dynamicprogramming;


import com.dassault_systemes.infra.hoptim.hbase.RegionView;

/**
 * This is the actual implementation of the Partition Problem Solver using recursive method.
 */
public class RecursivePartitionProblemSolver implements ThePartitionProblemSolver {

	private static int sum(RegionView[] x, int begin, int end) {
		int sum = 0;
		for (int i = begin; i < end; i++) sum += x[i].sizeMB;
		return sum;
	}

	private int minimumPossibleLength(RegionView[] regionViews, int n, int k) {
		if (k == 1) return sum(regionViews, 0, n);
		if (n == 1) return regionViews[0].sizeMB;
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < n; i++) {
			int a = minimumPossibleLength(regionViews, i+1, k - 1);
			int b = sum(regionViews, i+1, n);
			min = Math.min(min, Math.max(a, b));
		}
		return min;
	}
	
	public int[] solve(RegionView[] regionViews, int k) {
		assert k > 0;
		assert regionViews.length >= k;

		int min = minimumPossibleLength(regionViews, regionViews.length, k);
		int[] dividers = new int[k-1];
		int sum = 0, d = 0;
		//System.out.printf("%s, %d => %d\n", Arrays.toString(books), k, min);
		for (int i = 0; i < regionViews.length; i++) {
			if (sum + regionViews[i].sizeMB > min) {
				dividers[d++] = i;
				sum = regionViews[i].sizeMB;
			} else {
				sum += regionViews[i].sizeMB;
			}
		}
		assert d == dividers.length;
		return dividers;
	}

}

