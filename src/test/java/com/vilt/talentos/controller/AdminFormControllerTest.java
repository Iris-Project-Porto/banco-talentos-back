package com.vilt.talentos.controller;

import com.vilt.talentos.dto.FormCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class AdminFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<FormCreateRequest> formCreateRequestJacksonTester;

    @Test
    @DisplayName("Deve retornar codigo 201 ao criar nova definição de formulário")
    @WithMockUser
    void criarDefinicaoCenario01() throws Exception{
        var response = mockMvc.perform(post("/")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("")
    @WithMockUser
    void criarDefinicaoCenario02() throws Exception{
        var response = mockMvc.perform(post("/")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED);
    }
}