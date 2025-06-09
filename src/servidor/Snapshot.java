package servidor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Snapshot {
    private final String snapshotFile = "snapshot.dat";
    private final ReentrantLock lock = new ReentrantLock();

    public void saveSnapshot(Map<String, List<Integer>> listas) {
        lock.lock();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(snapshotFile))) {
            oos.writeObject(listas);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public Map<String, List<Integer>> loadSnapshot() {
        lock.lock();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(snapshotFile))) {
            return (Map<String, List<Integer>>) ois.readObject();
        } catch (Exception e) {
            System.out.println("Nenhum snapshot encontrado. Iniciando com listas vazias.");
            return new java.util.HashMap<>();
        } finally {
            lock.unlock();
        }
    }

}
