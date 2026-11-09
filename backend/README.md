# Sistema de Triagem Hospitalar — Backend

Backend em Java da A3 de Estrutura de Dados. Segue o mesmo caminho do site: o paciente chega,
espera a triagem na ordem de chegada, recebe uma cor e passa para a fila de atendimento, ordenada
pela classificação de risco e, no empate, pela ordem de chegada.

```
Recepção ──► Fila de triagem ──► Classificação ──► Fila de atendimento ──► Médico
(sem cor)    FIFO (ArrayDeque)   (cor, sala...)    cor + ordem de chegada
```

| Status | Quando |
|---|---|
| Aguardando triagem | cadastrado, ainda sem cor |
| Aguardando atendimento | classificado, na fila de atendimento |
| Em atendimento | chamado pelo médico |

## Prioridades

1. Vermelho
2. Laranja
3. Amarelo
4. Verde
5. Azul

Quanto menor o número, maior a prioridade. Com a mesma cor, quem chegou primeiro é atendido primeiro
(spec 4.3). O desempate usa a **ordem de cadastro** (`ordemChegada`), não o relógio: se o relógio do
computador for ajustado para trás, a ordem da fila não muda.

## Estruturas utilizadas

- `ArrayDeque<Paciente>`: fila de triagem (FIFO). Cadastrar e chamar são O(1).
- `FilaAtendimento`: interface da fila de atendimento, com três implementações:
  - `FilaHeap` — `PriorityQueue` (heap binário). **É a estrutura padrão do sistema.**
  - `FilaArvore` — `TreeSet` (árvore rubro-negra).
  - `FilaListaOrdenada` — `ArrayList` mantida em ordem **decrescente** (base de comparação).
- `LinkedHashMap<Integer, Paciente>`: localiza qualquer paciente, em qualquer etapa, pelo código.
- `PacienteComparator`: compara a prioridade e depois a ordem de chegada.

O código do paciente é gerado no cadastro: `P001`, `P002`... A busca aceita `P025`, `p25` ou `25`.

### Complexidade por estrutura

| Operação | Heap (`PriorityQueue`) | Árvore (`TreeSet`) | Lista ordenada (`ArrayList`) |
|---|---|---|---|
| Inserir paciente | O(log n) | O(log n) | O(n) |
| Consultar próximo | O(1) | O(log n) | O(1) |
| Chamar próximo | O(log n) | O(log n) | O(1) |
| Alterar prioridade (remover + reinserir) | O(n) | O(log n) | O(n) |
| Listar a fila em ordem | O(n log n) | O(n) | O(n) |
| Buscar por código (`LinkedHashMap`) | O(1) | O(1) | O(1) |

Na lista ordenada, o próximo paciente fica no **fim** do `ArrayList`, então chamá-lo é um
`remove(size - 1)` sem deslocar nada. O custo O(n) fica na inserção: um paciente novo de uma cor que
já está na fila entra perto do início da lista e desloca todos os outros. Isso aparece no cenário
`MESMA_PRIORIDADE`, em que todo paciente novo vai para o início.

## Funcionalidades (menu)

1. Cadastrar paciente (nome, idade, telefone, endereço, queixa). O código é gerado.
2. Ver a fila de triagem.
3. Classificar: Enter chama o próximo da triagem, ou digite um código. Define cor, especialidade, sala e observação.
4. Consultar o próximo da fila de atendimento.
5. Chamar o próximo. O status passa para "Em atendimento" e o menu mostra chegada, atendimento e tempo de espera (spec 4.8).
6. Buscar paciente pelo código. Mostra a etapa, a posição na fila ou os horários.
7. Alterar prioridade. Mostra a prioridade atual, depois "anterior → nova" e a nova posição (spec 4.5).
8. Ver a fila de atendimento em ordem.
9. Estatísticas: total, aguardando triagem, aguardando atendimento, atendidos, quantidade por cor, tempo médio e maior tempo de espera.
0. Sair.

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

## API HTTP

`ApiServidor` expõe o `TriagemService` para o site. Usa o servidor HTTP que já vem no JDK
(`com.sun.net.httpserver`) e um leitor/escritor de JSON próprio (`Json`): nenhuma dependência.

```bash
javac -d out src/main/java/br/com/triagem/*.java
java -cp out br.com.triagem.ApiServidor          # porta 8080
java -cp out br.com.triagem.ApiServidor 9000     # outra porta
```

- Os dados ficam em memória. Ao parar o servidor, tudo é perdido.
- CORS liberado (`Access-Control-Allow-Origin: *`). O site pode ser aberto de outro endereço, inclusive pelo Live Server.
- Respostas em JSON UTF-8. Erros vêm como `{"erro": "mensagem"}`.

### Rotas

| Método | Rota | Corpo | Resposta |
|---|---|---|---|
| POST | `/api/pacientes` | `nome`, `idade`, `telefone`, `endereco`, `queixa` | 201 + paciente (`Location: /api/pacientes/P001`) |
| GET | `/api/pacientes` | — | todos os pacientes, em ordem de cadastro |
| GET | `/api/pacientes/{codigo}` | — | paciente |
| GET | `/api/fila-triagem` | — | fila de triagem, em ordem de chegada |
| POST | `/api/fila-triagem/chamar` | — | primeiro da triagem (sai da fila), ou 204 se vazia |
| POST | `/api/pacientes/{codigo}/classificacao` | `prioridade`, `especialidade`, `sala`, `observacao` | paciente, já na fila de atendimento |
| GET | `/api/fila-atendimento` | — | fila de atendimento, na ordem de chamada |
| GET | `/api/fila-atendimento/proximo` | — | próximo, sem tirar da fila, ou 204 se vazia |
| POST | `/api/fila-atendimento/chamar` | — | paciente chamado ("Em atendimento"), ou 204 se vazia |
| PUT | `/api/pacientes/{codigo}/prioridade` | `prioridade` | paciente com a nova posição |
| GET | `/api/estatisticas` | — | totais, quantidade por cor e tempos de espera |

- `{codigo}` aceita `P001`, `p1` ou `1`.
- `idade` aceita número ou texto (`30` ou `"30"`), como vem de um `<input>`.
- `prioridade` aceita o nome da cor (`"Laranja"`, sem diferenciar maiúsculas) ou o nível (`2` ou `"2"`).

Códigos de erro:

| Código | Quando |
|---|---|
| 400 | JSON inválido, campo obrigatório faltando, idade fora de 0–130, código ou prioridade inválidos |
| 404 | paciente ou rota inexistente |
| 405 | método errado para a rota (ex.: `DELETE /api/pacientes`) |
| 409 | classificar duas vezes, ou alterar a prioridade de quem não está na fila de atendimento |

### Paciente em JSON

```json
{
  "codigo": "P001",
  "nome": "Ana",
  "idade": 30,
  "telefone": "11 99999-0000",
  "endereco": "Rua A, 10",
  "queixa": "Febre",
  "prioridade": "Laranja",
  "nivelPrioridade": 2,
  "especialidade": "Clínica geral",
  "sala": "Sala 2",
  "observacao": "",
  "status": "Aguardando atendimento",
  "horarioChegada": "2026-11-09T13:20:00",
  "horarioTriagem": "2026-11-09T13:35:00",
  "horarioAtendimento": null,
  "tempoEsperaMinutos": null,
  "posicao": 1
}
```

- `prioridade` e `nivelPrioridade` são `null` até a classificação.
- `tempoEsperaMinutos` é a espera da chegada ao atendimento. Só aparece com o status "Em atendimento".
- `posicao` é a posição na fila em que o paciente está (triagem ou atendimento), começando em 1. É `null` para quem já foi chamado.

### Exemplos com `fetch`

```js
const API = "http://localhost:8080/api";

async function chamarApi(metodo, rota, corpo) {
  const resposta = await fetch(API + rota, {
    method: metodo,
    headers: corpo ? { "Content-Type": "application/json" } : {},
    body: corpo ? JSON.stringify(corpo) : undefined,
  });
  if (resposta.status === 204) return null;            // fila vazia
  const dados = await resposta.json();
  if (!resposta.ok) throw new Error(dados.erro);        // 400, 404, 409...
  return dados;
}

// Recepção
const paciente = await chamarApi("POST", "/pacientes", {
  nome: "Ana", idade: "30", telefone: "11 99999-0000", endereco: "Rua A, 10", queixa: "Febre",
});
console.log(paciente.codigo); // "P001"

// Triagem
const filaTriagem = await chamarApi("GET", "/fila-triagem");
const chamado = await chamarApi("POST", "/fila-triagem/chamar");
await chamarApi("POST", `/pacientes/${chamado.codigo}/classificacao`, {
  prioridade: "Laranja", especialidade: "Clínica geral", sala: "Sala 2", observacao: "",
});

// Painel da TV e médico
const filaAtendimento = await chamarApi("GET", "/fila-atendimento");
const proximo = await chamarApi("GET", "/fila-atendimento/proximo");
const atendido = await chamarApi("POST", "/fila-atendimento/chamar");

// Alterar prioridade e estatísticas
await chamarApi("PUT", "/pacientes/P001/prioridade", { prioridade: "Vermelho" });
const estatisticas = await chamarApi("GET", "/estatisticas");
// { total, aguardandoTriagem, aguardandoAtendimento, atendidos,
//   porCor: { Vermelho, Laranja, Amarelo, Verde, Azul },
//   tempoMedioEsperaSegundos, maiorTempoEsperaSegundos }
```

O `js/script.js` do site ainda usa o `localStorage`. Trocar pelas chamadas acima faz a recepção, a
triagem e o painel da TV enxergarem a mesma fila.

## Testes de desempenho

Mede as três estruturas da fila de atendimento com 100, 1.000, 10.000, 100.000 e 1.000.000 de
pacientes, em cinco cenários:

| Cenário | Cores | Ordem de inserção |
|---|---|---|
| `EQUILIBRADO` | sorteadas, 20% cada | ordem de chegada |
| `MAIORIA_AZUL` | 70% Azul, o resto sorteado | ordem de chegada |
| `MAIORIA_VERMELHO` | 70% Vermelho, o resto sorteado | ordem de chegada |
| `MESMA_PRIORIDADE` | todos Amarelo | ordem de chegada |
| `CHEGADA_ALEATORIA` | sorteadas, 20% cada | embaralhada (spec 6: "pacientes chegando aleatoriamente") |

Em `CHEGADA_ALEATORIA`, cada paciente mantém a sua ordem de chegada, mas entra na fila fora de ordem.

```bash
mvnw.cmd -q compile
java -Xms2g -Xmx2g -cp target/classes br.com.triagem.Benchmark
```

Cada combinação é medida 5 vezes (com `System.gc()` antes de cada uma) e o resultado usa a
**mediana**, para que uma pausa do coletor de lixo não distorça a curva. A memória fixa
(`-Xms2g -Xmx2g`) evita que a JVM redimensione o heap no meio da medição. A execução completa
leva alguns minutos.

A lista ordenada não é medida com 1.000.000 de pacientes: a inserção O(n) por paciente
tornaria o teste lento demais (O(n²) no total).

### Arquivo `resultados/benchmark.csv`

Pronto para abrir no Excel em português: UTF-8 com BOM, `;` como separador e vírgula decimal.

| Coluna | O que é |
|---|---|
| `estrutura`, `cenario`, `n` | o que foi medido |
| `insercao_ms` | inserir os n pacientes |
| `consulta_ns` | uma consulta do próximo (média de 1.000) |
| `remocao_ms` | chamar os n pacientes até esvaziar a fila |
| `ciclo_ms` | inserção + remoção: o caminho completo de n pacientes pela fila |
| `pacientes_por_segundo` | n ÷ ciclo: quantos pacientes a estrutura recebe e chama por segundo |
| `alteracoes` | quantas alterações de prioridade foram medidas: min(1.000, n) |
| `alteracao_us_por_op` | custo médio de UMA alteração (remover, mudar a cor, reinserir), em µs |

"Pacientes por segundo" usa só inserção e remoção, que são feitas n vezes. A alteração de prioridade
é feita um número fixo de vezes, por isso aparece à parte, como custo por operação.

A consulta soma os códigos numa variável local e grava o total uma vez só, no fim. Assim o tempo
medido é o do `consultarPrimeiro`, não o da escrita na variável `volatile`.

## Pendências

Pontos levantados na revisão do código. Todos resolvidos.

### Regra de negócio

- [x] **Desempate dependia do relógio.** O `PacienteComparator` agora compara a prioridade e depois
  só a `ordemChegada`.

### Testes de desempenho

- [x] **Cenário "pacientes chegando aleatoriamente"** (spec 6): `CHEGADA_ALEATORIA`.
- [x] **Lista ordenada: chamar o próximo custava O(n)** (`remove(0)`). A lista agora fica em ordem
  decrescente e o próximo sai do fim, em O(1).
- [x] **Tempo de consulta pouco confiável.** A soma fica numa variável local e o `volatile` é gravado uma vez.
- [x] **"Pacientes por segundo" misturava operações.** Agora é n ÷ (inserção + remoção). A alteração
  aparece à parte, em µs por operação.
- [x] **CSV no Excel em português:** UTF-8 com BOM, `;` e vírgula decimal.

### Menu do terminal

- [x] A busca aceita o código como é exibido (`P025`).
- [x] Chamar e buscar mostram chegada, atendimento e tempo de espera (spec 4.8).
- [x] Alterar prioridade mostra a prioridade atual antes da nova (spec 4.5).

### Integração com o site

- [x] **Fila de triagem no backend.** Cadastro sem cor numa FIFO (`ArrayDeque`), `chamarParaTriagem()`
  e `classificar()`, que define a cor e insere na `FilaAtendimento`.
- [x] **Critério de desempate.** O grupo escolheu a **hora de chegada** (spec 4.3), via `ordemChegada`.
  O site ainda desempata pela hora da triagem; com a API, a ordem passa a vir do backend.
- [x] **Dados do paciente.** Telefone, endereço, queixa, especialidade, sala, observação e horário da triagem.
- [x] **Status.** Os três do site: "Aguardando triagem", "Aguardando atendimento", "Em atendimento".
- [x] **Código do paciente.** Gerado pelo backend em sequência (`P001`, `P002`...), sem repetição.
- [x] **Ligar os dois.** API HTTP pronta e testada (seção "API HTTP"). Falta só trocar o
  `localStorage` do `script.js` pelas chamadas `fetch`. Isso fica com quem cuida do site.
