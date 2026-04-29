package org.acme.infrastructure.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "frequencies")
public class FrequencyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @Column(name = "start_time", nullable = false, length = 8)
    private String startTime;

    @Column(name = "end_time", nullable = false, length = 8)
    private String endTime;

    @Column(name = "headway_secs", nullable = false)
    private Integer headwaySecs;

    @Column(name = "exact_times")
    private Byte exactTimes;

    public FrequencyEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TripEntity getTrip() { return trip; }
    public void setTrip(TripEntity trip) { this.trip = trip; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Integer getHeadwaySecs() { return headwaySecs; }
    public void setHeadwaySecs(Integer headwaySecs) { this.headwaySecs = headwaySecs; }

    public Byte getExactTimes() { return exactTimes; }
    public void setExactTimes(Byte exactTimes) { this.exactTimes = exactTimes; }
}
