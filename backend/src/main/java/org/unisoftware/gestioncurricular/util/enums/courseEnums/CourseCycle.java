package org.unisoftware.gestioncurricular.util.enums.courseEnums;

import lombok.Getter;

@Getter
public enum CourseCycle {
    acceso("A", "acceso"),
    especifico("E", "especifico"),
    fundamentacion("F", "fundamentacion"),
    no_aplica("NA", "no_aplica");

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

    CourseCycle(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /** Convierte abreviatura a enum */
    public static CourseCycle fromCode(String code) {
        for (CourseCycle courseCycle : values()) {
            if (courseCycle.getCode().equalsIgnoreCase(code)) {
                return courseCycle;
            }
        }
        throw new IllegalArgumentException("Invalid Cycle: " + code);
    }

    @Override
    public String toString() {
        return label;
    }
}
