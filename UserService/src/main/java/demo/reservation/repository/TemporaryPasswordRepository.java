package demo.reservation.repository;

import demo.reservation.model.entity.TemporaryPassword;
import demo.reservation.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemporaryPasswordRepository extends JpaRepository<TemporaryPassword, Long> {

    List<TemporaryPassword> findAllByUserAndIsUsedFalse(UserEntity user);

    Optional<TemporaryPassword> findByIdAndUserAndIsUsedFalse(Long id, UserEntity user);

    long countByUserAndIsUsedFalse(UserEntity user);

    void deleteAllByUser(UserEntity user);
}

