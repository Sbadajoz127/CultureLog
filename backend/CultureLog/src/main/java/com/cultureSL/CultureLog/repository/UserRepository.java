package com.cultureSL.CultureLog.repository;

import com.cultureSL.CultureLog.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Buscar usuario por nombre (para el Login)
    // Devuelve Optional por si el usuario no existe
    Optional<User> findByUsername(String username);
    
    // Métodos útiles para validaciones al registrarse
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}