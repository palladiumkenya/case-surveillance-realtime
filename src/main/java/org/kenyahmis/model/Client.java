package org.kenyahmis.model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String patientPk;
    private String sex;
    private String ward;
    private String county;
    private String subCounty;
    private LocalDate dob;
}
