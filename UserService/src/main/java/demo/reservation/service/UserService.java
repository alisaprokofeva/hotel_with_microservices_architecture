package demo.reservation.service;

import demo.reservation.mapper.UserEntityMapper;
import demo.reservation.model.UserResponseDto;
import demo.reservation.model.entity.UserEntity;
import demo.reservation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository repository;
    private final UserEntityMapper mapper;


    public UserResponseDto getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return mapper.toUserResponseDto(user);
    }


    public UserEntity findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Transactional
    public void deleteUser(Long id) {
        repository.deleteById(id);
    }
}