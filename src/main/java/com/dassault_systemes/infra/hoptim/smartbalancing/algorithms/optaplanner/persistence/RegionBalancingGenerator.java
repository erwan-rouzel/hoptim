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

package com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.persistence;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain.RegionBalance;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain.RegionEntity;
import com.dassault_systemes.infra.hoptim.smartbalancing.algorithms.optaplanner.domain.RegionServerEntity;
import org.optaplanner.examples.common.persistence.SolutionDao;

public class RegionBalancingGenerator {

    private static class Price {

        private int hardwareValue;
        private String description;
        private int cost;

        private Price(int hardwareValue, String description, int cost) {
            this.hardwareValue = hardwareValue;
            this.description = description;
            this.cost = cost;
        }

        public int getHardwareValue() {
            return hardwareValue;
        }

        public String getDescription() {
            return description;
        }

        public int getCost() {
            return cost;
        }
    }

    private static final Price[] DISK_SPACE_PRICES = { // in gigahertz
            new Price(3, "3 MB", 1),
            new Price(10, "10 MB", 1),
            new Price(100, "100 MB", 1),
            new Price(500, "500 MB", 1),
            new Price(1000, "1000 MB", 1),
    };
    private static final Price[] WRITE_REQUESTS_PRICES = { // in gigabyte RAM
            new Price(2, "2 gigabyte", 1),
            new Price(4, "4 gigabyte", 1),
            new Price(8, "8 gigabyte", 1),
            new Price(16, "16 gigabyte", 1),
            new Price(32, "32 gigabyte", 1),
    };

    private static final int MAXIMUM_REQUIRED_CPU_POWER = 12; // in gigahertz
    private static final int MAXIMUM_REQUIRED_MEMORY = 32; // in gigabyte RAM

    public static void main(String[] args) {
        new RegionBalancingGenerator().generate();
    }

    protected final SolutionDao solutionDao;
    protected final File outputDir;
    protected Random random;

    public RegionBalancingGenerator() {
        checkConfiguration();
        solutionDao = new RegionBalancingDao();
        outputDir = new File(solutionDao.getDataDir(), "unsolved");
    }

    public RegionBalancingGenerator(boolean withoutDao) {
        if (!withoutDao) {
            throw new IllegalArgumentException("The parameter withoutDao (" + withoutDao + ") must be true.");
        }
        checkConfiguration();
        solutionDao = null;
        outputDir = null;
    }

    public void generate() {
        writeRegionBalance(2, 6);
        writeRegionBalance(3, 9);
        writeRegionBalance(4, 12);
        writeRegionBalance(100, 300);
        writeRegionBalance(200, 600);
        writeRegionBalance(400, 1200);
        writeRegionBalance(800, 2400);
        writeRegionBalance(1600, 4800);
    }

    private void checkConfiguration() {
        if (DISK_SPACE_PRICES.length != WRITE_REQUESTS_PRICES.length) {
            throw new IllegalStateException("All price arrays must be equal in length.");
        }
    }

    private void writeRegionBalance(int computerListSize, int processListSize) {
        String fileName = determineFileName(computerListSize, processListSize);
        File outputFile = new File(outputDir, fileName + ".xml");
        RegionBalance regionBalance = createRegionBalance(fileName, computerListSize, processListSize);
        solutionDao.writeSolution(regionBalance, outputFile);
    }

    public RegionBalance createRegionBalance(int computerListSize, int processListSize) {
        return createRegionBalance(determineFileName(computerListSize, processListSize),
                computerListSize, processListSize);
    }

    private String determineFileName(int computerListSize, int processListSize) {
        return computerListSize + "computers-" + processListSize + "processes";
    }

    public RegionBalance createRegionBalance(String inputId, int computerListSize, int processListSize) {
        random = new Random(47);
        RegionBalance regionBalance = new RegionBalance();
        regionBalance.setId(0L);
        createComputerList(regionBalance, computerListSize);
        createProcessList(regionBalance, processListSize);
        assureComputerCapacityTotalAtLeastProcessRequiredTotal(regionBalance);
        BigInteger possibleSolutionSize = BigInteger.valueOf(regionBalance.getRegionServerList().size()).pow(
                regionBalance.getRegionList().size());
        //logger.info("RegionBalance {} has {} computers and {} processes with a search space of {}.",
        //        inputId, computerListSize, processListSize,
        //        AbstractSolutionImporter.getFlooredPossibleSolutionSize(possibleSolutionSize));
        return regionBalance;
    }

    private void createComputerList(RegionBalance regionBalance, int computerListSize) {
        List<RegionServerEntity> computerList = new ArrayList<RegionServerEntity>(computerListSize);
        for (int i = 0; i < computerListSize; i++) {
            RegionServerEntity computer = new RegionServerEntity();
            computer.setId((long) i);
            int cpuPowerPricesIndex = random.nextInt(DISK_SPACE_PRICES.length);
            computer.setDiskSpace(DISK_SPACE_PRICES[cpuPowerPricesIndex].getHardwareValue());
            int memoryPricesIndex = distortIndex(cpuPowerPricesIndex, WRITE_REQUESTS_PRICES.length);
            computer.setWriteRequests(WRITE_REQUESTS_PRICES[memoryPricesIndex].getHardwareValue());
            int cost = DISK_SPACE_PRICES[cpuPowerPricesIndex].getCost()
                    + WRITE_REQUESTS_PRICES[memoryPricesIndex].getCost();
            //computer.setCost(cost);
            //logger.trace("Created computer with cpuPowerPricesIndex ({}), memoryPricesIndex ({}),"
            //        + " networkBandwidthPricesIndex ({}).",
            //        cpuPowerPricesIndex, memoryPricesIndex, networkBandwidthPricesIndex);
            computerList.add(computer);
        }
        regionBalance.setRegionServerList(computerList);
    }

    private int distortIndex(int referenceIndex, int length) {
        int index = referenceIndex;
        double randomDouble = random.nextDouble();
        double loweringThreshold = 0.25;
        while (randomDouble < loweringThreshold && index >= 1) {
            index--;
            loweringThreshold *= 0.10;
        }
        double heighteningThreshold = 0.75;
        while (randomDouble >= heighteningThreshold && index <= (length - 2)) {
            index++;
            heighteningThreshold = (1.0 - ((1.0 - heighteningThreshold) * 0.10));
        }
        return index;
    }

    private void createProcessList(RegionBalance regionBalance, int processListSize) {
        List<RegionEntity> processList = new ArrayList<RegionEntity>(processListSize);
        for (int i = 0; i < processListSize; i++) {
            RegionEntity process = new RegionEntity();
            process.setId((long) i);
            int requiredCpuPower = generateRandom(MAXIMUM_REQUIRED_CPU_POWER);
            process.setRequiredDiskSpace(requiredCpuPower);
            int requiredMemory = generateRandom(MAXIMUM_REQUIRED_MEMORY);
            process.setRequiredWriteRequests(requiredMemory);
            // Notice that we leave the PlanningVariable properties on null
            processList.add(process);
        }
        regionBalance.setRegionList(processList);
    }

    private int generateRandom(int maximumValue) {
        double randomDouble = random.nextDouble();
        double parabolaBase = 2000.0;
        double parabolaRandomDouble = (Math.pow(parabolaBase, randomDouble) - 1.0) / (parabolaBase - 1.0);
        if (parabolaRandomDouble < 0.0 || parabolaRandomDouble >= 1.0) {
            throw new IllegalArgumentException("Invalid generated parabolaRandomDouble (" + parabolaRandomDouble + ")");
        }
        int value = ((int) Math.floor(parabolaRandomDouble * ((double) maximumValue))) + 1;
        if (value < 1 || value > maximumValue) {
            throw new IllegalArgumentException("Invalid generated value (" + value + ")");
        }
        return value;
    }

    private void assureComputerCapacityTotalAtLeastProcessRequiredTotal(RegionBalance regionBalance) {
        List<RegionServerEntity> computerList = regionBalance.getRegionServerList();
        int cpuPowerTotal = 0;
        int memoryTotal = 0;
        for (RegionServerEntity computer : computerList) {
            cpuPowerTotal += computer.getDiskSpace();
            memoryTotal += computer.getWriteRequests();
        }
        int requiredCpuPowerTotal = 0;
        int requiredMemoryTotal = 0;
        for (RegionEntity process : regionBalance.getRegionList()) {
            requiredCpuPowerTotal += process.getRequiredDiskSpace();
            requiredMemoryTotal += process.getRequiredWriteRequests();
        }
        int cpuPowerLacking = requiredCpuPowerTotal - cpuPowerTotal;
        while (cpuPowerLacking > 0) {
            RegionServerEntity computer = computerList.get(random.nextInt(computerList.size()));
            int upgrade = determineUpgrade(cpuPowerLacking);
            computer.setDiskSpace(computer.getDiskSpace() + upgrade);
            cpuPowerLacking -= upgrade;
        }
        int memoryLacking = requiredMemoryTotal - memoryTotal;
        while (memoryLacking > 0) {
            RegionServerEntity computer = computerList.get(random.nextInt(computerList.size()));
            int upgrade = determineUpgrade(memoryLacking);
            computer.setWriteRequests(computer.getWriteRequests() + upgrade);
            memoryLacking -= upgrade;
        }
    }

    private int determineUpgrade(int lacking) {
        for (int upgrade : new int[] {8, 4, 2, 1}) {
            if (lacking >= upgrade) {
                return upgrade;
            }
        }
        throw new IllegalStateException("Lacking (" + lacking + ") should be at least 1.");
    }

}
