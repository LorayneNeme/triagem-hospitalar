# Sistema de Triagem Hospitalar

## Estruturas utilizadas

- `PriorityQueue<Paciente>`: organiza a ordem de atendimento.
- `HashMap<Integer, Paciente>`: permite localizar pacientes pelo código.
- `Comparator<Paciente>`: compara primeiro a prioridade e, em empate, o horário de chegada.

## Prioridades

1. Vermelho
2. Laranja
3. Amarelo
4. Verde
5. Azul

Quanto menor o número, maior a prioridade.

## Funcionalidades

1. Cadastrar paciente.
2. Consultar próximo paciente (`peek`).
3. Chamar próximo paciente (`poll`).
4. Buscar paciente pelo código.
5. Alterar prioridade com remoção e reinserção na `PriorityQueue`.
6. Exibir a fila em ordem sem modificar a fila original.

```bash
javac -d out src/main/java/br/com/triagem/*.java
java -cp out br.com.triagem.Main
```
