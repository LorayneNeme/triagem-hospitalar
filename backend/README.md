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

## Pendências

Pontos levantados na revisão do código que ainda não foram resolvidos.

### Regra de negócio

- [ ] **Desempate depende do relógio.** O `PacienteComparator` compara o horário de chegada antes da
  ordem de cadastro. Se o relógio do computador voltar no tempo (ajuste manual ou sincronização),
  um paciente cadastrado depois pode passar à frente de outro da mesma cor. Comparar só prioridade e
  depois `ordemChegada` evita isso e dá a mesma ordem com o relógio normal.

### Testes de desempenho

- [ ] **Falta o cenário "pacientes chegando aleatoriamente"**, citado na seção 6 do documento. Hoje os
  pacientes entram sempre em ordem de chegada, o que favorece a lista ordenada quando todos têm a
  mesma cor (a inserção vira praticamente um "adicionar no fim").
- [ ] **Lista ordenada: chamar o próximo custa O(n)** porque usa `remove(0)`. Guardar a lista em ordem
  decrescente e remover do fim deixaria essa operação em O(1). Se ficar como está, explicar no
  relatório que a lentidão na remoção vem dessa escolha.
- [ ] **Tempo de consulta pouco confiável.** O laço de 1.000 consultas mede principalmente a escrita na
  variável `sumidouro`, não o `consultarPrimeiro`. Acumular numa variável local e gravar uma vez só
  no fim.
- [ ] **"Pacientes por segundo" mistura operações de pesos diferentes:** `n` inserções e remoções com um
  número fixo de alterações (`min(1000, n)`). Deixar claro no relatório ou medir separadamente.
- [ ] **CSV no Excel em português:** o arquivo usa vírgula como separador e UTF-8 sem BOM, então abre
  numa coluna só. Por enquanto, importar por Dados → De Texto/CSV.

### Menu do terminal

- [ ] A busca só aceita o número (`25`), não o código como é exibido (`P025`).
- [ ] Chamar/buscar não mostra o horário de início do atendimento (spec 4.8: chegada, atendimento e
  tempo de espera).
- [ ] Alterar prioridade não mostra a prioridade atual antes da nova (spec 4.5).

### Integração com o site

O site (`js/script.js`) e o backend modelam o caminho do paciente de formas diferentes:

```
SITE:     Recepção ──► Fila 1: TRIAGEM ──────────► Classificação ──► Fila 2: CONSULTA ──────► Médico
          (sem cor)    ordem de chegada (FIFO)     (cor, sala...)    prioridade + hora da triagem

BACKEND:  Cadastro já com a cor ─────────────────────────────────► Fila de prioridade ────► Atendimento
                                                                   prioridade + hora de chegada
```

- [ ] **Adicionar a fila de triagem no backend.** Hoje o cadastro já exige a cor e o paciente entra
  direto na fila de prioridade; a etapa "chegou e aguarda classificação" não existe. Proposta:
  - cadastro sem prioridade, colocando o paciente numa fila FIFO (`ArrayDeque<Paciente>`) com status
    "Aguardando triagem";
  - operação `chamarParaTriagem()` (remove o primeiro da FIFO);
  - operação `classificar(codigo, prioridade, ...)` que define a cor e insere na `FilaAtendimento`
    com status "Aguardando atendimento";
  - o `chamarProximo()` atual continua sendo a chamada para consulta ("Em atendimento").
- [ ] **Alinhar o critério de desempate.** O site desempata a fila de consulta pela **hora da triagem**
  (`horarioTriagem`); o backend desempata pela **hora de chegada** (`horarioChegada`). O grupo precisa
  escolher um. O documento da A3 (seção 4.3) fala em horário de chegada.
- [ ] **Alinhar os dados do paciente.** O site guarda campos que o backend não tem: telefone, endereço,
  queixa, especialidade, sala, observação e horário da triagem.
- [ ] **Alinhar os status.** Site: "Aguardando triagem", "Aguardando atendimento", "Em atendimento".
  Backend: "Aguardando", "Em atendimento".
- [ ] **Alinhar o código do paciente.** Site: senha sorteada `A123` (pode repetir). Backend: número
  informado no cadastro, exibido como `P001`.
- [ ] **Ligar os dois.** Expor o `TriagemService` como API HTTP e trocar o `localStorage` do
  `script.js` por chamadas a ela. Hoje cada navegador tem a sua própria fila: a recepção, a triagem e
  o painel da TV não enxergam os mesmos dados.
