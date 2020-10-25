package the_fireplace.clans.legacy.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

/**
 * An interface for adding comments to the config. Annotate fields you want commented with this.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(FIELD)
public @interface Comment {
    String value();
}
