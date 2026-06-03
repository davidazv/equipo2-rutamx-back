package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.models.CmoDashboard;
import org.acme.domain.repository.DashboardRepository;

@ApplicationScoped
public class GetCmoDashboardUseCase {

    private final DashboardRepository dashboardRepository;

    @Inject
    public GetCmoDashboardUseCase(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public CmoDashboard execute(String agencyId) {
        return dashboardRepository.getCmoDashboard(agencyId);
    }
}
