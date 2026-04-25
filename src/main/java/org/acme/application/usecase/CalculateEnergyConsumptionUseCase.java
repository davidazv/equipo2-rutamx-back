package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.FleetCalculator;
import org.acme.application.FleetConstants;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.EnergyConsumption;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.Route;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;

import java.util.logging.Logger;

@ApplicationScoped
public class CalculateEnergyConsumptionUseCase {

    private static final Logger log = Logger.getLogger(CalculateEnergyConsumptionUseCase.class.getName());

    private final RouteRepository routeRepository;
    private final BusModelRepository busModelRepository;

    @Inject
    public CalculateEnergyConsumptionUseCase(RouteRepository routeRepository, BusModelRepository busModelRepository) {
        this.routeRepository = routeRepository;
        this.busModelRepository = busModelRepository;
    }

    public EnergyConsumption execute(String routeId, Long busModelId, int occupancyPercent) {
        Route route = routeRepository.findByIdWithDistance(routeId)
                .orElseThrow(() -> new RouteNotFoundException(routeId));

        BusModel busModel = busModelRepository.findById(busModelId)
                .orElseThrow(() -> new BusModelNotFoundException(busModelId));

        if (busModel.getFuelType() != FuelType.ELECTRIC) {
            throw new IllegalArgumentException("El modelo seleccionado debe ser eléctrico");
        }

        double distanceKm = route.getDistanceKm();
        double baseConsumption = busModel.getEnergyConsumptionKwhKm().doubleValue();
        double batteryCapacity = busModel.getBatteryCapacityKwh().doubleValue();
        int passengerCapacity = busModel.getPassengerCapacity();

        double occFactor = FleetCalculator.occupancyFactor(occupancyPercent, passengerCapacity);
        double totalFactor = FleetCalculator.totalConsumptionFactor(occFactor);
        double consumptionKwh = FleetCalculator.energyConsumptionKwh(distanceKm, baseConsumption, totalFactor);

        double batteryUsedPercent = (consumptionKwh / batteryCapacity) * 100.0;
        double batteryPercentAfter = Math.max(0, 100.0 - batteryUsedPercent);

        double remainingEnergy = batteryCapacity - consumptionKwh;
        double remainingRangeKm = Math.max(0, remainingEnergy / (baseConsumption * totalFactor));

        boolean canComplete = batteryPercentAfter > FleetConstants.MIN_BATTERY_PERCENT;

        log.info("Consumo energético: ruta=" + routeId + " modelo=" + busModelId
                + " ocupación=" + occupancyPercent + "% consumo=" + String.format("%.1f", consumptionKwh) + " kWh");

        EnergyConsumption result = new EnergyConsumption();
        result.setRouteId(routeId);
        result.setRouteDistanceKm(Math.round(distanceKm * 10.0) / 10.0);
        result.setBusModelId(busModelId);
        result.setBusModelName(busModel.getName());
        result.setOccupancyPercent(occupancyPercent);
        result.setEstimatedConsumptionKwh(Math.round(consumptionKwh * 10.0) / 10.0);
        result.setBatteryPercentAfter(Math.round(batteryPercentAfter * 10.0) / 10.0);
        result.setRemainingRangeKm(Math.round(remainingRangeKm * 10.0) / 10.0);
        result.setCanCompleteRoute(canComplete);
        return result;
    }
}
