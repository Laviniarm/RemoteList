package servidor;

import compartilhado.RemoteListInterface;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Servidor {
    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            RemoteListInterface service = new RemoteListService();
            Naming.rebind("rmi://localhost/RemoteList", service);
            System.out.println("Servidor RMI pronto!");
        } catch (Exception e) {
            System.err.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
