package it.synclab.sushiLab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.synclab.sushiLab.entity.IdToken;

@Repository
public interface IdTokenRepository extends JpaRepository<IdToken, String> {
    
}
