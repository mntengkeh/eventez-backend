package com.veridyl.eventez.controller

import com.veridyl.eventez.dto.matching.MatchRequest
import com.veridyl.eventez.dto.matching.MatchResponse
import com.veridyl.eventez.service.MatchingService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/match")
class MatchController(
    private val matchingService: MatchingService
) {

    @PostMapping
    @PreAuthorize("hasAuthority('PLANNER')")
    fun findMatches(
        @RequestBody @Valid request: MatchRequest
    ): ResponseEntity<MatchResponse> {
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(matchingService.findMatches(request))
    }
}
