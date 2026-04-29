package demo.reservation.service;

import demo.reservation.model.entity.TemporaryPassword;
import demo.reservation.model.entity.UserEntity;
import demo.reservation.repository.TemporaryPasswordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TemporaryPasswordService {

    private final TemporaryPasswordRepository repository;
    private final PasswordEncoder passwordEncoder;

    private static final int TEMP_PASSWORD_COUNT = 10;
    private static final int TEMP_PASSWORD_VALIDITY_HOURS = 24;

    public List<String> generateTemporaryPasswords(UserEntity user) {

        repository.deleteAllByUser(user);

        List<String> generatedPasswords = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(TEMP_PASSWORD_VALIDITY_HOURS);


        for (int i = 0; i < TEMP_PASSWORD_COUNT; i++) {
            String tempPassword = UUID.randomUUID().toString().replace("-", "");
            generatedPasswords.add(tempPassword);

            TemporaryPassword temp = TemporaryPassword.builder()
                    .user(user)
                    .hashedPassword(passwordEncoder.encode(tempPassword))
                    .isUsed(false)
                    .createdAt(now)
                    .expiresAt(expiresAt)
                    .build();

            repository.save(temp);
        }

        return generatedPasswords;
    }

    public boolean validateAndUseTemporaryPassword(
            UserEntity user,
            String tempPassword
    ) {
        List<TemporaryPassword> unusedPasswords = repository.findAllByUserAndIsUsedFalse(user);

        for (TemporaryPassword tp : unusedPasswords) {

            if (LocalDateTime.now().isAfter(tp.getExpiresAt())) {
                continue;
            }

            if (passwordEncoder.matches(tempPassword, tp.getHashedPassword())) {
                tp.setIsUsed(true);
                repository.save(tp);
                return true;
            }
        }

        return false;
    }
}

