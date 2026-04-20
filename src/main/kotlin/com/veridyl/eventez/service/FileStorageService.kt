package com.veridyl.eventez.services

import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

// Custom exception class
class FileStorageException(
    message: String,
    val status: HttpStatus,
    cause: Throwable? = null
) : RuntimeException(message, cause)

@Service
class FileStorageService {

    // Storage locations for EventEz
    private val profilePicturesLocation: Path
    private val portfolioImagesLocation: Path
    private val portfolioVideosLocation: Path

    companion object {
        // size limits in bytes)
        private const val MAX_VIDEO_SIZE   = 50 * 1024 * 1024L
        private const val MAX_IMAGE_SIZE   = 10  * 1024 * 1024L
        private const val MAX_PROFILE_SIZE = 5   * 1024 * 1024L

        // allowed MIME types
        private val ALLOWED_VIDEO_TYPES       = arrayOf("video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo")
        private val ALLOWED_IMAGE_TYPES       = arrayOf("image/jpeg", "image/png", "image/gif", "image/webp")
        private val ALLOWED_PROFILE_PIC_TYPES = arrayOf("image/jpeg", "image/png", "image/gif")
    }

    init {
        profilePicturesLocation  = Paths.get("uploads/users/profilePictures").toAbsolutePath().normalize()
        portfolioImagesLocation  = Paths.get("uploads/portfolio/images").toAbsolutePath().normalize()
        portfolioVideosLocation  = Paths.get("uploads/portfolio/videos").toAbsolutePath().normalize()

        try {
            Files.createDirectories(profilePicturesLocation)
            Files.createDirectories(portfolioImagesLocation)
            Files.createDirectories(portfolioVideosLocation)
        } catch (ex: Exception) {
            throw FileStorageException("Could not create upload directories.", HttpStatus.INTERNAL_SERVER_ERROR, ex)
        }
    }

    // ---------- Generic validation ----------
    fun validateFile(file: MultipartFile, maxSize: Long, allowedTypes: Array<String>?) {
        if (file.isEmpty) {
            throw FileStorageException("File is empty", HttpStatus.BAD_REQUEST)
        }
        if (file.size > maxSize) {
            throw FileStorageException(
                "File exceeds maximum allowed size (${maxSize / (1024 * 1024)} MB)",
                HttpStatus.BAD_REQUEST
            )
        }
        if (!allowedTypes.isNullOrEmpty()) {
            val contentType = file.contentType
            val isValid = allowedTypes.any { type -> contentType != null && contentType.equals(type, ignoreCase = true) }
            if (!isValid) {
                throw FileStorageException(
                    "Invalid file type. Allowed: ${allowedTypes.joinToString(", ")}",
                    HttpStatus.BAD_REQUEST
                )
            }
        }
    }

    // ---------- Profile picture ----------
    fun storeProfilePicture(file: MultipartFile, filename: String): String {
        validateFile(file, MAX_PROFILE_SIZE, ALLOWED_PROFILE_PIC_TYPES)
        return storeFile(file, profilePicturesLocation, filename)
    }

    fun loadProfilePicture(filename: String): Resource = loadFile(profilePicturesLocation, filename)

    // ---------- Portfolio image ----------
    fun storePortfolioImage(file: MultipartFile, filename: String): String {
        validateFile(file, MAX_IMAGE_SIZE, ALLOWED_IMAGE_TYPES)
        return storeFile(file, portfolioImagesLocation, filename)
    }

    fun loadPortfolioImage(filename: String): Resource = loadFile(portfolioImagesLocation, filename)

    // ---------- Portfolio video ----------
    fun storePortfolioVideo(file: MultipartFile, filename: String): String {
        validateFile(file, MAX_VIDEO_SIZE, ALLOWED_VIDEO_TYPES)
        return storeFile(file, portfolioVideosLocation, filename)
    }

    fun loadPortfolioVideo(filename: String): Resource = loadFile(portfolioVideosLocation, filename)

    // ---------- Content type detection -----
    fun determineContentType(filename: String?): String {
        if (filename == null) return "application/octet-stream"
        return when {
            filename.endsWith(".jpg",  ignoreCase = true) ||
                    filename.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            filename.endsWith(".png",  ignoreCase = true) -> "image/png"
            filename.endsWith(".gif",  ignoreCase = true) -> "image/gif"
            filename.endsWith(".webp", ignoreCase = true) -> "image/webp"
            filename.endsWith(".mp4",  ignoreCase = true) -> "video/mp4"
            filename.endsWith(".mpeg", ignoreCase = true) ||
                    filename.endsWith(".mpg",  ignoreCase = true) -> "video/mpeg"
            filename.endsWith(".mov",  ignoreCase = true) -> "video/quicktime"
            filename.endsWith(".avi",  ignoreCase = true) -> "video/x-msvideo"
            else -> "application/octet-stream"
        }
    }

    // ---------- generic store / load / delete ----------
    private fun storeFile(file: MultipartFile, location: Path, filename: String): String {
        if (filename.contains("..")) {
            throw FileStorageException("Invalid filename: $filename", HttpStatus.BAD_REQUEST)
        }
        return try {
            val targetLocation = location.resolve(filename)
            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
            targetLocation.toString()
        } catch (e: IOException) {
            throw FileStorageException("Could not store file: $filename", HttpStatus.INTERNAL_SERVER_ERROR, e)
        }
    }

    private fun loadFile(location: Path, filename: String): Resource {
        return try {
            val filePath = location.resolve(filename).normalize()
            val resource = UrlResource(filePath.toUri())
            if (resource.exists() && resource.isReadable) {
                resource
            } else {
                throw FileStorageException("File not found: $filename", HttpStatus.NOT_FOUND)
            }
        } catch (e: MalformedURLException) {
            throw FileStorageException("Invalid file path: $filename", HttpStatus.NOT_FOUND, e)
        }
    }

    fun deleteFile(filePath: String) {
        try {
            val path = Paths.get(filePath)
            Files.deleteIfExists(path)
        } catch (e: IOException) {
            throw FileStorageException("Failed to delete file: $filePath", HttpStatus.INTERNAL_SERVER_ERROR, e)
        }
    }

    fun extractFilenameFromPath(path: String): String = Paths.get(path).fileName.toString()
}
