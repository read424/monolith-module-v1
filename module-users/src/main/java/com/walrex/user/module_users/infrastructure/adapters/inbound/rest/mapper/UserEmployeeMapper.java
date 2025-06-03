package com.walrex.user.module_users.infrastructure.adapters.inbound.rest.mapper;

import com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto.UserDetailsResponseDTO;
import com.walrex.user.module_users.infrastructure.adapters.outbound.persistence.entity.UserEmployee;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = Collections.class)
public interface UserEmployeeMapper {
    UserEmployeeMapper INSTANCE = Mappers.getMapper(UserEmployeeMapper.class);

    @Mapping(source ="id_usuario", target = "id_usuario")
    @Mapping(source ="no_usuario", target = "no_usuario")
    @Mapping(source ="idrol_sistema", target = "id_rol")
    @Mapping(source ="id_empleado", target = "id_empleado")
    @Mapping(source ="id_area", target = "id_area")
    @Mapping(source ="state_default", target = "state_default")
    @Mapping(target = "state_menu", expression = "java(Collections.emptyMap())")
    @Mapping(target = "permissions", expression = "java(Collections.emptyList())")
    @Mapping(source=".", target = "no_empleado", qualifiedByName = "concatenarNombreEmpleado")
    UserDetailsResponseDTO toDto(UserEmployee userEmployee);

    @Named("concatenarNombreEmpleado")
    static String concatenarNombreEmpleado(UserEmployee userEmployee){
        if(userEmployee == null ) {
            return "";
        }
        String apepat = userEmployee.getNo_apepat()!=null?userEmployee.getNo_apepat():"";
        String nombres = userEmployee.getNo_nombres()!=null?userEmployee.getNo_nombres():"";
        return (apepat+" "+nombres).trim();
    }

}
