package com.veridyl.eventez.service

import com.veridyl.eventez.dto.portfolio.CreatePortfolioItemRequest
import com.veridyl.eventez.dto.portfolio.PortfolioItemResponse
import com.veridyl.eventez.entity.PortfolioItem
import com.veridyl.eventez.entity.enums.MediaType
import com.veridyl.eventez.exception.AccessDeniedException
import com.veridyl.eventez.exception.ResourceNotFoundException
import com.veridyl.eventez.mapper.PortfolioMapper
import com.veridyl.eventez.repository.PortfolioItemRepository
import com.veridyl.eventez.repository.ProviderProfileRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class PortfolioService(
    private val portfolioRepository: PortfolioItemRepository,
    private val providerRepository: ProviderProfileRepository,
    private val cloudinaryService: CloudinaryService,
    private val fileStorageService: FileStorageService,
    private val portfolioMapper: PortfolioMapper,
    private val authService: AuthService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun addItem(
        providerId: Long,
        file: MultipartFile,
        request: CreatePortfolioItemRequest
    ): PortfolioItemResponse {
        val provider = providerRepository.findByIdOrNull(providerId)
            ?: throw ResourceNotFoundException("Provider profile not found: id=$providerId")

        verifyOwnership(provider.user.email)

        fileStorageService.validate(file)

        val isVideo = request.mediaType == MediaType.VIDEO
        val resourceType = if (isVideo) "video" else "image"

        // store locally
//        val localPath = if (isVideo) {
//            fileStorageService.storePortfolioVideo(file)
//        } else {
//            fileStorageService.storePortfolioImage(file)
//        }

        // upload to Cloudinary
        val cloudResult: Map<String, Any>
        try {
            cloudResult = cloudinaryService.uploadFile(file)
        } catch (ex: Exception) {
            // roll back local file on upload failure
            log.error(
                "Cloudinary upload failed for providerId={}. ", providerId
//                        "Rolling back local file: path={}", providerId, localPath, ex
            )
//            runCatching { fileStorageService.deleteFile(localPath) }
//                .onFailure { cleanupEx ->
//                    log.error(
//                        "Local file cleanup also failed. " +
//                                "Orphaned local file requires manual cleanup: path={}",
//                        localPath, cleanupEx
//                    )
//                }
            throw ex
        }

        val publicId  = cloudResult["public_id"] as String
        val secureUrl = cloudResult["secure_url"] as String

        return try {
            val item = PortfolioItem(
                provider = provider,
                mediaUrl = secureUrl,
                cloudinaryPublicId = publicId,
//                localMediaPath = localPath,
                mediaType = request.mediaType,
                caption = request.caption,
                displayOrder = request.displayOrder
            )
            val saved = portfolioRepository.save(item)
            log.info(
                "Portfolio item persisted: id={}, publicId={}, type={}",
                saved.id, publicId,  resourceType
            )
            portfolioMapper.toResponse(saved)

        } catch (ex: Exception) {
            log.error(
                "DB persistence failed after upload. " +
                        "Issuing compensating deletes: publicId={}",
                publicId, ex
            )
            runCatching { cloudinaryService.deleteFile(publicId, resourceType) }
                .onFailure { cleanupEx ->
                    log.error(
                        "Compensating Cloudinary delete failed. " +
                                "Orphaned asset requires manual cleanup: publicId={}",
                        publicId, cleanupEx
                    )
                }
//            runCatching { fileStorageService.deleteFile(localPath) }
//                .onFailure { cleanupEx ->
//                    log.error(
//                        "Compensating local file delete failed. " +
//                                "Orphaned file requires manual cleanup: path={}",
//                        localPath, cleanupEx
//                    )
//                }
            throw RuntimeException("Failed to persist portfolio item after upload", ex)
        }
    }

    @Transactional(readOnly = true)
    fun getItems(providerId: Long): List<PortfolioItemResponse> {
        if (!providerRepository.existsById(providerId)) {
            throw ResourceNotFoundException("Provider profile not found: id=$providerId")
        }
        return portfolioMapper.toResponseList(
            portfolioRepository.findByProviderIdOrderByDisplayOrderAsc(providerId)
        )
    }

    @Transactional
    fun deleteItem(itemId: Long) {
        val item = portfolioRepository.findByIdOrNull(itemId)
            ?: throw ResourceNotFoundException("Portfolio item not found: id=$itemId")

        verifyOwnership(item.provider.user.email)

        val resourceType = if (item.mediaType == MediaType.VIDEO) "video" else "image"

        // Delete from Cloudinary first
        cloudinaryService.deleteFile(item.cloudinaryPublicId, resourceType)

        // Delete local file
//        runCatching { fileStorageService.deleteFile(item.localMediaPath) } // NOTE: item.localMediaPath — see above
//            .onFailure { ex ->
//                log.error(
//                    "Local file delete failed during portfolio item removal. " +
//                            "Orphaned file requires manual cleanup: path={}",
//                    item.localMediaPath, ex // NOTE: item.localMediaPath — see above
//                )
//            }

        portfolioRepository.delete(item)
        log.info(
            "Portfolio item deleted: id={}, publicId={}",
            itemId, item.cloudinaryPublicId
        )
    }

    // Helper

    private fun verifyOwnership(ownerEmail: String) {
        val currentEmail = authService.getAuthenticatedUser().email
        if (currentEmail != ownerEmail) {
            throw AccessDeniedException("You do not have permission to modify this portfolio")
        }
    }
}
