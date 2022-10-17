package it.synclab.sushiLab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.synclab.sushiLab.entity.Ordine;

@Repository
public interface OrdineRepository extends JpaRepository<Ordine, Integer> {
    
}
