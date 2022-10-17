package it.synclab.sushiLab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.synclab.sushiLab.entity.Blacklist;

@Repository
public interface BlacklistRepository extends JpaRepository<Blacklist, Integer> {
    
}
