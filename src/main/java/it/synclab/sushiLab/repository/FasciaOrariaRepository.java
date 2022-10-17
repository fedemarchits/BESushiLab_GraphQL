package it.synclab.sushiLab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.synclab.sushiLab.entity.FasciaOraria;

@Repository
public interface FasciaOrariaRepository extends JpaRepository<FasciaOraria, Integer> {
    
}
