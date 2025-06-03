package com.walrex.user.module_users.domain.service;

import com.walrex.user.module_users.common.security.PasswordEncryptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserDomainService {

    public boolean matches(String rawPassword, String encryptedPassword) {
        String hashedPassword = PasswordEncryptor.encryptPassword(rawPassword);
        log.info("encryptedPassword {} hashedPassword {}", encryptedPassword, hashedPassword);
        return hashedPassword.equals(encryptedPassword);
    }

}
