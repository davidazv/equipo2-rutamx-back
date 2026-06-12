package org.acme.infrastructure.mapper;

import org.acme.domain.models.BusModel;
import org.acme.domain.models.FuelType;
import org.acme.infrastructure.entities.BusModelEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class BusModelMapperTest {

    @Test
    void toDomainShouldMapAllFieldsFromEntity() {
        BusModelEntity entity = buildFullEntity();

        BusModel domain = BusModelMapper.toDomain(entity);

        assertEquals(1L, domain.getId());
        assertEquals("E12PRO", domain.getName());
        assertEquals("Yutong", domain.getManufacturer());
        assertEquals(FuelType.ELECTRIC, domain.getFuelType());
        assertEquals(new BigDecimal("300.00"), domain.getAutonomyKm());
        assertEquals(85, domain.getPassengerCapacity());
        assertEquals(new BigDecimal("420000.00"), domain.getUnitCostUsd());
        assertEquals(new BigDecimal("352.08"), domain.getBatteryCapacityKwh());
        assertEquals(new BigDecimal("1.0000"), domain.getEnergyConsumptionKwhKm());
        assertNull(domain.getFuelConsumptionLKm());
        assertEquals(new BigDecimal("0.1200"), domain.getMaintenanceCostPerKm());
        assertEquals(new BigDecimal("0.00"), domain.getCo2EmissionsGKm());
    }

    @Test
    void toDomainShouldHandleNullOptionalFields() {
        BusModelEntity entity = new BusModelEntity();
        entity.setId(2L);
        entity.setName("DMT Hybrid H8");
        entity.setManufacturer("Yutong");
        entity.setFuelType(FuelType.DIESEL);
        entity.setAutonomyKm(new BigDecimal("400.00"));
        entity.setPassengerCapacity(70);
        entity.setUnitCostUsd(new BigDecimal("120000.00"));
        // Leave electric-specific fields null
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        BusModel domain = BusModelMapper.toDomain(entity);

        assertEquals("DMT Hybrid H8", domain.getName());
        assertEquals(FuelType.DIESEL, domain.getFuelType());
        assertNull(domain.getBatteryCapacityKwh());
        assertNull(domain.getEnergyConsumptionKwhKm());
        assertNull(domain.getFuelConsumptionLKm());
        assertNull(domain.getMaintenanceCostPerKm());
        assertNull(domain.getCo2EmissionsGKm());
    }

    @Test
    void toEntityShouldMapAllFieldsFromDomain() {
        BusModel domain = buildFullDomain();

        BusModelEntity entity = BusModelMapper.toEntity(domain);

        assertEquals("E12PRO", entity.getName());
        assertEquals("Yutong", entity.getManufacturer());
        assertEquals(FuelType.ELECTRIC, entity.getFuelType());
        assertEquals(new BigDecimal("300.00"), entity.getAutonomyKm());
        assertEquals(85, entity.getPassengerCapacity());
        assertEquals(new BigDecimal("420000.00"), entity.getUnitCostUsd());
        assertEquals(new BigDecimal("352.08"), entity.getBatteryCapacityKwh());
        assertEquals(new BigDecimal("1.0000"), entity.getEnergyConsumptionKwhKm());
        assertNull(entity.getFuelConsumptionLKm());
        assertEquals(new BigDecimal("0.1200"), entity.getMaintenanceCostPerKm());
        assertEquals(new BigDecimal("0.00"), entity.getCo2EmissionsGKm());
    }

    @Test
    void toEntityShouldNotSetId() {
        BusModel domain = buildFullDomain();
        domain.setId(99L);

        BusModelEntity entity = BusModelMapper.toEntity(domain);

        assertNull(entity.getId());
    }

    @Test
    void toDomainShouldMapElectricFuelType() {
        BusModelEntity entity = buildFullEntity();
        entity.setFuelType(FuelType.ELECTRIC);

        BusModel domain = BusModelMapper.toDomain(entity);

        assertEquals(FuelType.ELECTRIC, domain.getFuelType());
    }

    @Test
    void toDomainShouldMapDieselFuelType() {
        BusModelEntity entity = buildFullEntity();
        entity.setFuelType(FuelType.DIESEL);

        BusModel domain = BusModelMapper.toDomain(entity);

        assertEquals(FuelType.DIESEL, domain.getFuelType());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private BusModelEntity buildFullEntity() {
        BusModelEntity entity = new BusModelEntity();
        entity.setId(1L);
        entity.setName("E12PRO");
        entity.setManufacturer("Yutong");
        entity.setFuelType(FuelType.ELECTRIC);
        entity.setAutonomyKm(new BigDecimal("300.00"));
        entity.setPassengerCapacity(85);
        entity.setUnitCostUsd(new BigDecimal("420000.00"));
        entity.setBatteryCapacityKwh(new BigDecimal("352.08"));
        entity.setEnergyConsumptionKwhKm(new BigDecimal("1.0000"));
        entity.setFuelConsumptionLKm(null);
        entity.setMaintenanceCostPerKm(new BigDecimal("0.1200"));
        entity.setCo2EmissionsGKm(new BigDecimal("0.00"));
        entity.setCreatedAt(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0));
        entity.setUpdatedAt(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0));
        return entity;
    }

    private BusModel buildFullDomain() {
        BusModel model = new BusModel();
        model.setId(1L);
        model.setName("E12PRO");
        model.setManufacturer("Yutong");
        model.setFuelType(FuelType.ELECTRIC);
        model.setAutonomyKm(new BigDecimal("300.00"));
        model.setPassengerCapacity(85);
        model.setUnitCostUsd(new BigDecimal("420000.00"));
        model.setBatteryCapacityKwh(new BigDecimal("352.08"));
        model.setEnergyConsumptionKwhKm(new BigDecimal("1.0000"));
        model.setFuelConsumptionLKm(null);
        model.setMaintenanceCostPerKm(new BigDecimal("0.1200"));
        model.setCo2EmissionsGKm(new BigDecimal("0.00"));
        return model;
    }
}
