package com.veridyl.eventez.service

import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

class FileStorageException(
    message: String,
    val status: HttpStatus,
    cause: Throwable? = null
) : RuntimeException(message, cause)

@Service
class FileStorageService {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val MAX_VIDEO_SIZE   = 50 * 1024 * 1024L
        private const val MAX_IMAGE_SIZE   = 10 * 1024 * 1024L
        private const val MAX_PROFILE_SIZE =  5 * 1024 * 1024L

        private val ALLOWED_VIDEO_TYPES       = arrayOf("video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo")
        private val ALLOWED_IMAGE_TYPES       = arrayOf("image/jpeg", "image/png", "image/gif", "image/webp")
        private val ALLOWED_PROFILE_PIC_TYPES = arrayOf("image/jpeg", "image/png", "image/gif")

        private const val PROFILE_PICTURES_DIR = "uploads/users/profilePictures"
        private const val PORTFOLIO_IMAGES_DIR = "uploads/portfolio/images"
        private const val PORTFOLIO_VIDEOS_DIR = "uploads/portfolio/videos"
    }

    // Validation

    fun validate(file: MultipartFile) {
        val contentType = file.contentType
            ?: throw FileStorageException("File content type is missing", HttpStatus.BAD_REQUEST)

        when {
            contentType.startsWith("image/") ->
                validateFile(file, MAX_IMAGE_SIZE, ALLOWED_IMAGE_TYPES)
            contentType.startsWith("video/") ->
                validateFile(file, MAX_VIDEO_SIZE, ALLOWED_VIDEO_TYPES)
            else -> throw FileStorageException(
                "Unsupported file type '$contentType'. " +
                        "Accepted image types: ${ALLOWED_IMAGE_TYPES.joinToString()}. " +
                        "Accepted video types: ${ALLOWED_VIDEO_TYPES.joinToString()}.",
                HttpStatus.UNSUPPORTED_MEDIA_TYPE
            )
        }
    }

    fun validateFile(file: MultipartFile, maxSize: Long, allowedTypes: Array<String>?) {
        if (file.isEmpty) {
            throw FileStorageException("File must not be empty", HttpStatus.BAD_REQUEST)
        }
        if (file.size > maxSize) {
            throw FileStorageException(
                "File exceeds the maximum allowed size of ${maxSize / (1024 * 1024)} MB.",
                HttpStatus.BAD_REQUEST
            )
        }
        if (!allowedTypes.isNullOrEmpty()) {
            val contentType = file.contentType
            val isValid = allowedTypes.any { it.equals(contentType, ignoreCase = true) }
            if (!isValid) {
                throw FileStorageException(
                    "Invalid file type '$contentType'. Allowed: ${allowedTypes.joinToString(", ")}.",
                    HttpStatus.UNSUPPORTED_MEDIA_TYPE
                )
            }
        }
    }

    // Profile picture

    fun storeProfilePicture(file: MultipartFile, filename: String): String {
        validateFile(file, MAX_PROFILE_SIZE, ALLOWED_PROFILE_PIC_TYPES)
        return store(file, PROFILE_PICTURES_DIR, filename)
    }

    fun loadProfilePicture(filename: String): Resource =
        retrieve(filename, PROFILE_PICTURES_DIR)


    // Portfolio image

    fun storePortfolioImage(file: MultipartFile): String {
        validateFile(file, MAX_IMAGE_SIZE, ALLOWED_IMAGE_TYPES)
        return store(file, PORTFOLIO_IMAGES_DIR)
    }

    fun loadPortfolioImage(filename: String): Resource =
        retrieve(filename, PORTFOLIO_IMAGES_DIR)

    // Portfolio video

    fun storePortfolioVideo(file: MultipartFile): String {
        validateFile(file, MAX_VIDEO_SIZE, ALLOWED_VIDEO_TYPES)
        return store(file, PORTFOLIO_VIDEOS_DIR)
    }

    fun loadPortfolioVideo(filename: String): Resource =
        retrieve(filename, PORTFOLIO_VIDEOS_DIR)

    // Base

    fun store(file: MultipartFile, directory: String, filename: String = generateFilename(file)): String {
        return try {
            val targetDir = Paths.get(directory).toAbsolutePath().normalize()
            Files.createDirectories(targetDir)

            val targetPath = targetDir.resolve(filename)
            Files.copy(file.inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING)

            log.info("File stored: path={}, size={}", targetPath, file.size)
            "$directory/$filename"

        } catch (ex: IOException) {
            throw FileStorageException(
                "Failed to store file '${file.originalFilename}' in '$directory': ${ex.message}",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex
            )
        }
    }

    fun retrieve(filename: String, directory: String): Resource {
        return try {
            val targetDir  = Paths.get(directory).toAbsolutePath().normalize()
            val targetPath = targetDir.resolve(filename).normalize()

            if (!targetPath.startsWith(targetDir)) {
                throw FileStorageException(
                    "Filename '$filename' resolves outside the target directory — possible path traversal attempt.",
                    HttpStatus.BAD_REQUEST
                )
            }

            val resource = UrlResource(targetPath.toUri())
            if (!resource.exists() || !resource.isReadable) {
                throw FileStorageException("File not found or unreadable: $filename", HttpStatus.NOT_FOUND)
            }

            resource

        } catch (ex: MalformedURLException) {
            throw FileStorageException(
                "Could not resolve path for file '$filename': ${ex.message}",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex
            )
        }
    }

    fun deleteFile(filePath: String) {
        try {
            Files.deleteIfExists(Paths.get(filePath))
            log.info("File deleted: path={}", filePath)
        } catch (ex: IOException) {
            throw FileStorageException(
                "Failed to delete file '$filePath': ${ex.message}",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex
            )
        }
    }

    fun extractFilenameFromPath(path: String): String =
        Paths.get(path).fileName.toString()

    // Helper

    private fun generateFilename(file: MultipartFile): String {
        val original = StringUtils.cleanPath(file.originalFilename ?: "file")
        return "${UUID.randomUUID()}_$original"
    }
}
