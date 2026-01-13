package com.cultureSL.CultureLog.service;

import com.cultureSL.CultureLog.model.User;
import java.util.Optional;

public interface UserService {
    
    // Método para registrar un usuario (encriptando la pass)
    User registerUser(User user) throws Exception;
    
    // Método para hacer login
    // Devuelve el usuario si el login es correcto, o vacío si falla
    Optional<User> login(String username, String rawPassword);
    
    // Método auxiliar
    boolean exists(String username);
}