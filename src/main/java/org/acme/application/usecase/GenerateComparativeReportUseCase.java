package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.FleetCalculator;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.ComparativeReport;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.Route;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;

@ApplicationScoped
public class GenerateComparativeReportUseCase {

    private final RouteRepository routeRepository;
    private final BusModelRepository busModelRepository;

    @Inject
    public GenerateComparativeReportUseCase(RouteRepository routeRepository,
                                            BusModelRepository busModelRepository) {
        this.routeRepository = routeRepository;
        this.busModelRepository = busModelRepository;
    }

    public ComparativeReport execute(String routeId, Long electricModelId, Long dieselModelId, int numberOfBuses) {
        Route route = routeRepository.findByIdWithDistance(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        BusModel electricModel = busModelRepository.findById(electricModelId)
                .orElseThrow(() -> new BusModelNotFoundException(electricModelId));

        if (electricModel.getFuelType() != FuelType.ELECTRIC) {
            throw new IllegalArgumentException("El modelo eléctrico seleccionado no es de tipo ELECTRIC");
        }

        BusModel dieselModel = busModelRepository.findById(dieselModelId)
                .orElseThrow(() -> new BusModelNotFoundException(dieselModelId));

        if (dieselModel.getFuelType() != FuelType.DIESEL) {
            throw new IllegalArgumentException("El modelo diésel seleccionado no es de tipo DIESEL");
        }

        double kmPerYear = FleetCalculator.kmPerBusPerYear(route.getDistanceKm());

        double electricEnergyCost = FleetCalculator.electricCostPerYear(
                numberOfBuses, kmPerYear, electricModel.getEnergyConsumptionKwhKm().doubleValue());
        double electricMaintCost = numberOfBuses * kmPerYear
                * electricModel.getMaintenanceCostPerKm().doubleValue();
        double electricTotal = electricEnergyCost + electricMaintCost;
        double electricCo2 = (numberOfBuses * kmPerYear
                * electricModel.getCo2EmissionsGKm().doubleValue()) / 1_000_000.0;

        double dieselFuelCost = FleetCalculator.dieselCostPerYear(
                numberOfBuses, kmPerYear, dieselModel.getFuelConsumptionLKm().doubleValue());
        double dieselMaintCost = numberOfBuses * kmPerYear
                * dieselModel.getMaintenanceCostPerKm().doubleValue();
        double dieselTotal = dieselFuelCost + dieselMaintCost;
        double dieselCo2 = (numberOfBuses * kmPerYear
                * dieselModel.getCo2EmissionsGKm().doubleValue()) / 1_000_000.0;

        double annualSavings = dieselTotal - electricTotal;
        double co2Avoided = dieselCo2 - electricCo2;
        double savingsPercent = dieselTotal > 0 ? (annualSavings / dieselTotal) * 100.0 : 0;

        ComparativeReport report = new ComparativeReport();
        report.setRouteId(routeId);
        report.setRouteDistanceKm(Math.round(route.getDistanceKm() * 10.0) / 10.0);
        report.setNumberOfBuses(numberOfBuses);

        report.setElectricModelName(electricModel.getName());
        report.setElectricCostPerYear(round2(electricEnergyCost));
        report.setElectricMaintenanceCostPerYear(round2(electricMaintCost));
        report.setElectricTotalCostPerYear(round2(electricTotal));
        report.setElectricCo2TonsPerYear(round2(electricCo2));

        report.setDieselModelName(dieselModel.getName());
        report.setDieselCostPerYear(round2(dieselFuelCost));
        report.setDieselMaintenanceCostPerYear(round2(dieselMaintCost));
        report.setDieselTotalCostPerYear(round2(dieselTotal));
        report.setDieselCo2TonsPerYear(round2(dieselCo2));

        report.setAnnualSavingsMXN(round2(annualSavings));
        report.setCo2AvoidedTonsPerYear(round2(co2Avoided));
        report.setSavingsPercent(round2(savingsPercent));

        return report;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
