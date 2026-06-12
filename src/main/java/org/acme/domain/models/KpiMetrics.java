package org.acme.domain.models;

public class KpiMetrics {

    private double totalFuelSavingsMXN;
    private double totalCo2AvoidedTons;
    private double totalInvestmentMXN;
    private double totalElectricCostMXN;
    private double totalDieselCostMXN;
    private int routesAnalyzed;

    public KpiMetrics() {
        // intentionally empty
    }

    public double getTotalFuelSavingsMXN() { return totalFuelSavingsMXN; }
    public void setTotalFuelSavingsMXN(double totalFuelSavingsMXN) { this.totalFuelSavingsMXN = totalFuelSavingsMXN; }

    public double getTotalCo2AvoidedTons() { return totalCo2AvoidedTons; }
    public void setTotalCo2AvoidedTons(double totalCo2AvoidedTons) { this.totalCo2AvoidedTons = totalCo2AvoidedTons; }

    public double getTotalInvestmentMXN() { return totalInvestmentMXN; }
    public void setTotalInvestmentMXN(double totalInvestmentMXN) { this.totalInvestmentMXN = totalInvestmentMXN; }

    public double getTotalElectricCostMXN() { return totalElectricCostMXN; }
    public void setTotalElectricCostMXN(double totalElectricCostMXN) { this.totalElectricCostMXN = totalElectricCostMXN; }

    public double getTotalDieselCostMXN() { return totalDieselCostMXN; }
    public void setTotalDieselCostMXN(double totalDieselCostMXN) { this.totalDieselCostMXN = totalDieselCostMXN; }

    public int getRoutesAnalyzed() { return routesAnalyzed; }
    public void setRoutesAnalyzed(int routesAnalyzed) { this.routesAnalyzed = routesAnalyzed; }
}
