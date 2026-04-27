package org.acme.application;

/**
 * Shared fleet operation constants used across use cases.
 */
public final class FleetConstants {

    private FleetConstants() {}

    public static final int DAILY_TRIPS = 10;
    public static final int OPERATING_DAYS = 310;
    public static final double MXN_PER_USD = 17.5;
    public static final double PRICE_PER_KWH_MXN = 2.8;
    public static final double DIESEL_PRICE_PER_LITER_MXN = 24.0;

    public static final double OCCUPANCY_IMPACT = 0.003;
    public static final double TERRAIN_FACTOR = 1.15;
    public static final double AC_FACTOR = 1.1;
    public static final double MIN_BATTERY_PERCENT = 10.0;

    public static final double PEAK_HOUR_FACTOR = 0.12;
    public static final int DEFAULT_BUS_CAPACITY = 80;
    public static final double DEFAULT_OCCUPANCY = 0.80;
}
