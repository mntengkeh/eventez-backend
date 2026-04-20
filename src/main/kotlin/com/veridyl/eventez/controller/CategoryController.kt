package com.veridyl.eventez.controller

import com.veridyl.eventez.dto.service.ServiceCategoryResponse
import com.veridyl.eventez.service.ServiceCategoryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/categories")
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