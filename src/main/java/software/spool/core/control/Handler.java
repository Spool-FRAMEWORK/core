package software.spool.core.control;

import software.spool.core.exception.SpoolException;

public interface Handler<T> {
    void handle(T object) throws SpoolException;
}
