package org.unisoftware.gestioncurricular.util.courseParser;

import org.unisoftware.gestioncurricular.dto.CourseDTO;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;

public interface CourseFileParser {
    /**
     * Parse input stream into course DTOs
     */
    List<CourseDTO> parse(InputStream is) throws IOException;

    /**
     * Indicates whether this parser supports the given filename (by extension)
     */
    boolean supports(String filename);
}