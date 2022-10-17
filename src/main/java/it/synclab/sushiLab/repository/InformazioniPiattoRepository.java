package it.synclab.sushiLab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.synclab.sushiLab.entity.InformazioniPiatto;

@Repository
public interface InformazioniPiattoRepository extends JpaRepository<InformazioniPiatto, Integer> {
    
}
