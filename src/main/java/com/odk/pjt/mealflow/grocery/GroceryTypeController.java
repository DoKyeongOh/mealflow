package com.odk.pjt.mealflow.grocery;

import com.odk.pjt.mealflow.grocery.dto.GroceryTypeDtos;
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
    public List<GroceryTypeDtos.Response> list() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return groceryTypeService.list(userId).stream().map(GroceryTypeDtos.Response::from).toList();
    }

    @GetMapping("/{id}")
    public GroceryTypeDtos.Response get(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        GroceryType entity = groceryTypeService.get(userId, id);
        return GroceryTypeDtos.Response.from(entity);
    }

    /** 보관 항목이 이 식료품 종류를 참조하는지 (삭제 전 확인 등). */
    @GetMapping("/{id}/referenced")
    public GroceryTypeDtos.ReferenceStatusResponse referenced(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        return new GroceryTypeDtos.ReferenceStatusResponse(groceryTypeService.isReferenced(userId, id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroceryTypeDtos.Response create(@Valid @RequestBody GroceryTypeDtos.CreateRequest body) {
        Long userId = SecurityUtils.requireCurrentUserId();
        GroceryType entity = groceryTypeService.create(userId, body);
        return GroceryTypeDtos.Response.from(entity);
    }

    @PutMapping("/{id}")
    public GroceryTypeDtos.Response update(@PathVariable Long id, @Valid @RequestBody GroceryTypeDtos.UpdateRequest body) {
        Long userId = SecurityUtils.requireCurrentUserId();
        GroceryType entity = groceryTypeService.update(userId, id, body);
        return GroceryTypeDtos.Response.from(entity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        groceryTypeService.delete(userId, id);
    }
}
