package com.dita.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ReviewWriteDTO {
    private Integer rstId;
    private String menuNames;
    private String content;
    private double rating;
    private List<MultipartFile> photos;
}
