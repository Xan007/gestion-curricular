package org.unisoftware.gestioncurricular.util.enums.courseEnums;

import lombok.Getter;

@Getter
public enum CourseType {
    teorico("T", "teorico"), practico("P", "practica"), teorico_practico("TP", "teorico_practico");

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

    CourseType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /** Convierte abreviatura a enum */
    public static CourseType fromCode(String code) {
        for (CourseType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid Course Type: " + code);
    }

    @Override
    public String toString() {
        return label;
    }
}
