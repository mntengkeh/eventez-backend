package com.veridyl.eventez.service

import com.cloudinary.Cloudinary
import com.cloudinary.Transformation
import com.veridyl.eventez.exception.CloudinaryUploadException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException

@Service
class CloudinaryService(
    private val cloudinary: Cloudinary
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${cloudinary.folder}")
    private lateinit var baseFolder: String

    fun uploadFile(file: MultipartFile): Map<String, Any> {
        return try {
            val resourceType = resolveResourceType(file)
            val folderPath   = "$baseFolder/$resourceType"

            val uploadParams = mutableMapOf<String, Any>(
                "resource_type"   to resourceType,
                "folder"          to folderPath,
                "use_filename"    to true,
                "unique_filename" to true,
                "overwrite"       to false
            )

            if (resourceType == "video") {
                val videoTransformation = Transformation<Transformation<*>>()
                    .height(720)
                    .crop("scale")
                    .chain()
                    .videoCodec("h264")
                    .audioCodec("aac")

                uploadParams["eager"] = listOf(videoTransformation)
                uploadParams["eager_async"] = true
            }


            log.info(
                "Uploading {} to Cloudinary: folder={}, filename={}, size={}",
                resourceType, folderPath, file.originalFilename, file.size
            )

            @Suppress("UNCHECKED_CAST")
            val result = cloudinary.uploader().upload(file.bytes, uploadParams) as Map<String, Any>

            log.info("Cloudinary upload successful: public_id={}", result["public_id"])
            result

        } catch (e: IOException) {
            throw CloudinaryUploadException(
                "Failed to upload file to Cloudinary: ${e.message}"
            )
        }
    }

    fun deleteFile(publicId: String, resourceType: String) {
        try {
            @Suppress("UNCHECKED_CAST")
            val result = cloudinary.uploader().destroy(
                publicId,
                mapOf("resource_type" to resourceType)
            ) as Map<String, Any>

            when (val status = result["result"] as? String) {
                "ok"        -> log.info("Cloudinary delete successful: public_id={}", publicId)
                "not found" -> log.warn(
                    "Cloudinary delete: asset not found (already deleted?): public_id={}", publicId
                )
                else        -> throw CloudinaryUploadException(
                    "Cloudinary delete returned unexpected status '$status' for public_id=$publicId"
                )
            }
        } catch (e: IOException) {
            log.error("Failed to delete file from Cloudinary: public_id={}", publicId, e)
            throw CloudinaryUploadException(
                "Failed to delete file from Cloudinary: ${e.message}"
            )
        }
    }

    private fun resolveResourceType(file: MultipartFile): String =
        when {
            file.contentType?.startsWith("video/") == true -> "video"
            file.contentType?.startsWith("image/") == true -> "image"
            else -> "auto"
        }
}