package software.spool.core.pipeline;

import javax.management.AttributeNotFoundException;

@FunctionalInterface
public interface Step<I, O> {
    O apply(I input) throws AttributeNotFoundException;
}