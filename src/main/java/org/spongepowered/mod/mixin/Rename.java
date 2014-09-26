package org.spongepowered.mod.mixin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to rename a method.
 * Usefully for methods that have the same name.
 */

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Rename {
   public String value();
}
