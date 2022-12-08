package de.hipphampel.validation.core.condition;

import de.hipphampel.validation.core.execution.ValidationContext;
import de.hipphampel.validation.core.path.Path;
import de.hipphampel.validation.core.path.PathResolver;
import de.hipphampel.validation.core.utils.OneOfTwo;
import de.hipphampel.validation.core.value.Value;
import java.util.Objects;
import java.util.Optional;

/**
 * Condition about existence of {@link Path Paths}.
 * <p>
 * This {@link Condition} implementation checks, whether the given {@code Path} exists or not.
 *
 * @param path            The {@code Path} to evaluate
 * @param referenceObject The object the path is resolved for. If empty, the object being validated is used
 */
public record PathCondition(OneOfTwo<Value<String>, Path> path,
                            Optional<Value<?>> referenceObject) implements Condition {

  public PathCondition {
    Objects.requireNonNull(path);
    Objects.requireNonNull(referenceObject);
  }

  @Override
  public boolean evaluate(ValidationContext context, Object facts) {
    PathResolver resolver = context.getPathResolver();
    Object reference = referenceObject
        .map(ref -> (Object) ref.get(context, facts))
        .orElse(facts);
    Path pathToResolve = path.mapIfFirst(value -> valueToPath(context, facts, value));

    return resolver.exists(reference, pathToResolve);
  }

  private Path valueToPath(ValidationContext context, Object facts, Value<String> value) {
    String pathStr = value.get(context, facts);
    return context.getPathResolver().parse(pathStr);
  }

}
