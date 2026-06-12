package org.acme.domain.models;

/**
 * A single bus model entry in a recommendation ranking for HU12.
 */
public class BusModelRank {

    private int rank;
    private BusModel model;
    private boolean meetsCapacity;
    private boolean meetsAutonomy;
    private boolean recommended;
    private int requiredCapacity;
    private String justification;

    public BusModelRank() {
        // intentionally empty
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public BusModel getModel() { return model; }
    public void setModel(BusModel model) { this.model = model; }

    public boolean isMeetsCapacity() { return meetsCapacity; }
    public void setMeetsCapacity(boolean meetsCapacity) { this.meetsCapacity = meetsCapacity; }

    public boolean isMeetsAutonomy() { return meetsAutonomy; }
    public void setMeetsAutonomy(boolean meetsAutonomy) { this.meetsAutonomy = meetsAutonomy; }

    public boolean isRecommended() { return recommended; }
    public void setRecommended(boolean recommended) { this.recommended = recommended; }

    public int getRequiredCapacity() { return requiredCapacity; }
    public void setRequiredCapacity(int requiredCapacity) { this.requiredCapacity = requiredCapacity; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
}
