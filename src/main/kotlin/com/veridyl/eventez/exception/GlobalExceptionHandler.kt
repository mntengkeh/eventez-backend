package com.veridyl.eventez.exception

import com.veridyl.eventez.dto.common.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(404).body(
            ErrorResponse(404, "Not Found", ex.message ?: "Resource not found")
        )
    }

    @ExceptionHandler(DuplicateResourceException::class)
    fun handleConflict(ex: DuplicateResourceException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(409).body(
            ErrorResponse(409, "Conflict", ex.message ?: "Resource already exists")
        )
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAuthorizationDenied(ex: AuthorizationDeniedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(403)
            .body(
                ErrorResponse(403, "Forbidden", ex.message ?: "Access denied")
            )
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleForbidden(ex: AccessDeniedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(403).body(
            ErrorResponse(403, "Forbidden", ex.message ?: "Access denied")
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(400).body(
            ErrorResponse(400, "Bad Request", message)
        )
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(401, "Bad Credentials","Invalid email or password"))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(400)
            .body(ErrorResponse(400, "Bad Request", ex.message?:"Bad request")
        )
    }

    @ExceptionHandler(CloudinaryUploadException::class)
    fun handleCloudinaryUpload(ex: CloudinaryUploadException?): ResponseEntity<ErrorResponse> {
        log.error("Cloudinary upload failed", ex)
        return ResponseEntity.status(500).body(
            ErrorResponse(503, "Cloudinary upload Error", "An error occurred during upload")
        )
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    fun handleMaxUploadSize(ex: MaxUploadSizeExceededException?): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(500).body(
            ErrorResponse(413, "Max upload size exception", "File size exceeds the maximum allowed limit of 500MB.")
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(500).body(
            ErrorResponse(500, "Internal Server Error", "An unexpected error occurred")
        )
    }

}
