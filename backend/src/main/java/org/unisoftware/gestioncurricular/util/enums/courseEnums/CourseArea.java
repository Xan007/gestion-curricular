package org.unisoftware.gestioncurricular.util.enums.courseEnums;

import lombok.Getter;

@Getter
public enum CourseArea {
    profesional("PS", "profesional"),
    basica("BA", "basica"),
    complementaria("CO", "complementaria"),
    profundizacion("PZ", "profundizacion"),
    investigacion("I", "investigacion");

    /**
     * -- GETTER --
     * Abreviatura para guardar en la DB
     */
    private final String code;
    /**
     * -- GETTER --
     * Texto legible para mostrar al usuario
     */
    private final String label;

    CourseArea(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /** Convierte abreviatura a enum */
    public static CourseArea fromCode(String code) {
        for (CourseArea courseArea : values()) {
            if (courseArea.getCode().equalsIgnoreCase(code)) {
                return courseArea;
            }
        }
        throw new IllegalArgumentException("Invalid Area: " + code);
    }

    @Override
    public String toString() {
        return label;
    }
}
