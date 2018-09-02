package com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.dynamicprogramming;

import com.dassault_systemes.infra.hoptim.hbase.RegionView;

/*
 * This is just a simple class with main method to test the Partition Problem Solver algorithm.
 *
 * This implementation of the PP Solver was adapted from this implementation :
 * https://gist.github.com/ishikawa/21680
 */
public class TestPartitionProblemSolver {

    public static void main(String[] args) {
        DynamicProgrammingPartitionProblemSolver solution = new DynamicProgrammingPartitionProblemSolver();

        System.out.println("\n=== Regions ===");
        RegionView[] regionViews = new RegionView[10];
        for(int i = 0; i < 10; i++) {
            RegionView regionView = new RegionView();
            regionView.sizeMB = (int) (Math.random() * 500 + 1);
            regionView.encodedName = "e" + i;
            regionView.id = i;
            regionView.currentRegionServer = "rs1";

            regionViews[i] = regionView;
            System.out.print("(" + regionView.sizeMB + ", " + regionView.id + ", " + regionView.currentRegionServer + ") |");
        }

        int[] result = solution.solve(regionViews, 6);

        System.out.println("");

        int numPartition = 0;
        int index_start = 0;
        for(int region: result) {
            printPartition(regionViews, numPartition, index_start, result[numPartition]);

            index_start = result[numPartition];
            numPartition++;
        }

        printPartition(regionViews, numPartition, index_start, regionViews.length);

    }

    private static void printPartition(RegionView[] regionViews, int numPartition, int start, int end) {
        System.out.println("\n=== Region Server " + (numPartition+1) + " ===");

        int sum = 0;
        System.out.print("Content: ");
        for(int i = start; i < end; i++) {
            System.out.print("(" + regionViews[i].sizeMB + ", " + regionViews[i].id + ", " + regionViews[i].currentRegionServer + ") |");
            sum += regionViews[i].sizeMB;
        }

        System.out.println("\nSum: " + sum);
    }
}
