package servidor;


import compartilhado.RemoteListInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class RemoteListService extends UnicastRemoteObject implements RemoteListInterface {
    private final Map<String, List<Integer>> listas = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Log log = new Log();
    private final Snapshot snapshot = new Snapshot();
    private Integer lastLogLine = 0;

    protected RemoteListService() throws RemoteException {
        super();
        recuperarEstado();
        salvarSnapshotPeriodicamente();
    }

    @Override
    public boolean  append(String listId, int valor) throws RemoteException {
        lock.lock();
        try {
            listas.putIfAbsent(listId, new ArrayList<>());
            listas.get(listId).add(valor);
            lastLogLine += 1;
            log.appendOperation(listId, valor, lastLogLine);
            return true;
        } finally {
            lock.unlock();
        }

    }

    @Override
    public int get(String listId, int index) throws RemoteException {
        return listas.getOrDefault(listId, new ArrayList<>()).get(index);
    }

    @Override
    public int remove(String listId) throws RemoteException {
        lock.lock();
        try {
            List<Integer> lista = listas.get(listId);
            if (lista == null || lista.isEmpty()) throw new RuntimeException("Lista vazia");
            int value = lista.removeLast();
            lastLogLine += 1;
            log.removeOperation(listId, lastLogLine);
            return value;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size(String listId) throws RemoteException {
        return listas.getOrDefault(listId, new ArrayList<>()).size();

    }

    @Override
    public List<Integer> getList(String listId) throws RemoteException {
        return listas.get(listId);
    }

    public void salvarSnapshotPeriodicamente() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(20000);
                    listas.get("snapshot").removeLast();
                    listas.get("snapshot").add(lastLogLine);
                    snapshot.saveSnapshot(listas);
                } catch (Exception e) {
                    break;
                }
            }
        }).start();
    }

    public void recuperarEstado() {
        lock.lock();
        try {
            Map<String, List<Integer>> snapshotData = snapshot.loadSnapshot();
            listas.clear();
            for (var entry : snapshotData.entrySet()) {
                listas.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            log.replayLog(listas,lastLogLine);
            listas.put("snapshot",new ArrayList<>(List.of(0)));
            System.out.println("Estado restaurado com sucesso.");
        } finally {
            lock.unlock();
        }
    }
}
