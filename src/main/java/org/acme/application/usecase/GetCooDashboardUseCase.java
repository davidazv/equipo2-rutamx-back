package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.models.CooDashboard;
import org.acme.domain.repository.DashboardRepository;

import java.time.LocalDate;

@ApplicationScoped
public class GetCooDashboardUseCase {

    private final DashboardRepository dashboardRepository;

    @Inject
    public GetCooDashboardUseCase(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public CooDashboard execute(LocalDate start, LocalDate end) {
        return dashboardRepository.getCooDashboard(start, end);
    }
}
