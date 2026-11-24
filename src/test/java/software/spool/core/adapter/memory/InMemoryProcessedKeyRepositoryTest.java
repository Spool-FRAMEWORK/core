package software.spool.core.adapter.memory;

import software.spool.core.port.idempotency.ProcessedKeyRepository;
import software.spool.core.port.idempotency.ProcessedKeyRepositoryContract;

class InMemoryProcessedKeyRepositoryTest extends ProcessedKeyRepositoryContract {

    @Override
    protected ProcessedKeyRepository repository() {
        return new InMemoryProcessedKeyRepository();
    }
}
