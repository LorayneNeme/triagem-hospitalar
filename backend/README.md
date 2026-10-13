# Sistema de Triagem Hospitalar — Backend

Backend em Java da A3 de Estrutura de Dados. Organiza a fila de atendimento pela
classificação de risco (protocolo de cores) e, no empate, pela ordem de chegada.

## Prioridades

1. Vermelho
2. Laranja
3. Amarelo
4. Verde
5. Azul

Quanto menor o número, maior a prioridade. Com a mesma cor, quem chegou primeiro é atendido primeiro.
Se dois pacientes forem cadastrados no mesmo instante, vale a ordem de cadastro.

## Estruturas utilizadas

- `FilaAtendimento`: interface da fila de atendimento, com três implementações:
  - `FilaHeap` — `PriorityQueue` (heap binário). **É a estrutura padrão do sistema.**
  - `FilaArvore` — `TreeSet` (árvore rubro-negra).
  - `FilaListaOrdenada` — `ArrayList` mantida em ordem (base de comparação).
- `HashMap<Integer, Paciente>`: localiza qualquer paciente (aguardando ou já atendido) pelo código.
- `PacienteComparator`: compara prioridade, depois horário de chegada, depois ordem de cadastro.

### Complexidade por estrutura

| Operação | Heap (`PriorityQueue`) | Árvore (`TreeSet`) | Lista ordenada (`ArrayList`) |
|---|---|---|---|
| Inserir paciente | O(log n) | O(log n) | O(n) |
| Consultar próximo | O(1) | O(log n) | O(1) |
| Chamar próximo | O(log n) | O(log n) | O(n) |
| Alterar prioridade (remover + reinserir) | O(n) | O(log n) | O(n) |
| Listar a fila em ordem | O(n log n) | O(n) | O(n) |
| Buscar por código (`HashMap`) | O(1) | O(1) | O(1) |

## Funcionalidades (menu)

1. Cadastrar paciente (código, nome, idade e prioridade).
2. Consultar próximo paciente.
3. Chamar próximo paciente — status passa de "Aguardando" para "Em atendimento" e mostra o tempo de espera.
4. Buscar paciente pelo código — mostra a posição na fila ou o tempo de espera.
5. Alterar prioridade — remove, altera e reinsere o paciente na fila.
6. Consultar a fila em ordem de atendimento.
7. Estatísticas — total, aguardando, atendidos, quantidade por cor, tempo médio e maior tempo de espera.

## Como executar o menu

Só com o JDK (17 ou mais recente):

```bash
javac -d out src/main/java/br/com/triagem/*.java
java -cp out br.com.triagem.Main
```

## Como rodar os testes

Não precisa instalar o Maven: o wrapper baixa sozinho na primeira execução.

```bash
./mvnw test        # Linux/Mac
mvnw.cmd test      # Windows
```

## Testes de desempenho

Mede as três estruturas com 100, 1.000, 10.000, 100.000 e 1.000.000 de pacientes, em quatro cenários
(distribuição equilibrada, maioria Azul, maioria Vermelho e todos com a mesma prioridade).
Para cada combinação mede inserção, consulta, 1.000 alterações de prioridade, remoção de todos
e pacientes processados por segundo.

```bash
mvnw.cmd -q compile
java -Xms2g -Xmx2g -cp target/classes br.com.triagem.Benchmark
```

Cada combinação é medida 5 vezes (com `System.gc()` antes de cada uma) e o resultado usa a
**mediana**, para que uma pausa do coletor de lixo não distorça a curva. A memória fixa
(`-Xms2g -Xmx2g`) evita que a JVM redimensione o heap no meio da medição. A execução completa
leva alguns minutos.

O resultado é impresso na tela e gravado em `resultados/benchmark.csv`.
A lista ordenada não é medida com 1.000.000 de pacientes: a inserção O(n) por paciente
tornaria o teste lento demais (O(n²) no total).
