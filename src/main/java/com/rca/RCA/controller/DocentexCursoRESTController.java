package com.rca.RCA.controller;

import com.rca.RCA.service.DocentexCursoService;
import com.rca.RCA.type.ApiResponse;
import com.rca.RCA.type.AulaDTO;
import com.rca.RCA.type.DocentexCursoDTO;
import com.rca.RCA.type.Pagination;
import com.rca.RCA.util.exceptions.AttributeException;
import com.rca.RCA.util.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/asignatura")
public class DocentexCursoRESTController {
    @Autowired
    DocentexCursoService docentexCursoService;

    public DocentexCursoRESTController(){
    }

    @GetMapping
    public ApiResponse<Pagination<DocentexCursoDTO>> list(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return this.docentexCursoService.getList(filter, page, size);
    }

    @GetMapping("alau")
    public ApiResponse<Pagination<DocentexCursoDTO>> list(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "") String anio,
            @RequestParam(defaultValue = "") String alumno,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return this.docentexCursoService.getList(filter, alumno, anio, page, size);
    }

    @GetMapping("ac")
    public ApiResponse<Pagination<DocentexCursoDTO>> listAc(
            @RequestParam(defaultValue = "") String filter,
            @RequestParam(defaultValue = "") String aula,
            @RequestParam(defaultValue = "") String curso,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return this.docentexCursoService.getListByAulaCurso(filter, aula, curso, page, size);
    }

    @GetMapping("{id}")
    public ApiResponse<DocentexCursoDTO> one(@PathVariable String id) throws ResourceNotFoundException {
        return this.docentexCursoService.one(id);
    }
    @PostMapping
    public ApiResponse<DocentexCursoDTO> add(@RequestBody DocentexCursoDTO docentexCursoDTO) throws ResourceNotFoundException, AttributeException {
        return this.docentexCursoService.add(docentexCursoDTO);
    }

    @PutMapping
    public ApiResponse<DocentexCursoDTO> update(@RequestBody DocentexCursoDTO docentexCursoDTO) throws ResourceNotFoundException, AttributeException {
        return this.docentexCursoService.update(docentexCursoDTO);
    }
    @DeleteMapping("{id}")
    public ApiResponse<DocentexCursoDTO> delete(@PathVariable String id) throws ResourceNotFoundException {
        return this.docentexCursoService.delete(id);
    }
}
