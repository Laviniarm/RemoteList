package compartilhado;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteListInterface extends Remote {
    boolean append(String listId, int valor) throws RemoteException;
    int get(String listId, int index) throws RemoteException;
    int remove(String listId) throws RemoteException;
    int size(String listid) throws RemoteException;
    List getList(String listid) throws RemoteException;
}
