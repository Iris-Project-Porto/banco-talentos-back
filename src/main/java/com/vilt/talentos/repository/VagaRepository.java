package com.vilt.talentos.repository;

import com.vilt.talentos.entity.Vaga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VagaRepository extends JpaRepository<Vaga, UUID> {

    List<Vaga> findAllByOrderByDataAberturaDesc();

    List<Vaga> findByStatus(Vaga.StatusVaga status);

    List<Vaga> findBySenioridade(Vaga.Senioridade senioridade);

    long countByStatus(Vaga.StatusVaga status);

    long countBySenioridade(Vaga.Senioridade senioridade);

    long countByStatusAndSenioridade(Vaga.StatusVaga status, Vaga.Senioridade senioridade);
}
