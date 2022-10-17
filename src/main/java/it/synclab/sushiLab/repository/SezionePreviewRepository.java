package it.synclab.sushiLab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.synclab.sushiLab.entity.SezionePreview;

@Repository
public interface SezionePreviewRepository extends JpaRepository<SezionePreview, Integer>{
    
}
