package org.acme.domain.models;

import java.math.BigDecimal;

public class Shape {

    private String shapeId;
    private BigDecimal shapePtLat;
    private BigDecimal shapePtLon;
    private int shapePtSequence;
    private BigDecimal shapeDistTraveled;

    public Shape() {
        // intentionally empty
    }

    public String getShapeId() { return shapeId; }
    public void setShapeId(String shapeId) { this.shapeId = shapeId; }

    public BigDecimal getShapePtLat() { return shapePtLat; }
    public void setShapePtLat(BigDecimal shapePtLat) { this.shapePtLat = shapePtLat; }

    public BigDecimal getShapePtLon() { return shapePtLon; }
    public void setShapePtLon(BigDecimal shapePtLon) { this.shapePtLon = shapePtLon; }

    public int getShapePtSequence() { return shapePtSequence; }
    public void setShapePtSequence(int shapePtSequence) { this.shapePtSequence = shapePtSequence; }

    public BigDecimal getShapeDistTraveled() { return shapeDistTraveled; }
    public void setShapeDistTraveled(BigDecimal shapeDistTraveled) { this.shapeDistTraveled = shapeDistTraveled; }
}
