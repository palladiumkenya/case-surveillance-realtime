package org.kenyahmis.worker.mapper;

import org.kenyahmis.shared.dto.ClientDto;
import org.kenyahmis.worker.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(target = "dob", source = "clientDto.dob", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Client clientDtoToClientModel(ClientDto clientDto);
}
