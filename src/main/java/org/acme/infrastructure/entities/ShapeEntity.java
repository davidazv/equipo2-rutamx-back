package org.acme.infrastructure.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shapes")
public class ShapeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shape_id", nullable = false, length = 50)
    private String shapeId;

    @Column(name = "shape_pt_lat", nullable = false, precision = 10, scale = 7)
    private BigDecimal shapePtLat;

    @Column(name = "shape_pt_lon", nullable = false, precision = 10, scale = 7)
    private BigDecimal shapePtLon;

    @Column(name = "shape_pt_sequence", nullable = false)
    private Integer shapePtSequence;

    @Column(name = "shape_dist_traveled", precision = 10, scale = 4)
    private BigDecimal shapeDistTraveled;

    public ShapeEntity() {
        // intentionally empty
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getShapeId() { return shapeId; }
    public void setShapeId(String shapeId) { this.shapeId = shapeId; }

    public BigDecimal getShapePtLat() { return shapePtLat; }
    public void setShapePtLat(BigDecimal shapePtLat) { this.shapePtLat = shapePtLat; }

    public BigDecimal getShapePtLon() { return shapePtLon; }
    public void setShapePtLon(BigDecimal shapePtLon) { this.shapePtLon = shapePtLon; }

    public Integer getShapePtSequence() { return shapePtSequence; }
    public void setShapePtSequence(Integer shapePtSequence) { this.shapePtSequence = shapePtSequence; }

    public BigDecimal getShapeDistTraveled() { return shapeDistTraveled; }
    public void setShapeDistTraveled(BigDecimal shapeDistTraveled) { this.shapeDistTraveled = shapeDistTraveled; }
}
