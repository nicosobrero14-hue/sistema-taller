package com.tallerapp.usuarios_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tallerapp.usuarios_service.entity.ConfiguracionTaller;

@Repository
public interface IConfiguracionRepository extends JpaRepository<ConfiguracionTaller, Long> {

}
