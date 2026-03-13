# 14. Error Handling

Create `com.veridyl.eventez.exception.GlobalExceptionHandler`:

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

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

    @ExceptionHandler(Exception::class)
    fun handleGeneral(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(500).body(
            ErrorResponse(500, "Internal Server Error", "An unexpected error occurred")
        )
    }
}
```

## Custom Exceptions

```kotlin
class ResourceNotFoundException(message: String) : RuntimeException(message)
class DuplicateResourceException(message: String) : RuntimeException(message)
class AccessDeniedException(message: String) : RuntimeException(message)
```
