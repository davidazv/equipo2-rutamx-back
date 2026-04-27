package org.acme.application.usecase;

import org.acme.application.exception.DemandNotFoundException;
import org.acme.domain.models.BusCountRecommendation;
import org.acme.domain.models.DayType;
import org.acme.domain.repository.AfluenciaMetrobusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecommendBusCountUseCaseTest {

    private AfluenciaMetrobusRepository afluenciaRepository;
    private RecommendBusCountUseCase useCase;

    @BeforeEach
    void setUp() {
        afluenciaRepository = mock(AfluenciaMetrobusRepository.class);
        useCase = new RecommendBusCountUseCase(afluenciaRepository);
    }

    @Test
    void executeShouldReturnRecommendationWhenValidWeekdayInput() {
        // demand=4000, peakHour=4000*0.12=480, buses=CEIL(480/(80*0.80))=CEIL(480/64)=8
        when(afluenciaRepository.findAverageDailyDemand("L1", DayType.WEEKDAY))
                .thenReturn(new BigDecimal("4000"));

        BusCountRecommendation result = useCase.execute("L1", "weekday", 80);

        assertEquals(480.0, result.getPeakHourDemand(), 0.001);
        assertEquals(8, result.getRecommendedBuses());
        assertEquals(0.80, result.getTargetOccupancy(), 0.001);
        assertEquals("weekday", result.getDayType());
        assertEquals("L1", result.getLinea());
        assertEquals(4000.0, result.getAvgDailyDemand(), 0.001);
    }

    @Test
    void executeShouldUseDefaultsWhenOptionalParamsNull() {
        // null dayType -> WEEKDAY, null occupancy -> 80
        when(afluenciaRepository.findAverageDailyDemand("L2", DayType.WEEKDAY))
                .thenReturn(new BigDecimal("4000"));

        BusCountRecommendation result = useCase.execute("L2", null, null);

        assertEquals("weekday", result.getDayType());
        assertEquals(0.80, result.getTargetOccupancy(), 0.001);
        assertEquals(8, result.getRecommendedBuses());
    }

    @Test
    void executeShouldThrowWhenNoDataForLinea() {
        when(afluenciaRepository.findAverageDailyDemand("L99", DayType.WEEKDAY))
                .thenReturn(null);

        assertThrows(DemandNotFoundException.class,
                () -> useCase.execute("L99", "weekday", null));
    }

    @Test
    void executeShouldReturnDifferentCountForSaturday() {
        // demand=2500, peakHour=2500*0.12=300, buses=CEIL(300/(80*0.80))=CEIL(300/64)=CEIL(4.6875)=5
        when(afluenciaRepository.findAverageDailyDemand("L1", DayType.SATURDAY))
                .thenReturn(new BigDecimal("2500"));

        BusCountRecommendation result = useCase.execute("L1", "saturday", 80);

        assertEquals(300.0, result.getPeakHourDemand(), 0.001);
        assertEquals(5, result.getRecommendedBuses());
        assertEquals("saturday", result.getDayType());
    }

    @Test
    void executeShouldHandleCustomOccupancy() {
        // demand=4000, peakHour=480, occupancy=95% -> buses=CEIL(480/(80*0.95))=CEIL(480/76)=CEIL(6.315...)=7
        when(afluenciaRepository.findAverageDailyDemand("L1", DayType.WEEKDAY))
                .thenReturn(new BigDecimal("4000"));

        BusCountRecommendation result = useCase.execute("L1", "weekday", 95);

        assertEquals(0.95, result.getTargetOccupancy(), 0.001);
        assertEquals(7, result.getRecommendedBuses());
    }

    @Test
    void executeShouldReturnOneWhenDemandIsVeryLow() {
        // demand=10, peakHour=10*0.12=1.2, buses=CEIL(1.2/64)=CEIL(0.01875)=1
        when(afluenciaRepository.findAverageDailyDemand("L1", DayType.WEEKDAY))
                .thenReturn(new BigDecimal("10"));

        BusCountRecommendation result = useCase.execute("L1", "weekday", 80);

        assertEquals(1, result.getRecommendedBuses());
    }

    @Test
    void executeShouldThrowWhenDayTypeInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute("L1", "holiday", null));
    }
}
