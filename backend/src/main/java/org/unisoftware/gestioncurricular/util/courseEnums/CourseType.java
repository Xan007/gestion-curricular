package org.unisoftware.gestioncurricular.util.courseEnums;

public enum CourseType {
    teorico("T", "teorico"), practico("P", "practica"), teorico_practico("TP", "teorico_practico");

    private final String code;
    private final String label;

    CourseType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /** Abreviatura para guardar en la DB */
    public String getCode() {
        return code;
    }

    /** Texto legible para mostrar al usuario */
    public String getLabel() {
        return label;
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
