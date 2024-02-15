package com.bundee.msfw.interfaces.endpoint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.bundee.msfw.defs.UniversalConstants;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BEndpoint {
    public String uri() default "/error/";
    public String httpMethod() default UniversalConstants.GET;
    public String permission() default UniversalConstants.UNKNOWN;
    public Class<?> reqDTOClass() default Object.class;
    public Class<?> auditorClass() default Object.class;
    public Class<?> pageHandlerClass() default Object.class;
}
