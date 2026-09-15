package com.gov.solicitudes.application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Habilita las tareas programadas (el relay del Outbox corre en un @Scheduled). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
