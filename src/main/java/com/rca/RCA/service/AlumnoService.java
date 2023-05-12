package com.rca.RCA.service;

import com.rca.RCA.auth.entity.Rol;
import com.rca.RCA.auth.enums.RolNombre;
import com.rca.RCA.auth.repository.RolRepository;
import com.rca.RCA.auth.service.LoginService;
import com.rca.RCA.entity.AlumnoEntity;
import com.rca.RCA.entity.ApoderadoEntity;
import com.rca.RCA.entity.GradoEntity;
import com.rca.RCA.entity.UsuarioEntity;
import com.rca.RCA.repository.*;
import com.rca.RCA.type.*;
import com.rca.RCA.util.Code;
import com.rca.RCA.util.ConstantsGeneric;
import com.rca.RCA.util.exceptions.AttributeException;
import com.rca.RCA.util.exceptions.ResourceNotFoundException;
import lombok.extern.log4j.Log4j2;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
public class AlumnoService {

    @Autowired
    private AlumnoRepository alumnoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ApoderadoRepository apoderadoRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private EvaluacionRepository evaluacionRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private LoginService loginService;

    @Autowired
    private UsuarioService usuarioService;

    public AlumnoService(AlumnoRepository alumnoRepository, UsuarioRepository usuarioRepository,
                         ApoderadoRepository apoderadoRepository, AsistenciaRepository asistenciaRepository,
                         EvaluacionRepository evaluacionRepository, RolRepository rolRepository){
        this.alumnoRepository = alumnoRepository;
        this.usuarioRepository = usuarioRepository;
        this.apoderadoRepository = apoderadoRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.rolRepository = rolRepository;
    }

    public ApiResponse<Pagination<AlumnoDTO>> getList(String filter, int page, int size) {
        log.info("filter page size {} {} {}", filter, page, size);
        ApiResponse<Pagination<AlumnoDTO>> apiResponse = new ApiResponse<>();
        Pagination<AlumnoDTO> pagination = new Pagination<>();
        pagination.setCountFilter(this.alumnoRepository.findCountEntities(ConstantsGeneric.CREATED_STATUS, filter));
        if (pagination.getCountFilter() > 0) {
            Pageable pageable = PageRequest.of(page, size);
            List<AlumnoEntity> AlumnoEntities = this.alumnoRepository.findEntities(ConstantsGeneric.CREATED_STATUS, filter, pageable).orElse(new ArrayList<>());
            pagination.setList(AlumnoEntities.stream().map(AlumnoEntity::getAlumnoDTO).collect(Collectors.toList()));
        }
        pagination.setTotalPages(pagination.processAndGetTotalPages(size));
        apiResponse.setData(pagination);
        apiResponse.setSuccessful(true);
        apiResponse.setMessage("ok");
        return apiResponse;
    }

    public ApiResponse<AlumnoDTO> one(String id) throws ResourceNotFoundException {
        AlumnoEntity alumnoEntity=this.alumnoRepository.findByUniqueIdentifier(id).orElseThrow(()-> new ResourceNotFoundException("Alumno no encontrado"));
        ApiResponse<AlumnoDTO> apiResponse = new ApiResponse<>();
        apiResponse.setSuccessful(true);
        apiResponse.setMessage("ok");
        apiResponse.setData(alumnoEntity.getAlumnoDTO());
        return apiResponse;
    }

    //Agregar Alumno
    public ApiResponse<AlumnoDTO> add(AlumnoDTO AlumnoDTO) throws AttributeException, ResourceNotFoundException {

        //Verifica que el rol sea docente
        if (!AlumnoDTO.getUsuarioDTO().getRol().equalsIgnoreCase("STUDENT"))
            throw new AttributeException("El rol es inválido");
        log.info("Rol existe");
        ApiResponse<UsuarioDTO> apiResponseU = this.loginService.add(AlumnoDTO.getUsuarioDTO());
        ApiResponse<AlumnoDTO> apiResponse = new ApiResponse<>();
        if (apiResponseU.isSuccessful()) {
            log.info("Agregó al usuario");
            //AlumnoDTO add data
        AlumnoDTO.setId(UUID.randomUUID().toString());
        AlumnoDTO.setCode(Code.generateCode(Code.ALU_CODE, this.alumnoRepository.count() + 1, Code.ALU_LENGTH));
        AlumnoDTO.setStatus(ConstantsGeneric.CREATED_STATUS);
        AlumnoDTO.setCreateAt(LocalDateTime.now());
        //change dto to entity
        AlumnoEntity AlumnoEntity = new AlumnoEntity();
        AlumnoEntity.setAlumnoDTO(AlumnoDTO);

        AlumnoEntity.setUsuarioEntity(this.usuarioRepository.findByUniqueIdentifier(apiResponseU.getData().getId(), ConstantsGeneric.CREATED_STATUS).get());
        AlumnoEntity.setApoderadoEntity(this.apoderadoRepository.findByUniqueIdentifier(AlumnoDTO.getApoderadoDTO().getId(), ConstantsGeneric.CREATED_STATUS).get());

        apiResponse.setData(this.alumnoRepository.save(AlumnoEntity).getAlumnoDTO());
        apiResponse.setSuccessful(true);
        apiResponse.setMessage("ok");

        return apiResponse;
    }else {
            log.warn("No se agregó el registro");
            apiResponse.setSuccessful(false);
            apiResponse.setCode("ALUMNO_EXISTS");
            apiResponse.setMessage(apiResponseU.getMessage());
            return apiResponse;

        }
    }

    //Modificar Alumno
    public ApiResponse<AlumnoDTO> update(AlumnoDTO alumnoDTO) throws ResourceNotFoundException {
        if(alumnoDTO.getId().isBlank())
            throw new ResourceNotFoundException("Alumno no encontrado");

        ApiResponse<AlumnoDTO> apiResponse = new ApiResponse<>();

        AlumnoEntity alumnoEntity = this.alumnoRepository.findByUniqueIdentifier(alumnoDTO.getId()).orElseThrow(()->new ResourceNotFoundException("Alumno no existe"));

        //change dto to entity
        alumnoEntity.setDiseases(alumnoDTO.getDiseases());
        alumnoEntity.setUpdateAt(LocalDateTime.now());
        alumnoEntity.setNamecon_pri(alumnoDTO.getNamecon_pri());
        alumnoEntity.setTelcon_pri(alumnoDTO.getTelcon_pri());
        alumnoEntity.setNamecon_sec(alumnoDTO.getNamecon_sec());
        alumnoEntity.setTelcon_sec(alumnoDTO.getTelcon_sec());
        alumnoEntity.setVaccine(alumnoDTO.getVaccine());
        alumnoEntity.setType_insurance(alumnoDTO.getType_insurance());

        alumnoEntity.setUsuarioEntity(this.usuarioRepository.findByUniqueIdentifier(alumnoDTO.getId(), ConstantsGeneric.CREATED_STATUS).orElseThrow(()->new ResourceNotFoundException("Usuario no existe")));
        alumnoEntity.setApoderadoEntity(this.apoderadoRepository.findByUniqueIdentifier(alumnoDTO.getId(),ConstantsGeneric.CREATED_STATUS).orElseThrow(()-> new ResourceNotFoundException("Apoderado no existe")));

        //Update in database to usuario
        ApiResponse<UsuarioDTO> apiResponseU = this.usuarioService.update(alumnoEntity.getUsuarioEntity().getUsuarioDTO());
        if (apiResponseU.isSuccessful()) {
            //Update in database to docente
            apiResponse.setSuccessful(true);
            apiResponse.setMessage("ok");
            apiResponse.setData(this.alumnoRepository.save(alumnoEntity).getAlumnoDTO());
            return apiResponse;
        } else {
            apiResponse.setSuccessful(false);
            apiResponse.setMessage(apiResponseU.getMessage());
            return apiResponse;
        }
    }

    //Borrar Alumno
    public ApiResponse<AlumnoDTO> delete(String id) {
        ApiResponse<AlumnoDTO> apiResponse = new ApiResponse<>();
        Optional<AlumnoEntity> optionalAlumnoEntity = this.alumnoRepository.findByUniqueIdentifier(id);
        Long asistencias = this.asistenciaRepository.findCountEntities(ConstantsGeneric.CREATED_STATUS, id);
        Long evaluaciones = this.evaluacionRepository.findCountEntities(ConstantsGeneric.CREATED_STATUS, id);
        if (optionalAlumnoEntity.isPresent()) {

            if (asistencias > 0){
                this.alumnoRepository.deleteAsistencia(id, LocalDateTime.now());
            }

            if (evaluaciones > 0){
                this.alumnoRepository.deleteEvaluciones(id, LocalDateTime.now());
            }

            this.alumnoRepository.deleteUsuario(id, LocalDateTime.now());

            AlumnoEntity AlumnoEntity = optionalAlumnoEntity.get();
            AlumnoEntity.setStatus(ConstantsGeneric.DELETED_STATUS);
            AlumnoEntity.setDeleteAt(LocalDateTime.now());

            apiResponse.setSuccessful(true);
            apiResponse.setMessage("ok");
            apiResponse.setData(this.alumnoRepository.save(AlumnoEntity).getAlumnoDTO());

        } else {
            apiResponse.setSuccessful(false);
            apiResponse.setCode("ROL_DOES_NOT_EXISTS");
            apiResponse.setMessage("No existe el alumno para poder eliminar");
        }

        return apiResponse;
    }

    public ResponseEntity<Resource> datosPersonales(String uniqueIdentifier) {
        Optional<AlumnoEntity> optionalAlumnoEntity = this.alumnoRepository.findByUniqueIdentifier(uniqueIdentifier);

        if (optionalAlumnoEntity.isPresent()){

            try{
                final AlumnoEntity alumnoEntity = optionalAlumnoEntity.get();
                final ApoderadoEntity apoderadoEntity = alumnoEntity.getApoderadoEntity();
                final UsuarioEntity usuarioEntity = alumnoEntity.getUsuarioEntity();
                final File file = ResourceUtils.getFile("classpath:reportes/reporte.jasper");
                final File imgLogo = ResourceUtils.getFile("classpath:images/logo.png");
                final JasperReport report = (JasperReport) JRLoader.loadObject(file);



                //Parámetros
                final HashMap<String, Object> parameters = new HashMap<>();
                parameters.put("codeAlu", alumnoEntity.getCode());
                parameters.put("nombreAlu", alumnoEntity.getUsuarioEntity().getNameCompleto());
                parameters.put("emailAlu", usuarioEntity.getEmail());
                parameters.put("seguro", alumnoEntity.getType_insurance());
                parameters.put("enfAlu", alumnoEntity.getDiseases());
                parameters.put("docAlu", usuarioEntity.getType_doc());
                parameters.put("numDocAlu", usuarioEntity.getNumdoc());
                parameters.put("telAlu", usuarioEntity.getTel());
                parameters.put("vacunas", alumnoEntity.getVaccine());
                parameters.put("logo", new FileInputStream(imgLogo));
                parameters.put("nombreCon1", alumnoEntity.getNamecon_pri());
                parameters.put("nombreCon2", alumnoEntity.getNamecon_sec());
                parameters.put("telCon1", alumnoEntity.getTelcon_pri());
                parameters.put("telCon2", alumnoEntity.getTelcon_sec());
                parameters.put("nombreApo",apoderadoEntity.getNameCompleto());
                parameters.put("correoApo",apoderadoEntity.getEmail());
                parameters.put("typeDocApo",apoderadoEntity.getType_doc());
                parameters.put("numDocApo",apoderadoEntity.getNumdoc());
                parameters.put("telApo",apoderadoEntity.getTel());
                parameters.put("correoPerApo",apoderadoEntity.getEmail());


                JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, new JREmptyDataSource());
                byte[] reporte = JasperExportManager.exportReportToPdf(jasperPrint);
                String sdf = (new SimpleDateFormat("dd/MM/yyyy")).format(new Date());
                StringBuilder stringBuilder = new StringBuilder().append("InvoicePDF:");
                ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                        .filename(stringBuilder.append("1")
                                .append("generateDate:")
                                .append(sdf)
                                .append(".pdf")
                                .toString())
                        .build();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentDisposition(contentDisposition);

                return ResponseEntity.ok().contentLength((long) reporte.length)
                        .contentType(MediaType.APPLICATION_PDF)
                        .headers(httpHeaders).body(new ByteArrayResource(reporte));

            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            return  ResponseEntity.noContent().build();
        }
        return null;
    }
}

