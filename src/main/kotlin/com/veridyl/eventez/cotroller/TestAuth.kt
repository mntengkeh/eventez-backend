package com.veridyl.eventez.cotroller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class TestAuth {

    @GetMapping("/pro")
    @PreAuthorize("hasAuthority('PROVIDER')")
    fun testProvider(): ResponseEntity<String> {
        return ResponseEntity.ok().body("provider works")
    }

    @PreAuthorize("hasAuthority('PLANNER')")
    @GetMapping("/pla")
    fun testPlanner(): ResponseEntity<String> {
        return ResponseEntity.ok().body("planner works")
    }
}