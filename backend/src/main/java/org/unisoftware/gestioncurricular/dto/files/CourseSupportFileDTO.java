package org.unisoftware.gestioncurricular.dto.files;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.unisoftware.gestioncurricular.util.enums.AcademicSupportType;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CourseSupportFileDTO {
    public Long id;
    public String url;
    public LocalDateTime uploadedAt;
    public AcademicSupportType tipo;
}
