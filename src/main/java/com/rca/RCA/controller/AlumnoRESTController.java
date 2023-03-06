package com.rca.RCA.controller;

import com.rca.RCA.service.AlumnoService;
import com.rca.RCA.type.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("alumno")
public class AlumnoRESTController {

    @Autowired
    private AlumnoService alumnoService;

    public AlumnoRESTController(){

    }

    @GetMapping
    public ApiResponse<Pagination<AlumnoDTO>> list(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return this.alumnoService.getList(filter, page, size);
    }

    @PostMapping
    public ApiResponse<AlumnoDTO> add(@RequestBody @Valid AlumnoDTO AlumnoDTO) {
        return this.alumnoService.add(AlumnoDTO);
    }

    @PutMapping
    public ApiResponse<AlumnoDTO> update(@RequestBody AlumnoDTO alumnoDTO) {
        return this.alumnoService.update(alumnoDTO);
    }

    @DeleteMapping("{id}")
    public ApiResponse<AlumnoDTO> delete(@PathVariable String id) {
         return this.alumnoService.delete(id);
    }
    
}