package software.spool.circuitbreaker;

import software.spool.core.circuitbreaker.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class CircuitBreakerMain {

    public static void main(String[] args) throws Exception {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println(" Spool — Circuit Breaker Demo");
        System.out.println("═══════════════════════════════════════════════════\n");

        demoTimeBased();
        System.out.println();
        demoCountBased();
    }

    // ── TIME_BASED ────────────────────────────────────────────────────────────

    static void demoTimeBased() throws Exception {
        section("TIME_BASED — threshold 50%, min 4 calls, window 5s, cooldown 3s");

        CircuitBreakerPolicy policy = CircuitBreakerPolicy.timeBased(
                0.5f,
                4,
                Duration.ofSeconds(5),
                Duration.ofSeconds(3),
                2
        );

        CircuitBreaker cb = buildCB("cb-time", policy);

        // Llamadas normales — no debe tripear
        call(cb, true,  "Éxito 1");
        call(cb, true,  "Éxito 2");
        call(cb, false, "Fallo 1");
        call(cb, true,  "Éxito 3");  // 1F/4T = 25% → CLOSED

        printStatus(cb, "Tras 4 llamadas (25% fallos)");

        // Acumulamos fallos hasta alcanzar threshold
        call(cb, false, "Fallo 2");
        call(cb, false, "Fallo 3");  // 3F/6T = 50% → OPEN

        printStatus(cb, "Tras alcanzar threshold (50%)");

        // CB abierto — llamadas rechazadas
        call(cb, true, "Intento mientras OPEN");
        call(cb, true, "Intento mientras OPEN");

        printStatus(cb, "Tras intentos rechazados");

        // Esperamos el cooldown
        System.out.println("  ⏱  Esperando cooldown (3s)...");
        Thread.sleep(3100);

        // Primera llamada tras cooldown — transiciona a HALF_OPEN
        call(cb, true, "Probe 1 (HALF_OPEN)");
        printStatus(cb, "Tras probe 1");

        call(cb, true, "Probe 2 (HALF_OPEN)"); // 2 éxitos = halfOpenPermits → CLOSED
        printStatus(cb, "Tras probe 2 — debería ser CLOSED");

        // Ventana expirada — contadores se resetean
        System.out.println("  ⏱  Esperando expiración de ventana (5s)...");
        Thread.sleep(5100);

        call(cb, false, "Fallo post-ventana");
        printStatus(cb, "Tras fallo en ventana nueva (contadores reseteados)");
    }

    // ── COUNT_BASED ───────────────────────────────────────────────────────────

    static void demoCountBased() throws Exception {
        section("COUNT_BASED — threshold 50%, min 4 calls, window 6 calls, cooldown 2s");

        CircuitBreakerPolicy policy = CircuitBreakerPolicy.countBased(
                0.5f,
                4,
                6,
                Duration.ofSeconds(2),
                2
        );

        CircuitBreaker cb = buildCB("cb-count", policy);

        // Llenamos la ventana con 3F + 3S = 50% — justo en el threshold
        call(cb, false, "Fallo 1");
        call(cb, false, "Fallo 2");
        call(cb, false, "Fallo 3");
        call(cb, true,  "Éxito 1");
        call(cb, true,  "Éxito 2");
        call(cb, true,  "Éxito 3"); // 3F/6T = 50% → OPEN

        printStatus(cb, "Ventana llena con 50% fallos");

        // CB abierto
        call(cb, true, "Intento mientras OPEN");

        // Cooldown
        System.out.println("  ⏱  Esperando cooldown (2s)...");
        Thread.sleep(2100);

        // Probes
        call(cb, true, "Probe 1");
        call(cb, true, "Probe 2"); // → CLOSED

        printStatus(cb, "Tras probes exitosas");

        // Demostramos el sliding: los fallos van saliendo de la ventana
        section("  Sliding window — fallos antiguos salen al añadir éxitos");
        call(cb, false, "Fallo A");
        call(cb, false, "Fallo B");
        call(cb, false, "Fallo C");
        call(cb, true,  "Éxito A");
        printStatus(cb, "3F+1S en ventana (aún bajo mínimo de trips)");

        // Añadimos éxitos — los fallos antiguos van deslizando fuera
        call(cb, true, "Éxito B — Fallo A sale de ventana");
        call(cb, true, "Éxito C — Fallo B sale de ventana");
        call(cb, true, "Éxito D — Fallo C sale de ventana");

        printStatus(cb, "Todos los fallos han salido de la ventana sliding");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    static void call(CircuitBreaker cb, boolean success, String label) {
        try {
            cb.execute(() -> {
                if (!success) throw new RuntimeException("error simulado");
                return null;
            });
            System.out.printf("  [✓] %-45s → %s%n", label, cb.status());
        } catch (CircuitBreakerOpenException e) {
            System.out.printf("  [✕] %-45s → %s  ⚡ RECHAZADA%n", label, cb.status());
        } catch (Exception e) {
            System.out.printf("  [✕] %-45s → %s%n", label, cb.status());
        }
    }

    static void printStatus(CircuitBreaker cb, String context) {
        CircuitBreakerState state = ((InMemoryStore) cb.store()).ref.get();
        CircuitBreakerSnapshot snap = state.snapshot();
        System.out.printf("%n  ┌─ %s%n", context);
        System.out.printf("  │  status=%s  failures=%d  successes=%d  halfOpenAttempts=%d  version=%d%n",
                state.status(), snap.failures(), snap.successes(), snap.halfOpenAttempts(), snap.version());
        System.out.printf("  └─────────────────────────────────────────────%n%n");
    }

    static void section(String title) {
        System.out.println("───────────────────────────────────────────────────");
        System.out.println(" " + title);
        System.out.println("───────────────────────────────────────────────────");
    }

    static CircuitBreaker buildCB(String id, CircuitBreakerPolicy policy) {
        InMemoryStore store = new InMemoryStore(id);
        return new CircuitBreaker(id, policy, new TransitionEvaluator(), store);
    }

    // ── Store in-memory ───────────────────────────────────────────────────────

    static class InMemoryStore implements CircuitBreakerStateStore {
        final AtomicReference<CircuitBreakerState> ref;

        InMemoryStore(String id) {
            CircuitBreakerSnapshot snap = new CircuitBreakerSnapshot(
                    id, 0, 0, 0, Instant.now(), new ArrayDeque<>(), 0L
            );
            this.ref = new AtomicReference<>(
                    new CircuitBreakerState(snap, CircuitBreakerStatus.CLOSED, Optional.empty())
            );
        }

        @Override
        public CircuitBreakerState load(String id) { return ref.get(); }

        @Override
        public boolean compareAndSet(long expectedVersion, CircuitBreakerState newState) {
            CircuitBreakerState current = ref.get();
            if (current.snapshot().version() != expectedVersion) return false;
            return ref.compareAndSet(current, newState);
        }
    }
}