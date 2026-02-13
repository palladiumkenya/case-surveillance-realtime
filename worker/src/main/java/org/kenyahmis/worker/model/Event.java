package org.kenyahmis.worker.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "event")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String eventType;
    private String mflCode;
    @JoinColumn(name = "emrVendorId", referencedColumnName = "id")
    private UUID emrVendorId;
    @JsonBackReference
    @ManyToOne
    private Client client;
    @JsonManagedReference
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private LinkedCase linkedCase;
    @JsonManagedReference
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private NewCase newCase;
    @JsonManagedReference
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private AtRiskPbfw atRiskPbfw;
    @JsonManagedReference
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private EligibleForVl eligibleForVl;
    @JsonManagedReference
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private UnsuppressedViralLoad unsuppressedViralLoad;
    @JsonManagedReference
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private PrepLinkedAtRiskPbfw prepLinkedAtRiskPbfw;
    @JsonManagedReference
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private PrepUptake prepUptake;
    @JsonManagedReference
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private HeiWithoutPcr heiWithoutPcr;
    @JsonManagedReference
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private HeiWithoutFinalOutcome heiWithoutFinalOutcome;
    @JsonManagedReference
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private HeiAged6To8Months heiAged6To8Months;
    @JsonManagedReference
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private HeiAged24Months heiAged24Months;
    @JsonManagedReference
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Mortality mortality;
    @Column(name = "load_date")
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
