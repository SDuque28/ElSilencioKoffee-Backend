package ElSilencioKoffee_Backend.environment.controllers;

import ElSilencioKoffee_Backend.environment.dto.EnvironmentMetricCreateRequest;
import ElSilencioKoffee_Backend.environment.dto.EnvironmentMetricResponse;
import ElSilencioKoffee_Backend.environment.dto.EnvironmentMetricUpdateRequest;
import ElSilencioKoffee_Backend.environment.services.IEnvironmentMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/environment-metrics")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class EnvironmentMetricController {

    private final IEnvironmentMetricService environmentMetricService;

    @GetMapping
    public ResponseEntity<List<EnvironmentMetricResponse>> findAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long sectionId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam(required = false) LocalDateTime fromDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam(required = false) LocalDateTime toDate
    ) {
        return ResponseEntity.ok(environmentMetricService.findAll(type, sectionId, fromDate, toDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EnvironmentMetricResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(environmentMetricService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnvironmentMetricResponse> create(@RequestBody EnvironmentMetricCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(environmentMetricService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnvironmentMetricResponse> update(
            @PathVariable Long id,
            @RequestBody EnvironmentMetricUpdateRequest request
    ) {
        return ResponseEntity.ok(environmentMetricService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        environmentMetricService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
