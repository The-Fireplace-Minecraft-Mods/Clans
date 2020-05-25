package the_fireplace.clans.config.constraints;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * Constrain config value to a range of decimal numbers.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface RangeDecimal {
    double min() default Double.MIN_VALUE;
    double max() default Double.MAX_VALUE;
}
