package com.jjt.platform.api.common.security;

public final class SecurityContextHolder {
    private static final ThreadLocal<SecurityContext> HOLDER = new ThreadLocal<>();

    private SecurityContextHolder() {}

    public static void setContext(SecurityContext context) {
        HOLDER.set(context);
    }

    public static SecurityContext getContext() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
