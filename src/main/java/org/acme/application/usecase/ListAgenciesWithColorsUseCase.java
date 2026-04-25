package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.domain.models.AgencyWithColors;
import org.acme.domain.repository.AgencyRepository;

import java.util.List;

@ApplicationScoped
public class ListAgenciesWithColorsUseCase {

    private final AgencyRepository agencyRepository;

    @Inject
    public ListAgenciesWithColorsUseCase(AgencyRepository agencyRepository) {
        this.agencyRepository = agencyRepository;
    }

    public List<AgencyWithColors> execute() {
        return agencyRepository.findAllWithColorInfo();
    }
}
