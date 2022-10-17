package it.synclab.sushiLab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.synclab.sushiLab.entity.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, String>{
    
}
