package demo.cleaningservice.domain;


import org.springframework.data.jpa.repository.JpaRepository;

public interface CleaningEntityRepository  extends JpaRepository<CleaningEntity, Long> {
}
