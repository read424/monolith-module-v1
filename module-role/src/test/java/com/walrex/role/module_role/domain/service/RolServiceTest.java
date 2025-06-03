package com.walrex.role.module_role.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import com.walrex.role.module_role.application.ports.output.RolDetailsOutputPort;
import com.walrex.role.module_role.application.ports.output.RolOutputPort;
import com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.entity.RolDetails;
import com.walrex.role.module_role.infrastructure.adapters.outbound.persistence.entity.RolEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@ExtendWith(MockitoExtension.class)
public class RolServiceTest {

    @Mock
    private RolOutputPort rolOutputPort;

    @Mock
    private RolDetailsOutputPort rolDetailsOutputPort;

    @InjectMocks
    private RolService rolService;

    private RolEntity mockRol;
    private RolDetails mockDetail;

    @BeforeEach
    void setUp(){
        mockRol = new RolEntity(1L, "Administrador", "Tiene el Control Total del sistema", true);
        mockDetail = new RolDetails(1L, 1L, "dashboard.rrhh", 0,1);
    }

    @Test
    void getDetailsRolById_shouldReturnRolDetailDTO_whenRolExists(){
        when(rolOutputPort.getInfoRol(1L).thenReturn(Mono.just(mockRol)));
        when(rolDetailsOutputPort.getDetailsRoles(1L)).thenReturn(Flux.just(mockDetail));

        StepVerifier.create(rolService.getDetailsRolById(1L))
                .assertNext(rolDetailDTO -> {
                   assertEquals("Administrador", rolDetailDTO.getName_rol());
                   assertEquals(1, rolDetailDTO.getDetails().size());
                })
                .verifyComplete();
    }

    @Test
    void getDetailsRolById_shouldReturnError_whenRolDoesNotExists(){
        when(rolOutputPort.getInfoRol(1L)).thenReturn(Mono.empty());

        StepVerifier.create(rolService.getDetailsRolById(1L))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Rol no encontrado con el ID: 1"))
                .verify();
    }

}
