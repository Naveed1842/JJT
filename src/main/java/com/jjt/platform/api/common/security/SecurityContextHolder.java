package com.jjt.platform.api.common.security;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class SecurityContextHolder {
    private static final ThreadLocal<SecurityContext> HOLDER = new ThreadLocal<>();

    public void setContext(SecurityContext context) {
        HOLDER.set(context);
    }

    public SecurityContext getContext() {
        return HOLDER.get();
    }

    public void clear() {
        HOLDER.remove();
    }
}
