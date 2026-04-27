package com.veridyl.eventez.controller

import com.veridyl.eventez.dto.service.ServiceCategoryResponse
import com.veridyl.eventez.service.ServiceCategoryService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/categories")
@Tag(name = "Service Categories", description = "Browse and retrieve available service types (e.g., Catering, Photography)")
class CategoryController(
    private val categoryService: ServiceCategoryService
) {


//     returns all service categories.

    @GetMapping
    fun getAllCategories(): ResponseEntity<List<ServiceCategoryResponse>> =
        ResponseEntity.ok(categoryService.getAllCategories())

//    get service by id
    @GetMapping("/{id}")
    fun getCategoryById(@PathVariable id: Long): ResponseEntity<ServiceCategoryResponse> =
        ResponseEntity.ok(categoryService.getCategoryById(id))
}
