package org.acme.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FleetCalculatorTest {

    // ── occupancyFactor ─────────────────────────────────────────────

    @Test
    void occupancyFactorShouldReturn1WhenZeroOccupancy() {
        // 1.0 + (0/100) * 0.003 * 80 = 1.0
        assertEquals(1.0, FleetCalculator.occupancyFactor(0, 80), 0.0001);
    }

    @Test
    void occupancyFactorShouldScaleWithOccupancyAndCapacity() {
        // 1.0 + (50/100) * 0.003 * 80 = 1.12
        assertEquals(1.12, FleetCalculator.occupancyFactor(50, 80), 0.0001);
    }

    @Test
    void occupancyFactorShouldReturn1Point24At100PercentWith80Passengers() {
        // 1.0 + (100/100) * 0.003 * 80 = 1.24
        assertEquals(1.24, FleetCalculator.occupancyFactor(100, 80), 0.0001);
    }

    @Test
    void occupancyFactorShouldScaleWithHighCapacity() {
        // 1.0 + (50/100) * 0.003 * 140 = 1.21
        assertEquals(1.21, FleetCalculator.occupancyFactor(50, 140), 0.0001);
    }

    @Test
    void occupancyFactorShouldReturn1WithZeroCapacity() {
        // 1.0 + (50/100) * 0.003 * 0 = 1.0
        assertEquals(1.0, FleetCalculator.occupancyFactor(50, 0), 0.0001);
    }

    // ── totalConsumptionFactor ───────────────────────────────────────

    @Test
    void totalConsumptionFactorShouldMultiplyByTerrainAndAC() {
        // 1.0 * 1.15 * 1.1 = 1.265
        assertEquals(1.265, FleetCalculator.totalConsumptionFactor(1.0), 0.0001);
    }

    @Test
    void totalConsumptionFactorShouldApplyAt50PercentOccupancy() {
        // 1.12 * 1.15 * 1.1 = 1.4168
        assertEquals(1.4168, FleetCalculator.totalConsumptionFactor(1.12), 0.0001);
    }

    // ── energyConsumptionKwh ────────────────────────────────────────

    @Test
    void energyConsumptionKwhShouldMultiplyAllFactors() {
        // 20 * 1.0 * 1.4168 = 28.336
        assertEquals(28.336, FleetCalculator.energyConsumptionKwh(20.0, 1.0, 1.4168), 0.001);
    }

    @Test
    void energyConsumptionKwhShouldReturnZeroWithZeroDistance() {
        assertEquals(0.0, FleetCalculator.energyConsumptionKwh(0.0, 1.0, 1.4168), 0.0001);
    }

    @Test
    void energyConsumptionKwhShouldScaleWithHigherConsumptionRate() {
        // 20 * 1.3 * 1.265 = 32.89
        assertEquals(32.89, FleetCalculator.energyConsumptionKwh(20.0, 1.3, 1.265), 0.001);
    }

    // ── kmPerBusPerYear ─────────────────────────────────────────────

    @Test
    void kmPerBusPerYearShouldMultiplyByTripsAndDays() {
        // 20 * 10 * 310 = 62000
        assertEquals(62000.0, FleetCalculator.kmPerBusPerYear(20.0), 0.01);
    }

    @Test
    void kmPerBusPerYearShouldReturnZeroWithZeroDistance() {
        assertEquals(0.0, FleetCalculator.kmPerBusPerYear(0.0), 0.01);
    }

    // ── totalInvestmentMXN ──────────────────────────────────────────

    @Test
    void totalInvestmentMXNShouldConvertToMXN() {
        // 1 * 420000 * 17.5 = 7_350_000
        assertEquals(7_350_000.0, FleetCalculator.totalInvestmentMXN(1, 420000), 0.01);
    }

    @Test
    void totalInvestmentMXNShouldScaleWithBusCount() {
        // 5 * 420000 * 17.5 = 36_750_000
        assertEquals(36_750_000.0, FleetCalculator.totalInvestmentMXN(5, 420000), 0.01);
    }

    // ── electricCostPerYear ─────────────────────────────────────────

    @Test
    void electricCostPerYearShouldComputeCorrectly() {
        // 1 * 62000 * 1.0 * 2.8 = 173_600
        assertEquals(173_600.0, FleetCalculator.electricCostPerYear(1, 62000, 1.0), 0.01);
    }

    // ── dieselCostPerYear ───────────────────────────────────────────

    @Test
    void dieselCostPerYearShouldComputeCorrectly() {
        // 1 * 62000 * 0.35 * 24.0 = 520_800
        assertEquals(520_800.0, FleetCalculator.dieselCostPerYear(1, 62000, 0.35), 0.01);
    }

    // ── maintenanceSavings ──────────────────────────────────────────

    @Test
    void maintenanceSavingsShouldReturnDifference() {
        // 1 * 62000 * (0.22 - 0.12) = 6200
        assertEquals(6200.0, FleetCalculator.maintenanceSavings(1, 62000, 0.22, 0.12), 0.01);
    }

    @Test
    void maintenanceSavingsShouldReturnNegativeWhenElectricCostsMore() {
        // 1 * 62000 * (0.10 - 0.15) = -3100
        assertEquals(-3100.0, FleetCalculator.maintenanceSavings(1, 62000, 0.10, 0.15), 0.01);
    }

    // ── co2AvoidedTons ──────────────────────────────────────────────

    @Test
    void co2AvoidedTonsShouldConvertGramsToTons() {
        // 1 * 62000 * 940 / 1_000_000 = 58.28
        assertEquals(58.28, FleetCalculator.co2AvoidedTons(1, 62000, 940), 0.01);
    }

    @Test
    void co2AvoidedTonsShouldReturnZeroWithZeroEmissions() {
        assertEquals(0.0, FleetCalculator.co2AvoidedTons(1, 62000, 0), 0.01);
    }
}
