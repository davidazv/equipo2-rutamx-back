package org.acme.application;

/**
 * Static utility for fleet cost and emissions calculations.
 * All methods are pure functions — no side effects.
 */
public final class FleetCalculator {

    private FleetCalculator() {}

    public static double kmPerBusPerYear(double distanceKm) {
        return distanceKm * FleetConstants.DAILY_TRIPS * FleetConstants.OPERATING_DAYS;
    }

    public static double totalInvestmentMXN(int buses, double unitCostUsd) {
        return buses * unitCostUsd * FleetConstants.MXN_PER_USD;
    }

    public static double electricCostPerYear(int buses, double kmPerYear, double energyConsumptionKwhKm) {
        return buses * kmPerYear * energyConsumptionKwhKm * FleetConstants.PRICE_PER_KWH_MXN;
    }

    public static double dieselCostPerYear(int buses, double kmPerYear, double fuelConsumptionLKm) {
        return buses * kmPerYear * fuelConsumptionLKm * FleetConstants.DIESEL_PRICE_PER_LITER_MXN;
    }

    public static double maintenanceSavings(int buses, double kmPerYear, double dieselMaintenance, double electricMaintenance) {
        return buses * kmPerYear * (dieselMaintenance - electricMaintenance);
    }

    public static double co2AvoidedTons(int buses, double kmPerYear, double co2EmissionsGKm) {
        return (buses * kmPerYear * co2EmissionsGKm) / 1_000_000.0;
    }
}
