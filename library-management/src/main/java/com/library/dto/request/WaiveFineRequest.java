package com.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 免除罚款请求DTO
 */
@Data
public class WaiveFineRequest {

    /**
     * 免除原因
     */
    @NotBlank(message = "免除原因不能为空")
    @Size(max = 500, message = "免除原因不能超过500字")
    private String reason;
}
