package xyz.openmodloader.launcher.strippable;

import java.lang.annotation.*;

@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Strippable {
    Side side() default Side.UNIVERSAL;

    Environment environment() default Environment.UNIVERSAL;

    String[] mods() default {};

    String[] classes() default {};

    @Retention(value = RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @Repeatable(value = InterfaceContainer.class)
    @interface Interface {
        String[] interfaces();
        
        Side side() default Side.UNIVERSAL;

        Environment environment() default Environment.UNIVERSAL;

        String[] mods() default {};

        String[] classes() default {};
    }
}