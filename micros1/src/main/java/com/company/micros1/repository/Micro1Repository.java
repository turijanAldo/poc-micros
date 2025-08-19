package com.company.micros1.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.company.micros1.model.Solicitud;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
@Repository
public interface Micro1Repository extends JpaRepository<Solicitud, Long> {
     
     @Modifying
	 @Transactional
     @Query(value = "update solicitudes                  "
	  		      + "set  fecha_envio_s1     = :fecha,   "
                  + "     status             = 20,       "
                  + "     status_string      = 'Solicitud enviada al sistema de información 1'    "
	  		      + "WHERE  id_solicitud     = :id       ", 
	  		   nativeQuery = true)
	  public int updateEnvioDestino1(@Param("fecha") LocalDateTime fechaDestino1,
			                         @Param("id") Long id);
    
     @Modifying
	 @Transactional
     @Query(value = "update solicitudes                  "
	  		      + "set  fecha_respuesta_s1 = :fecha,    "
                  + "     status             = 30,       "
                  + "     status_string      = 'Solicitud recibida del sistema de información 1'    "
	  		      + "WHERE  id_solicitud     = :id       ", 
	  		   nativeQuery = true)
	  public int updateRespuestaDestino1(@Param("fecha") LocalDateTime fechaDestino1,
			                             @Param("id") Long id);
                                
     @Modifying
	 @Transactional
     @Query(value = "update solicitudes                  "
	  		      + "set  fecha_envio_s2     = :fecha,    "
                  + "     status             = 40,       "
                  + "     status_string      = 'Solicitud enviada al sistema de información 2'    "
	  		      + "WHERE  id_solicitud     = :id       ", 
	  		   nativeQuery = true)
	  public int updateEnvioDestino2(@Param("fecha") LocalDateTime fechaDestino1,
			                        @Param("id") Long id);
    
     @Modifying
	 @Transactional
     @Query(value = "update solicitudes                  "
	  		      + "set  fecha_respuesta_s2 = :fecha,    "
                  + "     status             = 50,       "
                  + "     status_string      = 'Solicitud recibida del sistema de información 2'    "
	  		      + "WHERE  id_solicitud     = :id       ", 
	  		   nativeQuery = true)
	  public int updateRespuestaDestino2(@Param("fecha") LocalDateTime fechaDestino1,
			                             @Param("id") Long id);
                                     

	// Método para encontrar todas las solicitudes con un estado específico
    List<Solicitud> findByStatus(Integer status);
}
