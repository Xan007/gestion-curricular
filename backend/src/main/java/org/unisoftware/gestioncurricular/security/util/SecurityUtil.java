package org.unisoftware.gestioncurricular.security.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class SecurityUtil {

    public static String getJwtFromSecurityContext() {
        var requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes attrs) {
            return (String) attrs.getRequest().getAttribute("jwt");
        }
        return null;
    }
}
