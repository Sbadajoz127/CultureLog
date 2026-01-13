package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    // Buscar una etiqueta por su nombre exacto
    Optional<Tag> findByName(String name);
}
