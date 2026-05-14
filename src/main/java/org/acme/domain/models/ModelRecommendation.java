package org.acme.domain.models;

import java.util.List;

public class ModelRecommendation {

    private String linea;
    private int requiredCapacity;
    private List<ModelCandidate> models;

    public ModelRecommendation() {}

    public String getLinea() { return linea; }
    public void setLinea(String linea) { this.linea = linea; }

    public int getRequiredCapacity() { return requiredCapacity; }
    public void setRequiredCapacity(int requiredCapacity) { this.requiredCapacity = requiredCapacity; }

    public List<ModelCandidate> getModels() { return models; }
    public void setModels(List<ModelCandidate> models) { this.models = models; }

    public static class ModelCandidate {

        private long id;
        private String name;
        private String manufacturer;
        private int passengerCapacity;
        private double autonomyKm;
        private double unitCostUsd;
        private boolean recommended;

        public ModelCandidate() {}

        public long getId() { return id; }
        public void setId(long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getManufacturer() { return manufacturer; }
        public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

        public int getPassengerCapacity() { return passengerCapacity; }
        public void setPassengerCapacity(int passengerCapacity) { this.passengerCapacity = passengerCapacity; }

        public double getAutonomyKm() { return autonomyKm; }
        public void setAutonomyKm(double autonomyKm) { this.autonomyKm = autonomyKm; }

        public double getUnitCostUsd() { return unitCostUsd; }
        public void setUnitCostUsd(double unitCostUsd) { this.unitCostUsd = unitCostUsd; }

        public boolean isRecommended() { return recommended; }
        public void setRecommended(boolean recommended) { this.recommended = recommended; }
    }
}
