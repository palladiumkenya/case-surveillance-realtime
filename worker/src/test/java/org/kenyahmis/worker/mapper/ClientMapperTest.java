package org.kenyahmis.worker.mapper;

import org.junit.jupiter.api.Test;
import org.kenyahmis.shared.dto.ClientDto;
import org.kenyahmis.worker.model.Client;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ClientMapperTest {

    private final ClientMapper clientMapper = Mappers.getMapper(ClientMapper.class);

    @Test
    void mapsAllFields() {
        ClientDto dto = new ClientDto();
        dto.setPatientPk("505");
        dto.setSex("male");
        dto.setCounty("Nairobi");
        dto.setSubCounty("Langata");
        dto.setWard("Langata");
        dto.setShaNumber("SHA123");
        dto.setDob("1990-11-11");

        Client result = clientMapper.clientDtoToClientModel(dto);

        assertEquals("505", result.getPatientPk());
        assertEquals("male", result.getSex());
        assertEquals("Nairobi", result.getCounty());
        assertEquals("Langata", result.getSubCounty());
        assertEquals("Langata", result.getWard());
        assertEquals("SHA123", result.getShaNumber());
    }

    @Test
    void parsesDateOfBirth() {
        ClientDto dto = new ClientDto();
        dto.setPatientPk("505");
        dto.setSex("male");
        dto.setDob("1990-11-11");

        Client result = clientMapper.clientDtoToClientModel(dto);

        assertEquals(LocalDate.of(1990, 11, 11), result.getDob());
    }

    @Test
    void idIsNull() {
        ClientDto dto = new ClientDto();
        dto.setPatientPk("505");
        dto.setSex("male");
        dto.setDob("1990-11-11");

        Client result = clientMapper.clientDtoToClientModel(dto);

        assertNull(result.getId());
    }

    @Test
    void eventsIsNull() {
        ClientDto dto = new ClientDto();
        dto.setPatientPk("505");
        dto.setSex("male");
        dto.setDob("1990-11-11");

        Client result = clientMapper.clientDtoToClientModel(dto);

        assertNull(result.getEvents());
    }
}
