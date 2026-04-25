package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.FleetCalculator;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.RoiEstimate;
import org.acme.domain.models.Route;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;

import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class EstimateRoiUseCase {

    private static final Logger log = Logger.getLogger(EstimateRoiUseCase.class.getName());

    private final BusModelRepository busModelRepository;
    private final RouteRepository routeRepository;

    @Inject
    public EstimateRoiUseCase(BusModelRepository busModelRepository, RouteRepository routeRepository) {
        this.busModelRepository = busModelRepository;
        this.routeRepository = routeRepository;
    }

    public RoiEstimate execute(String routeId, Long busModelId, int numberOfBuses) {
        Route route = routeRepository.findByIdWithDistance(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        BusModel electricModel = busModelRepository.findById(busModelId)
                .orElseThrow(() -> new BusModelNotFoundException(busModelId));

        if (electricModel.getFuelType() != FuelType.ELECTRIC) {
            throw new IllegalArgumentException("El modelo seleccionado debe ser eléctrico");
        }

        List<BusModel> dieselModels = busModelRepository.findByFuelType(FuelType.DIESEL);
        if (dieselModels.isEmpty()) {
            throw new IllegalStateException("No hay modelos diésel de referencia en la base de datos");
        }
        BusModel dieselBaseline = dieselModels.get(0);

        double kmPerYear = FleetCalculator.kmPerBusPerYear(route.getDistanceKm());
        double totalInvestmentMXN = FleetCalculator.totalInvestmentMXN(numberOfBuses, electricModel.getUnitCostUsd().doubleValue());
        double electricCostPerYear = FleetCalculator.electricCostPerYear(numberOfBuses, kmPerYear, electricModel.getEnergyConsumptionKwhKm().doubleValue());
        double dieselCostPerYear = FleetCalculator.dieselCostPerYear(numberOfBuses, kmPerYear, dieselBaseline.getFuelConsumptionLKm().doubleValue());
        double maintenanceSavings = FleetCalculator.maintenanceSavings(numberOfBuses, kmPerYear, dieselBaseline.getMaintenanceCostPerKm().doubleValue(), electricModel.getMaintenanceCostPerKm().doubleValue());
        double netAnnualReturn = dieselCostPerYear - electricCostPerYear + maintenanceSavings;
        double co2AvoidedTons = FleetCalculator.co2AvoidedTons(numberOfBuses, kmPerYear, dieselBaseline.getCo2EmissionsGKm().doubleValue());

        double roiPercent = totalInvestmentMXN > 0
                ? (netAnnualReturn / totalInvestmentMXN) * 100
                : 0;

        double paybackYears = netAnnualReturn > 0
                ? totalInvestmentMXN / netAnnualReturn
                : 0;

        log.info("ROI estimado para ruta=" + routeId + " modelo=" + busModelId
                + " buses=" + numberOfBuses + ": " + String.format("%.1f%%", roiPercent));

        RoiEstimate estimate = new RoiEstimate();
        estimate.setRoiPercent(roiPercent);
        estimate.setPaybackYears(paybackYears);
        estimate.setNetAnnualReturn(netAnnualReturn);
        estimate.setTotalInvestmentMXN(totalInvestmentMXN);
        estimate.setCo2AvoidedTons(co2AvoidedTons);
        estimate.setElectricCostPerYear(electricCostPerYear);
        estimate.setDieselCostPerYear(dieselCostPerYear);
        return estimate;
    }
}
