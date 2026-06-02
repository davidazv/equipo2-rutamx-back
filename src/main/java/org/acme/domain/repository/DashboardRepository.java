package org.acme.domain.repository;

import org.acme.domain.models.CmoDashboard;
import org.acme.domain.models.CooDashboard;

import java.time.LocalDate;

/**
 * Repository for stored-procedure-backed dashboards.
 * Wraps sp_get_cmo_dashboard and sp_get_coo_dashboard.
 */
public interface DashboardRepository {

    /**
     * Calls sp_get_cmo_dashboard(p_agency_id). Pass null to return all agencies.
     */
    CmoDashboard getCmoDashboard(String agencyId);

    /**
     * Calls sp_get_coo_dashboard(p_start, p_end). Both dates are inclusive; nulls mean no bound.
     */
    CooDashboard getCooDashboard(LocalDate start, LocalDate end);
}
