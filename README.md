<div align="center">

# 🏥 Triagem Hospitalar

**Sistema de triagem com classificação de risco e fila de atendimento por prioridade**

Projeto da A3 de **Estrutura de Dados**

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-wrapper-C71A36?logo=apachemaven&logoColor=white)
![HTML](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white)
![CSS](https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?logo=javascript&logoColor=black)

</div>

---

## 📋 Sobre o projeto

O paciente chega à recepção, espera a triagem na ordem de chegada e recebe uma **cor de risco**.
Depois entra na fila de atendimento, que chama primeiro os casos mais graves. Com a mesma cor,
quem chegou antes é atendido antes.

```mermaid
flowchart LR
    A[🧾 Recepção] --> B[⏳ Fila de triagem<br/>ordem de chegada]
    B --> C[🩺 Classificação<br/>cor, sala, especialidade]
    C --> D[📊 Fila de atendimento<br/>cor + ordem de chegada]
    D --> E[👨‍⚕️ Médico]
```

O projeto tem duas partes:

| Parte | Pasta | O que faz |
|---|---|---|
| 🌐 **Site** | [`html/`](html), [`css/`](css), [`js/`](js) | Telas da recepção, triagem, painel da TV e dashboard da equipe |
| ☕ **Backend** | [`backend/`](backend) | Filas em Java, menu no terminal, API HTTP e testes de desempenho |

## 🚦 Classificação de risco

| Nível | Cor | Atendimento |
|:---:|---|---|
| 1 | 🔴 **Vermelho** | Emergência |
| 2 | 🟠 **Laranja** | Muito urgente |
| 3 | 🟡 **Amarelo** | Urgente |
| 4 | 🟢 **Verde** | Pouco urgente |
| 5 | 🔵 **Azul** | Não urgente |

Quanto menor o nível, maior a prioridade.

## 🌐 Site

| Tela | Arquivo | Para quem |
|---|---|---|
| Cadastro | [`index.html`](html/index.html) | Recepção |
| Fila de triagem | [`fila-triagem.html`](html/fila-triagem.html) | Enfermagem |
| Classificação de risco | [`triagem.html`](html/triagem.html) | Enfermagem |
| Painel do paciente | [`painel-paciente.html`](html/painel-paciente.html) | TV da sala de espera |
| Dashboard | [`dashboard.html`](html/dashboard.html) | Equipe |

**Como abrir:** abra `html/index.html` no navegador ou use o Live Server do VS Code.
Hoje os dados ficam no `localStorage` do navegador.

## ☕ Backend em Java

A fila de atendimento foi feita com **três estruturas**, para comparar o desempenho:

| Estrutura | Classe Java | Inserir | Chamar próximo |
|---|---|:---:|:---:|
| Heap binário *(padrão)* | `PriorityQueue` | O(log n) | O(log n) |
| Árvore rubro-negra | `TreeSet` | O(log n) | O(log n) |
| Lista ordenada | `ArrayList` | O(n) | O(1) |

A fila de triagem é uma FIFO (`ArrayDeque`). A busca por código usa um `LinkedHashMap`, em O(1).

### Como executar

Precisa só do **JDK 17** ou mais novo.

```bash
cd backend

# Menu no terminal
javac -d out src/main/java/br/com/triagem/*.java
java -cp out br.com.triagem.Main

# API HTTP (porta 8080)
java -cp out br.com.triagem.ApiServidor

# Testes (o wrapper baixa o Maven sozinho)
./mvnw test        # Linux/Mac
mvnw.cmd test      # Windows
```

📖 Rotas da API, testes de desempenho e bugs encontrados: [**README do backend**](backend/README.md).

## 📁 Estrutura

```
triagem-hospitalar/
├── html/          telas do site
├── css/           estilos
├── js/            lógica do site (localStorage)
└── backend/
    ├── src/main/java/br/com/triagem/   filas, serviço, menu, API e benchmark
    └── src/test/java/br/com/triagem/   testes JUnit
```

## 👥 Equipe

- Andressa
- Carlos
- Gustavo
- Lorayne
