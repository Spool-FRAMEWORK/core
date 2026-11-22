package software.spool.core.port.health;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Watches one piece of infrastructure a module uses: every real call to it, and the check of its type when the
 * plugin of that type has one.
 *
 * <p>A piece is unhealthy while its last call failed or its check fails, and degraded when its check takes longer
 * than its policy allows. The check runs when the health is asked, at most once per interval of its policy.</p>
 */
public final class PieceMonitor {
    private final String piece;
    private final String type;
    private final Optional<HealthProbe> probe;
    private final PieceCheckPolicy policy;
    private final Clock clock;
    private volatile Optional<String> lastCallFailure = Optional.empty();
    private volatile Optional<CheckResult> lastCheck = Optional.empty();

    public PieceMonitor(String piece, String type, Optional<HealthProbe> probe, PieceCheckPolicy policy, Clock clock) {
        this.piece = piece;
        this.type = type;
        this.probe = probe;
        this.policy = policy;
        this.clock = clock;
    }

    public static PieceMonitor of(String piece, String type, Optional<HealthProbe> probe, PieceCheckPolicy policy) {
        return new PieceMonitor(piece, type, probe, policy, Clock.systemUTC());
    }

    public <T, E extends Exception> T call(PieceCall<T, E> call) throws E {
        return call(call, List.of());
    }

    /**
     * Calls the piece. An exception of one of the given types is an answer of the piece, such as a duplicate, not a
     * failure of it, so it leaves the piece healthy.
     */
    public <T, E extends Exception> T call(PieceCall<T, E> call, List<Class<? extends Exception>> answers) throws E {
        try {
            T result = call.call();
            lastCallFailure = Optional.empty();
            return result;
        } catch (FailureOutsideThePiece outside) {
            throw outside.failure();
        } catch (Exception e) {
            lastCallFailure = isAnswer(e, answers) ? Optional.empty() : Optional.of(reasonOf(e));
            throw e;
        }
    }

    /**
     * Opens a stream the piece gives and watches each element read from it. What the reader of the stream does
     * with an element is not blamed on the piece.
     */
    public <T> Stream<T> stream(PieceCall<Stream<T>, RuntimeException> open) {
        Stream<T> opened = call(open);
        Spliterator<T> source = opened.spliterator();
        Spliterator<T> watched = new Spliterators.AbstractSpliterator<>(source.estimateSize(), source.characteristics()) {
            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                return call(() -> source.tryAdvance(element -> {
                    try {
                        action.accept(element);
                    } catch (RuntimeException e) {
                        throw new FailureOutsideThePiece(e);
                    }
                }));
            }
        };
        return StreamSupport.stream(watched, false).onClose(opened::close);
    }

    /**
     * A stream the piece is given, such as the results to write. A failure while producing its elements is not
     * blamed on the piece; a failure of the piece while handling an element is.
     */
    public <T> Stream<T> given(Stream<T> input) {
        Spliterator<T> source = input.spliterator();
        Spliterator<T> marked = new Spliterators.AbstractSpliterator<>(source.estimateSize(), source.characteristics()) {
            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                try {
                    return source.tryAdvance(element -> {
                        try {
                            action.accept(element);
                        } catch (RuntimeException e) {
                            throw new FailureOfThePiece(e);
                        }
                    });
                } catch (FailureOfThePiece inside) {
                    throw inside.failure();
                } catch (RuntimeException e) {
                    throw new FailureOutsideThePiece(e);
                }
            }
        };
        return StreamSupport.stream(marked, false).onClose(input::close);
    }

    public <E extends Exception> void run(PieceAction<E> action) throws E {
        call(() -> {
            action.run();
            return null;
        });
    }

    public boolean hasCheck() {
        return probe.isPresent();
    }

    public PieceHealth health() {
        Instant now = clock.instant();
        Optional<CheckResult> check = probe.map(this::checkDueAt);
        if (check.filter(result -> result.status() == HealthStatus.UNHEALTHY).isPresent()) {
            return healthOf(check.get(), now);
        }
        if (lastCallFailure.isPresent()) {
            return new PieceHealth(piece, type, HealthStatus.UNHEALTHY, lastCallFailure, now);
        }
        return check.map(result -> healthOf(result, now))
                .orElseGet(() -> new PieceHealth(piece, type, HealthStatus.HEALTHY, Optional.empty(), now));
    }

    private CheckResult checkDueAt(HealthProbe healthProbe) {
        Instant now = clock.instant();
        Optional<CheckResult> recent = lastCheck.filter(result -> result.at().plus(policy.interval()).isAfter(now));
        if (recent.isPresent()) return recent.get();
        CheckResult result = check(healthProbe, now);
        lastCheck = Optional.of(result);
        return result;
    }

    private CheckResult check(HealthProbe healthProbe, Instant startedAt) {
        try {
            HealthCheck check = healthProbe.probe();
            Duration took = Duration.between(startedAt, clock.instant());
            if (check.status() == HealthStatus.HEALTHY && took.compareTo(policy.slowAfter()) > 0) {
                return new CheckResult(HealthStatus.DEGRADED, Optional.of("its check took " + took.toMillis()
                        + " ms, more than the " + policy.slowAfter().toMillis() + " ms allowed"), startedAt);
            }
            return new CheckResult(check.status(), Optional.ofNullable(check.reason()), startedAt);
        } catch (Exception e) {
            return new CheckResult(HealthStatus.UNHEALTHY, Optional.of(reasonOf(e)), startedAt);
        }
    }

    private PieceHealth healthOf(CheckResult check, Instant now) {
        return new PieceHealth(piece, type, check.status(), check.reason(), now);
    }

    private static String reasonOf(Exception e) {
        return Optional.ofNullable(e.getMessage()).orElse(e.getClass().getSimpleName());
    }

    private static boolean isAnswer(Exception e, List<Class<? extends Exception>> answers) {
        return answers.stream().anyMatch(answer -> answer.isInstance(e));
    }

    private record CheckResult(HealthStatus status, Optional<String> reason, Instant at) {
    }

    private static final class FailureOutsideThePiece extends RuntimeException {
        private final RuntimeException failure;

        private FailureOutsideThePiece(RuntimeException failure) {
            super(failure.getMessage(), failure, false, false);
            this.failure = failure;
        }

        private RuntimeException failure() {
            return failure;
        }
    }

    private static final class FailureOfThePiece extends RuntimeException {
        private final RuntimeException failure;

        private FailureOfThePiece(RuntimeException failure) {
            super(failure.getMessage(), failure, false, false);
            this.failure = failure;
        }

        private RuntimeException failure() {
            return failure;
        }
    }

    @FunctionalInterface
    public interface PieceCall<T, E extends Exception> {
        T call() throws E;
    }

    @FunctionalInterface
    public interface PieceAction<E extends Exception> {
        void run() throws E;
    }
}
