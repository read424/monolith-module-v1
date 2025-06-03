package com.walrex.user.module_users.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walrex.user.module_users.application.ports.input.GetUserDetailsUseCase;
import com.walrex.user.module_users.application.ports.output.RoleMessageProducer;
import com.walrex.user.module_users.application.ports.output.UserDetailOutputPort;
import com.walrex.user.module_users.infrastructure.adapters.inbound.rest.dto.UserDetailsResponseDTO;
import com.walrex.user.module_users.infrastructure.adapters.inbound.rest.mapper.UserEmployeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEmployeeService implements GetUserDetailsUseCase {
    private final UserDetailOutputPort userDetailOutputPort;
    private final UserEmployeeMapper userEmployeeMapper;
    private final RoleMessageProducer roleMessageProducer;
    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Mono<UserDetailsResponseDTO> getUserDetails(String name_user) {
        log.info("🔍 Buscando usuario con nombre: {}", name_user);
        return userDetailOutputPort.findByUsername(name_user)
                .doOnNext(user -> log.info("Usuario encontrado {}", user.getNo_usuario()))
                .switchIfEmpty(Mono.defer(()->{
                    log.warn("⚠️ Usuario no encontrado: {}", name_user);
                    return Mono.error(new RuntimeException("Usuario no encontrado"));
                }))
                .flatMap(user->{
                    return roleMessageProducer.sendMessage("detail-rol", user.getIdrol_sistema().toString(), user.getIdrol_sistema().toString())
                            .flatMap(response->{
                                log.info("📥 Respuesta recibida del microservicio de roles: {}", response);
                                Map<String, Object> rol_info= parseJson(response);
                                List<String> tmp_permissions = new ArrayList<>();
                                Map<String, List<String>> details_rol_user = new HashMap<>();
                                details_rol_user.put("parents", new ArrayList<>());
                                details_rol_user.put("state_win", new ArrayList<>());
                                log.info("🛠️ Exist details: {}", rol_info.containsKey("details"));
                                if(rol_info.containsKey("details") && rol_info.get("details") instanceof List){
                                    //Primero ubicar los state de tipo parents
                                    List<Map<String,Object>> details = (List<Map<String, Object>>) rol_info.get("details");
                                    log.info("🛠️ details: {}", details);
                                    Map<String, String> item_parents = new HashMap<>();
                                    String key = null;
                                    for(Map<String, Object> item: details){
                                        if((item.containsKey("id_parent_win") && item.get("id_parent_win")!=null)){
                                            key = item.get("id_parent_win").toString();
                                            if(!item_parents.containsKey(key)){
                                                item_parents.put(key, "");
                                            }
                                        }
                                    }
                                    log.info("🛠️ Items Parents: {}", item_parents);
                                    String stateValue=null;
                                    String parentValue= null;
                                    for(Map<String, Object> item: details){
                                        log.info("🛠️ idwin_state: {}", item.get("idwin_state").toString());
                                        if(item.containsKey("type_state") && item.containsKey("name_state")){
                                            int typeState = (int) item.get("type_state");
                                            stateValue = item.get("name_state").toString();
                                            if(typeState==0){
                                                key = item.get("idwin_state").toString();
                                                if(item_parents.containsKey(key)){//si idwin_state esta en item_parents
                                                    parentValue=item_parents.get(key);
                                                    if(!parentValue.isEmpty())
                                                        continue;
                                                    item_parents.put(key, stateValue);
                                                    details_rol_user.get("parents").add(stateValue);
                                                }else{
                                                    details_rol_user.get("state_win").add(stateValue);
                                                }
                                            }
                                            if(typeState==1){
                                                tmp_permissions.add(stateValue);
                                            }
                                        }
                                    }
                                }
                                UserDetailsResponseDTO dto_response = userEmployeeMapper.toDto(user);
                                log.info("🛠️ Detalles rol usuario: {}", details_rol_user);
                                log.info("🛠️ Permisos tmp_permissions: {}", tmp_permissions);
                                dto_response.setState_menu(details_rol_user);
                                dto_response.setPermissions(tmp_permissions);
                                if(rol_info.containsKey("name_rol")) {
                                    dto_response.setNo_rol(rol_info.get("name_rol").toString());
                                }
                                if(!tmp_permissions.isEmpty()){
                                    dto_response.setPermissions(tmp_permissions);
                                }
                                return Mono.just(dto_response);
                            });
                });
    }
    private Map<String, Object> parseJson(String json){
        try{
            return json != null ? OBJECT_MAPPER.readValue(json, Map.class) : Collections.emptyMap();
        }catch(Exception e){
            return Collections.emptyMap();
        }
    }
}