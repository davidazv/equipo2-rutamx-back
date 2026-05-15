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
import org.acme.domain.models.TcoDataPoint;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class GenerateComparativeReportUseCase {

    private static final Logger log = Logger.getLogger(GenerateComparativeReportUseCase.class.getName());

    private final BusModelRepository busModelRepository;
    private final RouteRepository routeRepository;

    @Inject
    public GenerateComparativeReportUseCase(BusModelRepository busModelRepository,
                                             RouteRepository routeRepository) {
        this.busModelRepository = busModelRepository;
        this.routeRepository = routeRepository;
    }

    public ComparativeReport execute(String routeId, Long electricModelId, Long dieselModelId,
                                     int numberOfBuses, int projectionYears) {
        if (projectionYears < 1 || projectionYears > 30) {
            throw new IllegalArgumentException("Los años de proyección deben estar entre 1 y 30");
        }
        if (numberOfBuses < 1) {
            throw new IllegalArgumentException("El número de buses debe ser al menos 1");
        }

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

        double electricCostPerYear = FleetCalculator.electricCostPerYear(
                numberOfBuses, kmPerYear, electricModel.getEnergyConsumptionKwhKm().doubleValue());
        double dieselCostPerYear = FleetCalculator.dieselCostPerYear(
                numberOfBuses, kmPerYear, dieselModel.getFuelConsumptionLKm().doubleValue());

        double electricMaintenancePerYear = numberOfBuses * kmPerYear
                * electricModel.getMaintenanceCostPerKm().doubleValue();
        double dieselMaintenancePerYear = numberOfBuses * kmPerYear
                * dieselModel.getMaintenanceCostPerKm().doubleValue();

        double co2AvoidedTonsPerYear = FleetCalculator.co2AvoidedTons(
                numberOfBuses, kmPerYear, dieselModel.getCo2EmissionsGKm().doubleValue());

        double totalInvestmentMXN = FleetCalculator.totalInvestmentMXN(
                numberOfBuses, electricModel.getUnitCostUsd().doubleValue());

        double maintenanceSavings = FleetCalculator.maintenanceSavings(
                numberOfBuses, kmPerYear,
                dieselModel.getMaintenanceCostPerKm().doubleValue(),
                electricModel.getMaintenanceCostPerKm().doubleValue());

        double netAnnualSavings = (dieselCostPerYear - electricCostPerYear) + maintenanceSavings;
        double roiPercent = totalInvestmentMXN > 0 ? (netAnnualSavings / totalInvestmentMXN) * 100 : 0;
        double paybackYears = netAnnualSavings > 0 ? totalInvestmentMXN / netAnnualSavings : 0;

        // TCO acumulado año por año: eléctrico incluye inversión inicial + operación; diésel solo operación
        List<TcoDataPoint> tcoProjection = new ArrayList<>();
        int paybackYear = 0;
        for (int y = 1; y <= projectionYears; y++) {
            double electricTCO = totalInvestmentMXN
                    + (electricCostPerYear + electricMaintenancePerYear) * y;
            double dieselTCO = (dieselCostPerYear + dieselMaintenancePerYear) * y;
            tcoProjection.add(new TcoDataPoint(y, electricTCO, dieselTCO));
            if (paybackYear == 0 && electricTCO <= dieselTCO) {
                paybackYear = y;
            }
        }

        ComparativeReport report = new ComparativeReport();
        report.setRouteId(route.getRouteId());
        report.setRouteName(route.getRouteLongName());
        report.setDistanceKm(route.getDistanceKm());
        report.setNumberOfBuses(numberOfBuses);
        report.setProjectionYears(projectionYears);
        report.setElectricModelName(electricModel.getManufacturer() + " " + electricModel.getName());
        report.setDieselModelName(dieselModel.getManufacturer() + " " + dieselModel.getName());
        report.setElectricCostPerYear(electricCostPerYear);
        report.setDieselCostPerYear(dieselCostPerYear);
        report.setElectricMaintenancePerYear(electricMaintenancePerYear);
        report.setDieselMaintenancePerYear(dieselMaintenancePerYear);
        report.setCo2AvoidedTonsPerYear(co2AvoidedTonsPerYear);
        report.setTotalInvestmentMXN(totalInvestmentMXN);
        report.setNetAnnualSavings(netAnnualSavings);
        report.setRoiPercent(roiPercent);
        report.setPaybackYears(paybackYears);
        report.setTcoProjection(tcoProjection);
        report.setPaybackYear(paybackYear);

        log.info("Reporte comparativo generado: ruta=" + routeId
                + " eléctrico=" + electricModelId + " diésel=" + dieselModelId
                + " buses=" + numberOfBuses + " años=" + projectionYears);

        return report;
    }
}
