package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.FleetCalculator;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.CostBenefitReport;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.Route;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class GenerateCostBenefitReportUseCase {

    private final RouteRepository routeRepository;
    private final BusModelRepository busModelRepository;

    @Inject
    public GenerateCostBenefitReportUseCase(RouteRepository routeRepository,
                                            BusModelRepository busModelRepository) {
        this.routeRepository = routeRepository;
        this.busModelRepository = busModelRepository;
    }

    public CostBenefitReport execute(String routeId, Long electricModelId, Long dieselModelId,
                                     int numberOfBuses, int projectionYears) {
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
        double investmentMXN = FleetCalculator.totalInvestmentMXN(numberOfBuses, electricModel.getUnitCostUsd().doubleValue());

        double electricOpexPerYear = FleetCalculator.electricCostPerYear(
                numberOfBuses, kmPerYear, electricModel.getEnergyConsumptionKwhKm().doubleValue())
                + numberOfBuses * kmPerYear * electricModel.getMaintenanceCostPerKm().doubleValue();

        double dieselOpexPerYear = FleetCalculator.dieselCostPerYear(
                numberOfBuses, kmPerYear, dieselModel.getFuelConsumptionLKm().doubleValue())
                + numberOfBuses * kmPerYear * dieselModel.getMaintenanceCostPerKm().doubleValue();

        double netAnnualSaving = dieselOpexPerYear - electricOpexPerYear;
        double paybackYears = netAnnualSaving > 0 ? investmentMXN / netAnnualSaving : 0;

        List<CostBenefitReport.CostBenefitPoint> points = new ArrayList<>();
        boolean breakEvenMarked = false;

        for (int year = 1; year <= projectionYears; year++) {
            double electricCumulative = round2(investmentMXN + electricOpexPerYear * year);
            double dieselCumulative = round2(dieselOpexPerYear * year);

            boolean isBreakEven = false;
            if (!breakEvenMarked && paybackYears > 0 && year >= Math.ceil(paybackYears)) {
                isBreakEven = true;
                breakEvenMarked = true;
            }

            points.add(new CostBenefitReport.CostBenefitPoint(
                    year, electricCumulative, dieselCumulative, isBreakEven));
        }

        CostBenefitReport report = new CostBenefitReport();
        report.setRouteId(routeId);
        report.setRouteDistanceKm(Math.round(route.getDistanceKm() * 10.0) / 10.0);
        report.setNumberOfBuses(numberOfBuses);
        report.setElectricModelName(electricModel.getName());
        report.setDieselModelName(dieselModel.getName());
        report.setTotalInvestmentMXN(round2(investmentMXN));
        report.setPaybackYears(round2(paybackYears));
        report.setPoints(points);
        return report;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
