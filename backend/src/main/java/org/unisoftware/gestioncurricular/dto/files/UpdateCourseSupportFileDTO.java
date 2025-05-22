package org.unisoftware.gestioncurricular.dto.files;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.unisoftware.gestioncurricular.util.enums.AcademicSupportType;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UpdateCourseSupportFileDTO {
    public AcademicSupportType tipo;
}
