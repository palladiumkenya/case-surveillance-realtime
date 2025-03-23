package org.kenyahmis.model;

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
//    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    @JoinColumn(name = "client_id", referencedColumnName = "id")
    @JsonBackReference
    @ManyToOne
    private Client client;
    @JsonManagedReference
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private LinkedCase linkedCase;
    @JsonManagedReference
    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private NewCase newCase;
    @Column(name = "load_date")
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
