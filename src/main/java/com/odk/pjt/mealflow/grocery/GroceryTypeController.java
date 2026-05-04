package com.odk.pjt.mealflow.grocery;

import com.odk.pjt.mealflow.grocery.dto.GroceryTypeCreateRequest;
import com.odk.pjt.mealflow.grocery.dto.GroceryTypeReferenceStatusResponse;
import com.odk.pjt.mealflow.grocery.dto.GroceryTypeResponse;
import com.odk.pjt.mealflow.grocery.dto.GroceryTypeUpdateRequest;
import com.odk.pjt.mealflow.grocery.model.GroceryType;
import com.odk.pjt.mealflow.security.SecurityUtils;
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
@RequestMapping("/api/v1/grocery-types")
@RequiredArgsConstructor
public class GroceryTypeController {

    private final GroceryTypeService groceryTypeService;

    @GetMapping
    public List<GroceryTypeResponse> list() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return groceryTypeService.list(userId).stream().map(GroceryTypeResponse::from).toList();
    }

    @GetMapping("/{id}")
    public GroceryTypeResponse get(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        GroceryType entity = groceryTypeService.get(userId, id);
        return GroceryTypeResponse.from(entity);
    }

    /** 보관 항목이 이 식료품 종류를 참조하는지 (삭제 전 확인 등). */
    @GetMapping("/{id}/referenced")
    public GroceryTypeReferenceStatusResponse referenced(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return new GroceryTypeReferenceStatusResponse(groceryTypeService.isReferenced(userId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroceryTypeResponse create(@Valid @RequestBody GroceryTypeCreateRequest body) {
        Long userId = SecurityUtils.requireCurrentUserId();
        GroceryType entity = groceryTypeService.create(userId, body);
        return GroceryTypeResponse.from(entity);
    }

    @PutMapping("/{id}")
    public GroceryTypeResponse update(@PathVariable Long id, @Valid @RequestBody GroceryTypeUpdateRequest body) {
        Long userId = SecurityUtils.requireCurrentUserId();
        GroceryType entity = groceryTypeService.update(userId, id, body);
        return GroceryTypeResponse.from(entity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        groceryTypeService.delete(userId, id);
    }
}
