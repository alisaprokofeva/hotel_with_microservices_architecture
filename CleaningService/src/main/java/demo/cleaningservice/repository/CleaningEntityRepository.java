package demo.cleaningservice.repository;


import demo.cleaningservice.model.entity.CleaningEntity;
import demo.cleaningservice.model.status.CleanerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CleaningEntityRepository  extends JpaRepository<CleaningEntity, Long> {

    public List<CleaningEntity> findCleaningEntitiesByCleanerStatus(CleanerStatus cleanerStatus);

}
