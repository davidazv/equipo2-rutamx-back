package org.acme.infrastructure.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "stop_times")
public class StopTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private StopEntity stop;

    @Column(name = "stop_sequence", nullable = false)
    private Integer stopSequence;

    @Column(name = "arrival_time", length = 8)
    private String arrivalTime;

    @Column(name = "departure_time", length = 8)
    private String departureTime;

    @Column(name = "timepoint")
    private Byte timepoint;

    public StopTimeEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TripEntity getTrip() { return trip; }
    public void setTrip(TripEntity trip) { this.trip = trip; }

    public StopEntity getStop() { return stop; }
    public void setStop(StopEntity stop) { this.stop = stop; }

    public Integer getStopSequence() { return stopSequence; }
    public void setStopSequence(Integer stopSequence) { this.stopSequence = stopSequence; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public Byte getTimepoint() { return timepoint; }
    public void setTimepoint(Byte timepoint) { this.timepoint = timepoint; }
}
