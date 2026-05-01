package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.FleetCalculator;
import org.acme.application.FleetConstants;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.application.exception.RouteNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelSavings;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.Route;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;

import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class CalculateFuelSavingsUseCase {

    private static final Logger log = Logger.getLogger(CalculateFuelSavingsUseCase.class.getName());

    private final BusModelRepository busModelRepository;
    private final RouteRepository routeRepository;

    @Inject
    public CalculateFuelSavingsUseCase(BusModelRepository busModelRepository, RouteRepository routeRepository) {
        this.busModelRepository = busModelRepository;
        this.routeRepository = routeRepository;
    }

    public FuelSavings execute(String routeId, Long busModelId, int numberOfBuses, int projectionYears) {
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
        double dieselCostPerYear = FleetCalculator.dieselCostPerYear(
                numberOfBuses, kmPerYear, dieselBaseline.getFuelConsumptionLKm().doubleValue());
        double electricCostPerYear = FleetCalculator.electricCostPerYear(
                numberOfBuses, kmPerYear, electricModel.getEnergyConsumptionKwhKm().doubleValue());
        double fuelSavingsMXN = dieselCostPerYear - electricCostPerYear;
        double fuelSavingsLiters = FleetCalculator.dieselLitersPerYear(
                numberOfBuses, kmPerYear, dieselBaseline.getFuelConsumptionLKm().doubleValue());

        log.info("Fuel savings para ruta=" + routeId + " modelo=" + busModelId
                + " buses=" + numberOfBuses + ": $" + String.format("%.0f", fuelSavingsMXN)
                + " MXN, " + String.format("%.0f", fuelSavingsLiters) + " litros/año");

        FuelSavings savings = new FuelSavings();
        savings.setRouteId(routeId);
        savings.setRouteDistanceKm(route.getDistanceKm());
        savings.setBusModelId(electricModel.getId());
        savings.setBusModelName(electricModel.getManufacturer() + " " + electricModel.getName());
        savings.setNumberOfBuses(numberOfBuses);
        savings.setFuelSavingsMXN(fuelSavingsMXN);
        savings.setFuelSavingsLiters(fuelSavingsLiters);
        savings.setDieselReferencePriceMXN(FleetConstants.DIESEL_PRICE_PER_LITER_MXN);
        savings.setDieselConsumptionLKm(dieselBaseline.getFuelConsumptionLKm().doubleValue());
        savings.setDieselCostPerYear(dieselCostPerYear);
        savings.setElectricCostPerYear(electricCostPerYear);
        savings.setProjectionYears(projectionYears);
        return savings;
    }
}
