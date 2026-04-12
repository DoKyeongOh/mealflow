package com.odk.pjt.mealflow.storage;

import com.odk.pjt.mealflow.security.SecurityUtils;
import com.odk.pjt.mealflow.storage.dto.StorageLocationDtos;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/storage-locations")
@RequiredArgsConstructor
public class StorageLocationController {

    private final StorageLocationService storageLocationService;

    @GetMapping
    public List<StorageLocationDtos.Response> list() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return storageLocationService.list(userId).stream().map(StorageLocationDtos.Response::from).toList();
    }

    @GetMapping("/{id}")
    public StorageLocationDtos.Response get(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        StorageLocation entity = storageLocationService.get(userId, id);
        return StorageLocationDtos.Response.from(entity);
    }

    /** 저장소가 보관 항목·식료품 기본 저장소로 참조되는지 (삭제 전 확인 등). */
    @GetMapping("/{id}/referenced")
    public StorageLocationDtos.ReferenceStatusResponse referenced(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return new StorageLocationDtos.ReferenceStatusResponse(storageLocationService.isReferenced(userId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StorageLocationDtos.Response create(@Valid @RequestBody StorageLocationDtos.CreateRequest body) {
        Long userId = SecurityUtils.requireCurrentUserId();
        StorageLocation entity = storageLocationService.create(userId, body.name());
        return StorageLocationDtos.Response.from(entity);
    }

    @PutMapping("/{id}")
    public StorageLocationDtos.Response update(
            @PathVariable Long id, @Valid @RequestBody StorageLocationDtos.UpdateRequest body) {
        Long userId = SecurityUtils.requireCurrentUserId();
        StorageLocation entity = storageLocationService.update(userId, id, body.name());
        return StorageLocationDtos.Response.from(entity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        storageLocationService.delete(userId, id);
    }
}
