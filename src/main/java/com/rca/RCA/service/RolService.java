package com.rca.RCA.service;

import com.rca.RCA.entity.RolEntity;
import com.rca.RCA.repository.RolRepository;
import com.rca.RCA.type.ApiResponse;
import com.rca.RCA.type.Pagination;
import com.rca.RCA.type.RolDTO;
import com.rca.RCA.util.Code;
import com.rca.RCA.util.ConstantsGeneric;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Log4j2
@Service
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    public Pagination<RolDTO> getList(String filter, int page, int size) {

        Pagination<RolDTO> pagination = new Pagination();
        pagination.setCountFilter(this.rolRepository.findCountEntities(ConstantsGeneric.CREATED_STATUS, filter));
        if (pagination.getCountFilter() > 0) {
            Pageable pageable = PageRequest.of(page, size);
            List<RolEntity> RolEntities = this.rolRepository.findEntities(ConstantsGeneric.CREATED_STATUS, filter, pageable).orElse(new ArrayList<>());
            pagination.setList(RolEntities.stream().map(RolEntity::getRolDTO).collect(Collectors.toList()));
        }
        pagination.setTotalPages(pagination.processAndGetTotalPages(size));
        return pagination;
    }

    //Agregar Rol
    public ApiResponse<RolDTO> add(RolDTO RolDTO) {
        ApiResponse<RolDTO> apiResponse = new ApiResponse<>();
        System.out.println(RolDTO.toString());
        RolDTO.setId(UUID.randomUUID().toString());
        RolDTO.setCode(Code.generateCode(Code.ROL_CODE, this.rolRepository.count() + 1, Code.ROL_LENGTH));
        RolDTO.setStatus(ConstantsGeneric.CREATED_STATUS);
        RolDTO.setCreateAt(LocalDateTime.now());
        //validamos
        Optional<RolEntity> optionalRolEntity = this.rolRepository.findByName(RolDTO.getName());
        if (optionalRolEntity.isPresent()) {
            apiResponse.setSuccessful(false);
            apiResponse.setCode("Rol_EXISTS");
            apiResponse.setMessage("No se registro, el rol existe");
            return apiResponse;
        }
        //change dto to entity
        RolEntity RolEntity = new RolEntity();
        RolEntity.setRolDTO(RolDTO);

        apiResponse.setData(this.rolRepository.save(RolEntity).getRolDTO());
        apiResponse.setSuccessful(true);
        apiResponse.setMessage("ok");
        return apiResponse;
    }

    //Modificar Rol
    public void update(RolDTO RolDTO) {
        Optional<RolEntity> optionalRolEntity = this.rolRepository.findByUniqueIdentifier(RolDTO.getId());
        if (optionalRolEntity.isPresent()) {
            RolDTO.setUpdateAt(LocalDateTime.now());
            //validamos que no se repita
            Optional<RolEntity> optionalRolEntityValidation = this.rolRepository.findByName(RolDTO.getName(), RolDTO.getName());
            if (optionalRolEntityValidation.isPresent()) {
                System.out.println("No se actulizo, la categoria existe");
                return;
            }
            RolEntity RolEntity = optionalRolEntity.get();
            //set update data
            if (RolDTO.getCode() != null) {
                RolEntity.setCode(RolDTO.getCode());
            }
            if (RolDTO.getName() != null) {
                RolEntity.setName(RolDTO.getName());
            }
            RolEntity.setUpdateAt(RolDTO.getUpdateAt());
            //update in database
            this.rolRepository.save(RolEntity);
        } else {
            System.out.println("No existe la categoria para poder actualizar");
        }
    }

    //Borrar Rol
    public void delete(String id) {
        Optional<RolEntity> optionalRolEntity = this.rolRepository.findByUniqueIdentifier(id);
        if (optionalRolEntity.isPresent()) {
            RolEntity RolEntity = optionalRolEntity.get();
            RolEntity.setStatus(ConstantsGeneric.DELETED_STATUS);
            RolEntity.setDeleteAt(LocalDateTime.now());
            this.rolRepository.save(RolEntity);
        } else {
            System.out.println("No existe el Rol para poder eliminar");
        }
    }
}
