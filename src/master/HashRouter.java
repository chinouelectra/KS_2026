package master;

import common.WorkerInfo;

public class HashRouter {
    private final WorkerRegistry workerRegistry;

    public HashRouter(WorkerRegistry workerRegistry) {
        this.workerRegistry = workerRegistry;
    }

    public WorkerInfo routeByGameName(String gameName) {
        int hash = Math.abs(gameName.hashCode());
        int index = hash % workerRegistry.size();
        return workerRegistry.getByIndex(index);
    }
}