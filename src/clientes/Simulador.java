package clientes;

import compartilhado.RemoteListInterface;

import java.rmi.Naming;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Simulador {

    static class ClientTask implements Runnable {
        private String clientName;
        private String listIdToAccess;

        public ClientTask(String clientName, String listIdToAccess) {
            this.clientName = clientName;
            this.listIdToAccess = listIdToAccess;
        }

        @Override
        public void run() {
            RemoteListInterface service;
            try {
                service = (RemoteListInterface) Naming.lookup("rmi://localhost/RemoteList");
                System.out.println(clientName + ": Conectado ao serviço RMI.");


                int valueToAppend = clientName.hashCode() % 1000; // Um valor semi-único para cada cliente
                System.out.println(clientName + ": Adicionando " + valueToAppend + " à lista '" + listIdToAccess + "'");
                boolean success = service.append(listIdToAccess, valueToAppend);
                System.out.println(clientName + ": Append " + (success ? "sucesso" : "falha") + " para " + listIdToAccess);

                // Pequeno atraso para simular processamento ou latência real
                Thread.sleep(50 + (long) (Math.random() * 100)); // 50 a 150 ms de delay

                // Operação de leitura (size)
                int currentSize = service.size(listIdToAccess);
                System.out.println(clientName + ": Tamanho atual da '" + listIdToAccess + "': " + currentSize);

                Thread.sleep(50 + (long) (Math.random() * 100));

                // Operação de leitura (getList)
                try {
                    List<Integer> currentList = service.getList(listIdToAccess);
                    System.out.println(clientName + ": Conteúdo da '" + listIdToAccess + "': " + currentList);
                } catch (Exception e) {
                    System.err.println(clientName + ": Erro ao obter lista '" + listIdToAccess + "': " + e.getMessage());
                }


                Thread.sleep(50 + (long) (Math.random() * 100));

                // Operação de escrita (append novamente)
                valueToAppend = (clientName.hashCode() + 1) % 1000;
                System.out.println(clientName + ": Adicionando outro valor " + valueToAppend + " à '" + listIdToAccess + "'");
                service.append(listIdToAccess, valueToAppend);

                Thread.sleep(50 + (long) (Math.random() * 100));

                if (currentSize > 0) { // Remova apenas se a lista não estiver vazia
                    System.out.println(clientName + ": Removendo um item da '" + listIdToAccess + "'");
                    int removedValue = service.remove(listIdToAccess);
                    System.out.println(clientName + ": Item removido: " + removedValue);
                }

                System.out.println(clientName + ": Finalizou suas operações.");

            } catch (Exception e) {
                System.err.println(clientName + ": Erro durante a execução: " + e.getMessage());
                // e.printStackTrace(); // Descomente para ver o stack trace completo durante o debug
            } finally {
                // Em um cenário real, você não desconecta explicitamente clientes RMI.
                // A conexão é gerenciada pelo RMI. No entanto, é bom garantir que os recursos sejam liberados.
                // Para testes de carga, a conexão é mantida pela JVM da thread.
            }
        }
    }

    public static void main(String[] args) {
        int numberOfClients = 10; // Defina quantos clientes você quer simular
        // Use um ExecutorService para gerenciar o pool de threads
        ExecutorService executor = Executors.newFixedThreadPool(numberOfClients);

        System.out.println("Iniciando simulação RMI com " + numberOfClients + " clientes...");

        for (int i = 1; i <= numberOfClients; i++) {
            String clientName = "Cliente-" + i;
            // Exemplo: metade dos clientes acessa "ListaA", a outra metade "ListaB"
            // ou a contenção geral (se tiver lock global).
            String listToAccess = (i % 2 == 0) ? "ListaA" : "ListaB";
            executor.submit(new ClientTask(clientName, listToAccess));
        }

        // Importante: Desligar o executor e esperar que todas as tarefas concluam
        executor.shutdown(); // Inicia o desligamento, mas não impede tarefas em andamento
        try {
            // Espera por no máximo 60 segundos para todas as tarefas terminarem
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Alguns clientes não terminaram a tempo. Forçando desligamento.");
                executor.shutdownNow(); // Tenta parar as tarefas em execução imediatamente
            }
        } catch (InterruptedException e) {
            System.err.println("O simulador foi interrompido enquanto esperava as tarefas.");
            executor.shutdownNow();
        }

        System.out.println("Simulação de clientes RMI finalizada.");
    }
}
