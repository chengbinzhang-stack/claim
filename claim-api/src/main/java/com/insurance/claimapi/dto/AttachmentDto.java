package com.insurance.claimapi.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentDto {
    private Long id;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
}
