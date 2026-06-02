package org.acme.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.acme.domain.models.CmoDashboard;
import org.acme.domain.models.CmoDashboardRouteRow;
import org.acme.domain.models.CooDashboard;
import org.acme.domain.models.DashboardAgencyRow;
import org.acme.domain.models.HourlyTripRow;
import org.acme.domain.models.OperationalCounters;
import org.acme.domain.models.PassengerTrendRow;
import org.acme.domain.repository.DashboardRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DashboardRepositoryImpl implements DashboardRepository {

    @Inject
    EntityManager entityManager;

    @Override
    public CmoDashboard getCmoDashboard(String agencyId) {
        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("sp_get_cmo_dashboard")
                .registerStoredProcedureParameter("p_agency_id", String.class, ParameterMode.IN)
                .setParameter("p_agency_id", agencyId);

        query.execute();

        @SuppressWarnings("unchecked")
        List<Object[]> routeRows = query.getResultList();
        List<CmoDashboardRouteRow> routes = new ArrayList<>(routeRows.size());
        for (Object[] r : routeRows) {
            CmoDashboardRouteRow row = new CmoDashboardRouteRow();
            row.setRouteId(asString(r[0]));
            row.setRouteShortName(asString(r[1]));
            row.setRouteLongName(asString(r[2]));
            row.setAgencyId(asString(r[3]));
            row.setRouteColor(asString(r[4]));
            row.setDistanciaKm(asDouble(r[5]));
            row.setTotalTrips(asLong(r[6]));
            row.setAvgDailyTrips(asDouble(r[7]));
            row.setHeadwayMinutes((int) asLong(r[8]));
            row.setAnnualKm(asDouble(r[9]));
            row.setCo2DieselTonAnio(asDouble(r[10]));
            row.setCo2ElectricTonAnio(asDouble(r[11]));
            routes.add(row);
        }

        List<DashboardAgencyRow> agencies = readAgencies(query);
        return new CmoDashboard(routes, agencies);
    }

    @Override
    public CooDashboard getCooDashboard(LocalDate start, LocalDate end) {
        StoredProcedureQuery query = entityManager
                .createStoredProcedureQuery("sp_get_coo_dashboard")
                .registerStoredProcedureParameter("p_start", Date.class, ParameterMode.IN)
                .registerStoredProcedureParameter("p_end",   Date.class, ParameterMode.IN)
                .setParameter("p_start", start != null ? Date.valueOf(start) : null)
                .setParameter("p_end",   end   != null ? Date.valueOf(end)   : null);

        query.execute();

        // Result-set 1: counters (single row)
        @SuppressWarnings("unchecked")
        List<Object[]> counterRows = query.getResultList();
        OperationalCounters counters = new OperationalCounters();
        if (!counterRows.isEmpty()) {
            Object[] c = counterRows.get(0);
            counters.setTotalRoutes(asLong(c[0]));
            counters.setTotalTrips(asLong(c[1]));
            counters.setTotalStops(asLong(c[2]));
            counters.setTotalShapes(asLong(c[3]));
            counters.setAvgFrequencyMin(asDouble(c[4]));
        }

        // Result-set 2: passenger trend
        query.hasMoreResults();
        @SuppressWarnings("unchecked")
        List<Object[]> trendRows = query.getResultList();
        List<PassengerTrendRow> trend = new ArrayList<>(trendRows.size());
        for (Object[] r : trendRows) {
            trend.add(new PassengerTrendRow((int) asLong(r[0]), asDouble(r[1])));
        }

        // Result-set 3: hourly trips
        query.hasMoreResults();
        @SuppressWarnings("unchecked")
        List<Object[]> hourlyRows = query.getResultList();
        List<HourlyTripRow> hourly = new ArrayList<>(hourlyRows.size());
        for (Object[] r : hourlyRows) {
            hourly.add(new HourlyTripRow((int) asLong(r[0]), asLong(r[1])));
        }

        // Result-set 4: agencies
        query.hasMoreResults();
        List<DashboardAgencyRow> agencies = readAgencies(query);

        return new CooDashboard(counters, trend, hourly, agencies);
    }

    private List<DashboardAgencyRow> readAgencies(StoredProcedureQuery query) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<DashboardAgencyRow> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new DashboardAgencyRow(asString(r[0]), asString(r[1])));
        }
        return result;
    }

    private static String asString(Object v) {
        return v == null ? null : v.toString();
    }

    private static long asLong(Object v) {
        return v == null ? 0L : ((Number) v).longValue();
    }

    private static double asDouble(Object v) {
        return v == null ? 0.0 : ((Number) v).doubleValue();
    }
}
