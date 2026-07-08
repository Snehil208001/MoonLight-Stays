package com.snehil.moon_stays_androidapp.domain.usecase

import com.snehil.moon_stays_androidapp.core.common.NetworkResult
import com.snehil.moon_stays_androidapp.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    // Returns the backend response {"url": "/images/<filename>"}
    operator fun invoke(file: File, mimeType: String = "image/*"): Flow<NetworkResult<Map<String, String>>> {
        val part = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody(mimeType.toMediaTypeOrNull())
        )
        return adminRepository.uploadImage(part)
    }
}
