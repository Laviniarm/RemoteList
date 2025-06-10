package servidor;


import compartilhado.RemoteListInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RemoteListService extends UnicastRemoteObject implements RemoteListInterface {
    private final Map<String, List<Integer>> listas = new HashMap<>();
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();
    //private final ReentrantLock lock = new ReentrantLock();
    private final Log log = new Log();
    private final Snapshot snapshot = new Snapshot();
    private Integer lastLogLine = 0;

    protected RemoteListService() throws RemoteException {
        super();
        recuperarEstado();
        salvarSnapshotPeriodicamente();
    }

    @Override
    public boolean append(String listId, int valor) throws RemoteException {
        writeLock.lock();
        try {
            listas.putIfAbsent(listId, new ArrayList<>());
            listas.get(listId).add(valor);
            lastLogLine += 1;
            log.appendOperation(listId, valor, lastLogLine);
            return true;
        } finally {
            writeLock.unlock();
        }

    }

    @Override
    public int get(String listId, int index) throws RemoteException {
        readLock.lock();
        try {
            List<Integer> lista = listas.getOrDefault(listId, null);
            if (lista == null || index < 0 || index >= lista.size()) {
                throw new RemoteException("Lista ou índice inválido para get: " + listId + ", " + index);
            }
            return lista.get(index);
        } finally {
            readLock.unlock();
        }
    }


    @Override
    public int remove(String listId) throws RemoteException {
        writeLock.lock();
        try {
            List<Integer> lista = listas.get(listId);
            if (lista == null || lista.isEmpty()) throw new RuntimeException("Lista vazia");
            int value = lista.removeLast();
            lastLogLine += 1;
            log.removeOperation(listId, lastLogLine);
            return value;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int size(String listId) throws RemoteException {
        readLock.lock();
        try {
            return listas.getOrDefault(listId, new ArrayList<>()).size();
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public List<Integer> getList(String listId) throws RemoteException {
        readLock.lock();
        try {
            List<Integer> lista = listas.getOrDefault(listId, null);
            if (lista == null) {
                return Collections.unmodifiableList(new ArrayList<>());
            }
            return List.copyOf(lista);
        } finally {
            readLock.unlock();
        }
    }


    public void salvarSnapshotPeriodicamente() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(20000);
                    writeLock.lock();
                    try {
                        if (!listas.containsKey("snapshot")) {
                            listas.put("snapshot", new ArrayList<>(List.of(0)));
                        } else {
                            listas.get("snapshot").clear();
                        }
                        listas.get("snapshot").add(lastLogLine);
                        snapshot.saveSnapshot(listas);
                        System.out.println("Snapshot salvo. lastLogLine: " + lastLogLine);

                    } finally {
                        writeLock.unlock();
                    }
                } catch (InterruptedException e) {
                    System.err.println("Thread de snapshot interrompida.");
                    break;
                } catch (Exception e) {
                    System.err.println("Erro ao salvar snapshot: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void recuperarEstado() {
        writeLock.lock();
        try {
            Map<String, List<Integer>> snapshotData = snapshot.loadSnapshot();
            listas.clear();
            for (var entry : snapshotData.entrySet()) {
                listas.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            log.replayLog(listas, lastLogLine);
            listas.put("snapshot", new ArrayList<>(List.of(0)));
            System.out.println("Estado restaurado com sucesso.");
        } finally {
            writeLock.unlock();
        }
    }
}
