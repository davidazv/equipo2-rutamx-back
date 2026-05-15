package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.dto.UpdateBusModelDto;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.domain.models.BusModel;
import org.acme.domain.repository.BusModelRepository;

@ApplicationScoped
public class UpdateBusModelUseCase {

    private final BusModelRepository busModelRepository;

    @Inject
    public UpdateBusModelUseCase(BusModelRepository busModelRepository) {
        this.busModelRepository = busModelRepository;
    }

    public BusModel execute(Long id, UpdateBusModelDto dto) {
        busModelRepository.findById(id)
                .orElseThrow(() -> new BusModelNotFoundException(id));

        BusModel patch = new BusModel();
        patch.setId(id);
        patch.setName(dto.getName());
        patch.setAutonomyKm(dto.getAutonomyKm());
        patch.setPassengerCapacity(dto.getPassengerCapacity());
        patch.setUnitCostUsd(dto.getUnitCostUsd());

        return busModelRepository.update(patch);
    }
}
