package the_fireplace.clans.legacy.config.constraints;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Constrain config value to a range of numbers.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface RangeNumber {
    long min() default Long.MIN_VALUE;
    long max() default Long.MAX_VALUE;
}
