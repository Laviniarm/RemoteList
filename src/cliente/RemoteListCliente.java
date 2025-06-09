package cliente;

import compartilhado.RemoteListInterface;

import java.rmi.Naming;

public class RemoteListCliente {
    public static void main(String[] args) {
        try {
            RemoteListInterface service = (RemoteListInterface) Naming.lookup("rmi://localhost/RemoteList");

            boolean sucesso = service.append("Lista1", 1);
            if (sucesso) {
                System.out.println("Valor adicionado com sucesso!");
            }
            service.append("Lista1", 2);

            System.out.println("Valor na posição 0: " + service.get("Lista1", 0));
            System.out.println("Tamanho da lista1: " + service.size("Lista1"));

            System.out.println("Valores da lista: " + service.getList("Lista1"));

            service.remove("Lista1");
            System.out.println("Tamanho da lista1 apos remoção: " + service.size("Lista1"));

            System.out.println("Valores da lista: " + service.getList("Lista1"));

//            service.append("Lista2", 4);
//            service.append("Lista2", 5);
//            service.append("Lista2", 6);
//            System.out.println("Valor na posição 0: " + service.get("Lista2", 0));
//            System.out.println("Tamanho da Lista2: " + service.size("Lista2"));
//            int removido2 = service.remove("Lista2");
//            System.out.println("Removido: " + removido2);
//            System.out.println("Tamanho após remoção: " + service.size("Lista2"));
//            service.getList("Lista1");


        } catch (Exception e) {
            System.err.println("Erro no cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
