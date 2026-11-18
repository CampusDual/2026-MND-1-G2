package com.campusdual.cd2026mnd1g1.ws.controller;

import com.campusdual.cd2026mnd1g1.api.dto.*;
import com.campusdual.cd2026mnd1g1.api.service.RequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class RequestController {
    private final RequestService requestService;
    public RequestController(RequestService requestService) { this.requestService = requestService; }
    @GetMapping("/categories")
    public ResponseEntity<List<RequestCategoryDto>> categories(@RequestParam String type) {
        return ResponseEntity.ok(requestService.findCategories(type));
    }
    @GetMapping("/categories/{categoryId}/subcategories")
    public ResponseEntity<List<RequestSubcategoryDto>> subcategories(@PathVariable Long categoryId) {
        return ResponseEntity.ok(requestService.findSubcategories(categoryId));
    }
    @PostMapping("/requests")
    public ResponseEntity<ServiceRequestDto> create(@RequestBody ServiceRequestCreateDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.create(request));
    }
    @GetMapping("/requests")
    public ResponseEntity<List<ServiceRequestDto>> findAll() {
        return ResponseEntity.ok(requestService.findAll());
    }
    @GetMapping("/requests/{id}")
    public ResponseEntity<ServiceRequestDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(requestService.findById(id));
    }

    /**
     * Recupera solicitudes pendientes sin asignar de un departamento.
     *
     * @param department departamento seleccionado
     * @return solicitudes pendientes
     */
    @GetMapping("/worker-requests/unassigned")
    public ResponseEntity<List<WorkerRequestDto>> findUnassigned(@RequestParam String department) {
        return ResponseEntity.ok(requestService.findUnassignedByDepartment(department));
    }

    /**
     * Recupera las solicitudes asignadas o en curso del trabajador autenticado.
     *
     * @param department departamento seleccionado
     * @param principal usuario autenticado
     * @return solicitudes asignadas
     */
    @GetMapping("/worker-requests/assigned")
    public ResponseEntity<List<WorkerRequestDto>> findAssigned(
            @RequestParam String department,
            Principal principal) {
        return ResponseEntity.ok(requestService.findAssignedByDepartment(department, principal.getName()));
    }

    /**
     * Recupera las solicitudes finalizadas del trabajador autenticado.
     *
     * @param department departamento seleccionado
     * @param principal usuario autenticado
     * @return solicitudes finalizadas
     */
    @GetMapping("/worker-requests/resolved")
    public ResponseEntity<List<WorkerRequestDto>> findResolved(
            @RequestParam String department,
            Principal principal) {
        return ResponseEntity.ok(requestService.findResolvedByDepartment(department, principal.getName()));
    }

    /**
     * Asigna una solicitud al trabajador autenticado.
     *
     * @param id identificador de la solicitud
     * @param department departamento seleccionado
     * @param principal usuario autenticado
     * @return solicitud asignada
     */
    @PostMapping("/worker-requests/{id}/assign")
    public ResponseEntity<WorkerRequestDto> assign(
            @PathVariable Long id,
            @RequestParam String department,
            Principal principal) {
        return ResponseEntity.ok(requestService.assign(id, department, principal.getName()));
    }

    /**
     * Cambia una solicitud asignada a estado en curso.
     *
     * @param id identificador de la solicitud
     * @param department departamento seleccionado
     * @param principal usuario autenticado
     * @return solicitud iniciada
     */
    @PostMapping("/worker-requests/{id}/start")
    public ResponseEntity<WorkerRequestDto> start(
            @PathVariable Long id,
            @RequestParam String department,
            Principal principal) {
        return ResponseEntity.ok(requestService.start(id, department, principal.getName()));
    }

    /**
     * Cambia una solicitud en curso a estado finalizado.
     *
     * @param id identificador de la solicitud
     * @param department departamento seleccionado
     * @param principal usuario autenticado
     * @return solicitud finalizada
     */
    @PostMapping("/worker-requests/{id}/resolve")
    public ResponseEntity<WorkerRequestDto> resolve(
            @PathVariable Long id,
            @RequestParam String department,
            Principal principal) {
        return ResponseEntity.ok(requestService.resolve(id, department, principal.getName()));
    }
}
