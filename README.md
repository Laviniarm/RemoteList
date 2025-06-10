O sistema atual, que utiliza RMI para compartilhar listas entre clientes, prioriza a consistência forte dos dados por meio de locks e recuperação via logs e snapshots. No entanto, essa escolha impacta negativamente a escalabilidade e a disponibilidade.

Sua arquitetura de servidor central único é o principal gargalo, pois todas as operações passam por ele, e os locks globais serializam o acesso mesmo a listas diferentes. Além disso, a persistência em disco de logs e snapshots pode limitar o desempenho de I/O.

Os pontos de falha incluem o próprio servidor RMI, o sistema de arquivos (que pode corromper logs/snapshots) e falhas no gerenciamento de concorrência. O sistema lida com falhas do servidor através da recuperação de estado via snapshot e log, garantindo a persistência dos dados, mas não a continuidade do serviço.

Para melhorar a escalabilidade, poderíamos implementar:

Sharding das listas entre múltiplos servidores, Uso de ReadWriteLock para permitir leituras simultâneas e Criação de um cluster de servidores com replicação.
