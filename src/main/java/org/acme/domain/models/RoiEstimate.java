package org.acme.domain.models;

public class RoiEstimate {

    private double roiPercent;
    private double paybackYears;
    private double netAnnualReturn;
    private double totalInvestmentMXN;
    private double co2AvoidedTons;
    private double electricCostPerYear;
    private double dieselCostPerYear;

    public RoiEstimate() {
        // intentionally empty
    }

    public double getRoiPercent() { return roiPercent; }
    public void setRoiPercent(double roiPercent) { this.roiPercent = roiPercent; }

    public double getPaybackYears() { return paybackYears; }
    public void setPaybackYears(double paybackYears) { this.paybackYears = paybackYears; }

    public double getNetAnnualReturn() { return netAnnualReturn; }
    public void setNetAnnualReturn(double netAnnualReturn) { this.netAnnualReturn = netAnnualReturn; }

    public double getTotalInvestmentMXN() { return totalInvestmentMXN; }
    public void setTotalInvestmentMXN(double totalInvestmentMXN) { this.totalInvestmentMXN = totalInvestmentMXN; }

    public double getCo2AvoidedTons() { return co2AvoidedTons; }
    public void setCo2AvoidedTons(double co2AvoidedTons) { this.co2AvoidedTons = co2AvoidedTons; }

    public double getElectricCostPerYear() { return electricCostPerYear; }
    public void setElectricCostPerYear(double electricCostPerYear) { this.electricCostPerYear = electricCostPerYear; }

    public double getDieselCostPerYear() { return dieselCostPerYear; }
    public void setDieselCostPerYear(double dieselCostPerYear) { this.dieselCostPerYear = dieselCostPerYear; }
}
