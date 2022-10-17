package it.synclab.sushiLab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.synclab.sushiLab.entity.PiattoUpload;

@Repository
public interface PiattoUploadRepository extends JpaRepository<PiattoUpload, Integer> {
    
}
