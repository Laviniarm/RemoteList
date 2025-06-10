package servidor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class Log {
    private final String logFile = "log.txt";
    private final ReentrantLock lock = new ReentrantLock();

    public void Operation(String operation, String listId, Integer value, int logId) {
        lock.lock();
        try (FileWriter writer = new FileWriter(logFile, true)) {
            writer.write(operation + ";" + listId + ";" + (value != null ? value : "") + ";" + logId + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void removeOperation(String listId, int logId) {
        Operation("remove", listId, null, logId);
    }

    public void appendOperation(String listId, int value, int logId) {
        Operation("append", listId, value, logId);
    }

    public void replayLog(Map<String, List<Integer>> listas, int lastLogId) {
        lock.lock();
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(";");
                String operacao = partes[0];
                String listId = partes[1];
                String valCampo = partes[2];
                int entryId = Integer.parseInt(partes[3]);

                if (entryId <= lastLogId) continue;

                listas.putIfAbsent(listId, new ArrayList<>());

                if ("append".equals(operacao)) {
                    int valor = Integer.parseInt(valCampo);
                    listas.get(listId).add(valor);
                } else if ("remove".equals(operacao)) {
                    List<Integer> lista = listas.get(listId);
                    if (lista != null && !lista.isEmpty()) {
                        lista.removeLast();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Log não encontrado. Nada a reexecutar.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
