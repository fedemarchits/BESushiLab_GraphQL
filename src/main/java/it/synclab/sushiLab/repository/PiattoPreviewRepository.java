package it.synclab.sushiLab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.synclab.sushiLab.entity.PiattoPreview;


@Repository
public interface PiattoPreviewRepository extends JpaRepository<PiattoPreview, Integer> {
    
}
