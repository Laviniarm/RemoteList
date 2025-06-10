# Sistema RMI para Compartilhamento de Listas

Este projeto apresenta um sistema de compartilhamento de listas via RMI, focado em **consistência** e **durabilidade** dos dados.

## Funcionalidades 

- **Listas Remotas**: Permite a manipulação (adicionar, remover, consultar) de múltiplas listas por múltiplos clientes.
- **Controle de Concorrência**: Usa um `ReentrantReadWriteLock` no servidor. Múltiplas leituras são paralelas, enquanto escritas são exclusivas, garantindo a integridade dos dados.
- **Persistência**: Salva o estado das listas com snapshots periódicos e um log de operações.
- **Recuperação de Falhas**: Em caso de queda do servidor, o estado é restaurado fielmente a partir do último snapshot e do log.

## Limitações Atuais

Apesar da robustez na consistência, o sistema possui limitações:

- **Baixa Escalabilidade**: Todas as operações de escrita são serializadas por um único servidor, criando um gargalo de desempenho sob alta carga.
- **Disponibilidade Limitada**: É um ponto único de falha (SPOF). Uma falha no servidor causa inatividade total do serviço, exigindo reinício manual.

> Os principais pontos de falha são o servidor, o registro RMI e a integridade dos arquivos de persistência. A recuperação garante a durabilidade, mas não a continuidade imediata.

## Como Melhorar (e os Trade-offs)

Para aumentar a escalabilidade e a disponibilidade:

- **Locks Por Lista**: Permite concorrência total em operações de escrita em listas diferentes, mas o servidor continua sendo um SPOF.
- **Arquitetura Distribuída (Sharding/Replicação)**:
  - **Sharding**: Distribui listas entre vários servidores para escalabilidade horizontal.
  - **Replicação**: Cria cópias redundantes do servidor para alta disponibilidade.

### O Grande Trade-off

Isso exige escolher entre:

- **Consistência forte**: Maior latência e complexidade.
- **Consistência eventual**: Melhor desempenho e disponibilidade, mas dados temporariamente inconsistentes.
