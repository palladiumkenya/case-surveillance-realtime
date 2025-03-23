package org.kenyahmis.mapper;

import org.kenyahmis.dto.ClientDto;
import org.kenyahmis.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "dob", source = "clientDto.dob", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Client clientDtoToClientModel(ClientDto clientDto);
}
