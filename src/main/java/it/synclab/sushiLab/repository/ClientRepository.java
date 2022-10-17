package it.synclab.sushiLab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.synclab.sushiLab.entity.Utente;

@Repository
public interface ClientRepository extends JpaRepository<Utente, String> {
    
}
