package com.rca.RCA.repository;

import com.rca.RCA.entity.AsistenciaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<AsistenciaEntity, Integer> {
    @Query(value = "select a from AsistenciaEntity a " +
            "where a.status = :status " +
            "and ( a.code like concat('%', :filter, '%') or a.state like concat('%', :filter, '%') ) ")
    Optional<List<AsistenciaEntity>> findEntities(String status, String filter, Pageable pageable);

    @Query(value = "select count(a) from AsistenciaEntity a " +
            "where a.status = :status " +
            "and ( a.code like concat('%', :filter, '%') or a.state like concat('%', :filter, '%') ) ")
    Long findCountEntities(String status, String filter);


    Optional<AsistenciaEntity> findByUniqueIdentifier(String uniqueIdentifier);

}