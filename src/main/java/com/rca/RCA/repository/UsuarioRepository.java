package com.rca.RCA.repository;


import com.rca.RCA.entity.UsuarioEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Integer>{


    //Función para obtener un usaurio con filtro por codigo,nombre, apellidos
    @Query(value = "select u from UsuarioEntity u " +
            "where u.status = :status " +
            "and (u.code like concat('%', :filter, '%') or u.pa_surname like concat('%', :filter, '%') or " +
            "u.ma_surname like concat('%', :filter, '%') or u.name like concat('%', :filter, '%') or " +
            "u.numdoc like concat('%', :filter, '%'))")
    Optional<List<UsuarioEntity>> findEntities(String status, String filter, Pageable pageable);

    //Función para contar los usuarios
    @Query(value = "select count(u) from UsuarioEntity u " +
            "where u.status = :status " +
            "and (u.code like concat('%', :filter, '%') or u.pa_surname like concat('%', :filter, '%') or " +
            "u.ma_surname like concat('%', :filter, '%') or u.name like concat('%', :filter, '%') or " +
            "u.numdoc like concat('%', :filter, '%'))")
    Long findCountEntities(String status, String filter);

    @Query(value = "select count(*) from usuario u JOIN rol r where r.id = u.rol_id " +
            "and u.tx_status = :status and r.tx_status = :status " +
            "and r.tx_unique_identifier = :uniqueIdentifier", nativeQuery = true)
    Long findCountEntitiesRol(@Param("status") String status, @Param("uniqueIdentifier") String uniqueIdentifier);

    //Funcipon para encontrar un usuario por su identificador
    Optional<UsuarioEntity> findByUniqueIdentifier(String uniqueIdentifier);

    //Función para encontrar un usuario por su numero de documento
    Optional<UsuarioEntity> findByNumdoc(String numdoc);

    //Función para encontrar un usuario por su telefono
    Optional<UsuarioEntity> findByTel(String tel);

    @Query(value = "select u from UsuarioEntity u " +
            "where u.numdoc = :numdoc and u.uniqueIdentifier <> :uniqueIdentifier ")
    Optional<UsuarioEntity> findByNumdoc(String numdoc, String uniqueIdentifier);

    //Función para eliminar imágenes asociadas al usuario
    @Transactional
    @Modifying
    @Query(value="update imagen i JOIN usuario u  SET i.tx_status = 'DELETED', i.tx_delete_at = :fecha " +
            "where i.user_id = u.id" +
            " and u.tx_unique_identifier = :uniqueIdentifier", nativeQuery = true)
    void deleteImagen(@Param("uniqueIdentifier") String uniqueIdentifier, @Param("fecha") LocalDateTime fecha);

    //Función para eliminar noticias asociadas al usuario
    @Transactional
    @Modifying
    @Query(value="update noticia n JOIN usuario u  SET n.tx_status = 'DELETED', n.tx_delete_at = :fecha " +
            "where n.user_id = u.id " +
            "and u.tx_unique_identifier = :uniqueIdentifier", nativeQuery = true)
    void deleteNoticia(@Param("uniqueIdentifier") String uniqueIdentifier, @Param("fecha") LocalDateTime fecha);


    Optional<UsuarioEntity> findByNombreUsuario(String nombreUsuario);
    Optional<UsuarioEntity> findByNombreUsuarioOrEmail(String nombreUsuario, String email);
    Optional<UsuarioEntity> findByTokenPassword(String tokenPassword);
    boolean existsByNombreUsuario(String nombreUsuario);
    boolean existsByEmail(String email);
}



