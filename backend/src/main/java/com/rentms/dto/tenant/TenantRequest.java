package com.rentms.dto.tenant;

import com.rentms.entity.Tenant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotBlank(message = "Mobile number is required")
    @Size(min = 10, max = 15, message = "Mobile number must be between 10 and 15 characters")
    @Pattern(regexp = "^[0-9]+$", message = "Mobile number must contain only digits")
    private String mobileNumber;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;
}