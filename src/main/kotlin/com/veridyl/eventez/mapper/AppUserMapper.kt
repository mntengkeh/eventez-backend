package com.veridyl.eventez.mapper

import com.veridyl.eventez.dto.auth.UserResponse
import com.veridyl.eventez.entity.AppUser
import org.mapstruct.Mapper

@Mapper
interface AppUserMapper {
    fun toUserResponse(user: AppUser) : UserResponse
}
