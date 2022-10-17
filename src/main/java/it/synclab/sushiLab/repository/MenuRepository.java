package it.synclab.sushiLab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.synclab.sushiLab.entity.Menu;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Integer>{
    
}
