package com.gestor.users.configuration;

import com.gestor.users.model.RoleList;
import com.gestor.users.model.User;
import com.gestor.users.repository.RoleRepository;
import com.gestor.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GuestCleanupTask {

    private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    private final String[] microservicios = {
            "https://leads-services-976321280197.us-central1.run.app/api/leads/cleanup",
            "https://cliente-services-976321280197.us-central1.run.app/api/clientes/cleanup",
            "https://ventas-services-976321280197.us-central1.run.app/api/oportunidades/cleanup",
            "https://microservicios-services-976321280197.us-central1.run.app/api/servicios/cleanup"
    };

    @Scheduled(fixedRate = 2 * 60 * 1000) // cada 2 minutos
    public void limpiarDatosDelInvitado() {
        Optional<User> userOpt = userRepository.findByUsername("invitado");

        if (userOpt.isPresent()) {
            Long userId = userOpt.get().getId();

            for (String url : microservicios) {
                try {
                    webClientBuilder.build()
                            .delete()
                            .uri(url)
                            .header("X-User-Id", String.valueOf(userId))
                            .header("X-Role", "ROLE_INVITED")
                            .retrieve()
                            .toBodilessEntity()
                            .block(); // Espera que la petición termine

                    log.info("🧼 Limpieza realizada en: {}", url);
                } catch (Exception e) {
                    log.error("❌ Falló la limpieza en {}: {}", url, e.getMessage());
                }
            }
        } else {
            log.warn("⚠️ No se encontró el usuario 'invitado' para limpieza.");
        }
    }


}
