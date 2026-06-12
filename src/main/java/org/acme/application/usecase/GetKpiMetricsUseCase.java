package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.FleetCalculator;
import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelType;
import org.acme.domain.models.KpiMetrics;
import org.acme.domain.models.Route;
import org.acme.domain.repository.BusModelRepository;
import org.acme.domain.repository.RouteRepository;

import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class GetKpiMetricsUseCase {

    private static final Logger log = Logger.getLogger(GetKpiMetricsUseCase.class.getName());

    private final BusModelRepository busModelRepository;
    private final RouteRepository routeRepository;

    @Inject
    public GetKpiMetricsUseCase(BusModelRepository busModelRepository, RouteRepository routeRepository) {
        this.busModelRepository = busModelRepository;
        this.routeRepository = routeRepository;
    }

    public KpiMetrics execute(int busesPerRoute) {
        List<Route> routes = routeRepository.findAllWithDistance();
        List<BusModel> electricModels = busModelRepository.findByFuelType(FuelType.ELECTRIC);
        List<BusModel> dieselModels = busModelRepository.findByFuelType(FuelType.DIESEL);

        if (dieselModels.isEmpty()) {
            throw new IllegalStateException("No hay modelos diésel de referencia en la base de datos");
        }

        BusModel dieselBaseline = dieselModels.get(0);

        double totalElectricCost = 0;
        double totalDieselCost = 0;
        double totalInvestment = 0;
        double totalCo2 = 0;

        for (Route route : routes) {
            double kmPerYear = FleetCalculator.kmPerBusPerYear(route.getDistanceKm());

            for (BusModel electric : electricModels) {
                totalElectricCost += FleetCalculator.electricCostPerYear(busesPerRoute, kmPerYear, electric.getEnergyConsumptionKwhKm().doubleValue());
                totalDieselCost += FleetCalculator.dieselCostPerYear(busesPerRoute, kmPerYear, dieselBaseline.getFuelConsumptionLKm().doubleValue());
                totalInvestment += FleetCalculator.totalInvestmentMXN(busesPerRoute, electric.getUnitCostUsd().doubleValue());
                totalCo2 += FleetCalculator.co2AvoidedTons(busesPerRoute, kmPerYear, dieselBaseline.getCo2EmissionsGKm().doubleValue());
            }
        }

        KpiMetrics metrics = new KpiMetrics();
        metrics.setTotalElectricCostMXN(totalElectricCost);
        metrics.setTotalDieselCostMXN(totalDieselCost);
        metrics.setTotalFuelSavingsMXN(totalDieselCost - totalElectricCost);
        metrics.setTotalInvestmentMXN(totalInvestment);
        metrics.setTotalCo2AvoidedTons(totalCo2);
        metrics.setRoutesAnalyzed(routes.size());

        log.log(java.util.logging.Level.INFO, "KPI metrics computed: {0} routes, {1} electric models, {2} buses/route",
                new Object[]{routes.size(), electricModels.size(), busesPerRoute});

        return metrics;
    }
}
