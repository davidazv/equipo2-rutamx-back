package org.acme.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.application.exception.BusModelNotFoundException;
import org.acme.domain.repository.BusModelRepository;

@ApplicationScoped
public class DeleteBusModelUseCase {

    private final BusModelRepository busModelRepository;

    @Inject
    public DeleteBusModelUseCase(BusModelRepository busModelRepository) {
        this.busModelRepository = busModelRepository;
    }

    public void execute(Long id) {
        busModelRepository.findById(id)
                .orElseThrow(() -> new BusModelNotFoundException(id));

        busModelRepository.deleteById(id);
    }
}
