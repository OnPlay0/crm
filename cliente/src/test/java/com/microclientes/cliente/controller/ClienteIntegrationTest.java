package com.microclientes.cliente.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microclientes.cliente.dto.ClienteDTO;
import com.microclientes.cliente.model.EstadoCliente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
class ClienteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @WithMockUser(username = "99", authorities = "ROLE_USER")
    void crearClienteYObtenerlo() throws Exception {
        ClienteDTO cliente = new ClienteDTO();
        cliente.setNombre("Carlos");
        cliente.setApellido("Cesar");
        cliente.setEmail("carlos@test.com");
        cliente.setTelefono("1155588899");
        cliente.setDireccion("Calle Test 123");
        cliente.setEstado(EstadoCliente.ACTIVO);

        String json = mapper.writeValueAsString(cliente);

        // POST /api/clientes
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Carlos"));

        // GET /api/clientes (lista paginada)
        mockMvc.perform(get("/api/clientes")
                        .param("size", "10")
                        .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", containsString("1")))
                .andExpect(content().string(containsString("Carlos")));
    }
}
