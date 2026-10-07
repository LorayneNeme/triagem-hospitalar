// CADASTRO DE PACIENTE

const formulario = document.getElementById("formCadastro");

if (formulario) {

    formulario.addEventListener("submit", function (event) {

        event.preventDefault();

        const nome = document.getElementById("nome").value;
        const idade = document.getElementById("idade").value;
        const telefone = document.getElementById("telefone").value;
        const endereco = document.getElementById("endereco").value;
        const queixa = document.getElementById("queixa").value;

        // Gera o código do paciente

        const codigo =
            "A" + Math.floor(100 + Math.random() * 900);

        // Recupera pacientes já cadastrados

        const pacientesSalvos =
            JSON.parse(
                localStorage.getItem("pacientes")
            ) || [];

        // Cria o paciente

        const paciente = {

            codigo: codigo,

            nome: nome,

            idade: idade,

            telefone: telefone,

            endereco: endereco,

            queixa: queixa,

            horarioChegada:
                new Date().toISOString(),

            status:
                "Aguardando triagem"

        };

        // Adiciona o paciente

        pacientesSalvos.push(paciente);

        // Salva no navegador

        localStorage.setItem(
            "pacientes",
            JSON.stringify(pacientesSalvos)
        );

        // Mostra confirmação

        const confirmacao =
            document.getElementById(
                "confirmacaoCadastro"
            );

        const codigoPaciente =
            document.getElementById(
                "codigoPaciente"
            );

        const tituloCadastro =
            document.getElementById(
                "tituloCadastro"
            );

        if (confirmacao && codigoPaciente) {

            codigoPaciente.textContent =
                codigo;

            confirmacao.style.display =
                "block";

            formulario.style.display =
                "none";

            if (tituloCadastro) {

                tituloCadastro.style.display =
                    "none";

            }

        }

    });

}


// IMPRIMIR CÓDIGO

const btnImprimirCodigo =
    document.getElementById(
        "btnImprimirCodigo"
    );

if (btnImprimirCodigo) {

    btnImprimirCodigo.addEventListener(
        "click",
        function () {

            const codigo =
                document.getElementById(
                    "codigoPaciente"
                ).textContent;

            const janela =
                window.open(
                    "",
                    "_blank",
                    "width=500,height=500"
                );

            janela.document.write(`

                <!DOCTYPE html>

                <html lang="pt-BR">

                <head>

                    <meta charset="UTF-8">

                    <title>
                        Código do Paciente
                    </title>

                    <style>

                        body {
                            font-family: Arial, sans-serif;
                            text-align: center;
                            padding: 50px;
                        }

                        h2 {
                            margin-bottom: 30px;
                        }

                        .codigo {
                            font-size: 60px;
                            font-weight: bold;
                            margin: 30px 0;
                        }

                        p {
                            font-size: 18px;
                        }

                    </style>

                </head>

                <body>

                    <h2>
                        Triagem Hospitalar
                    </h2>

                    <p>
                        Código do paciente
                    </p>

                    <div class="codigo">
                        ${codigo}
                    </div>

                    <p>
                        Aguarde sua chamada.
                    </p>

                </body>

                </html>

            `);

            janela.document.close();

            janela.print();

        }
    );

}


// NOVO CADASTRO

const btnNovoCadastro =
    document.getElementById(
        "btnNovoCadastro"
    );

if (btnNovoCadastro) {

    btnNovoCadastro.addEventListener(
        "click",
        function () {

            if (formulario) {

                formulario.reset();

                formulario.style.display =
                    "block";

            }

            const confirmacao =
                document.getElementById(
                    "confirmacaoCadastro"
                );

            if (confirmacao) {

                confirmacao.style.display =
                    "none";

            }

            const tituloCadastro =
                document.getElementById(
                    "tituloCadastro"
                );

            if (tituloCadastro) {

                tituloCadastro.style.display =
                    "flex";

            }

        }
    );

}


// MÁSCARA DE TELEFONE

const campoTelefone =
    document.getElementById("telefone");

if (campoTelefone) {

    campoTelefone.addEventListener(
        "input",
        function () {

            let valor =
                campoTelefone.value.replace(
                    /\D/g,
                    ""
                );

            if (valor.length > 11) {

                valor =
                    valor.substring(0, 11);

            }

            if (valor.length > 6) {

                valor =
                    valor.replace(
                        /^(\d{2})(\d{5})(\d{0,4})$/,
                        "($1) $2-$3"
                    );

            } else if (valor.length > 2) {

                valor =
                    valor.replace(
                        /^(\d{2})(\d{0,5})$/,
                        "($1) $2"
                    );

            }

            campoTelefone.value =
                valor;

        }
    );

}


// DATA ATUAL

function atualizarDatas() {

    const hoje =
        new Date();

    const dia =
        String(
            hoje.getDate()
        ).padStart(2, "0");

    const meses = [

        "janeiro",
        "fevereiro",
        "março",
        "abril",
        "maio",
        "junho",
        "julho",
        "agosto",
        "setembro",
        "outubro",
        "novembro",
        "dezembro"

    ];

    const mes =
        meses[hoje.getMonth()];

    const ano =
        hoje.getFullYear();

    const data =
        `${dia} de ${mes} de ${ano}`;

    const dataAtual =
        document.getElementById(
            "dataAtual"
        );

    const dataPainel =
        document.getElementById(
            "dataPainel"
        );

    if (dataAtual) {

        dataAtual.textContent =
            data;

    }

    if (dataPainel) {

        dataPainel.textContent =
            data;

    }

}

atualizarDatas();


// CLASSIFICAÇÃO DE RISCO

const cardsRisco =
    document.querySelectorAll(
        ".risk-card"
    );

const btnConfirmar =
    document.getElementById(
        "btnConfirmar"
    );

let prioridadeSelecionada = null;

if (cardsRisco.length > 0) {

    cardsRisco.forEach(
        function (card) {

            card.addEventListener(
                "click",
                function () {

                    // Remove seleção anterior

                    cardsRisco.forEach(
                        function (item) {

                            item.classList.remove(
                                "selected"
                            );

                            const icone =
                                item.querySelector(
                                    ".risk-radio i"
                                );

                            if (icone) {

                                icone.className =
                                    "bi bi-circle";

                            }

                        }
                    );

                    // Seleciona o card

                    card.classList.add(
                        "selected"
                    );

                    const iconeSelecionado =
                        card.querySelector(
                            ".risk-radio i"
                        );

                    if (iconeSelecionado) {

                        iconeSelecionado.className =
                            "bi bi-check-circle-fill";

                    }

                    prioridadeSelecionada =
                        card.dataset.priority;

                    if (btnConfirmar) {

                        btnConfirmar.disabled =
                            false;

                    }

                }
            );

        }
    );

}


// CONFIRMAR CLASSIFICAÇÃO

if (btnConfirmar) {

    btnConfirmar.addEventListener(
        "click",
        function () {

            if (!prioridadeSelecionada) {

                alert(
                    "Erro: selecione uma classificação de risco."
                );

                return;

            }

            // Especialidade

            const campoEspecialidade =
                document.getElementById(
                    "especialidade"
                );

            const especialidade =
                campoEspecialidade
                    ? campoEspecialidade.value
                    : "";

            // Sala

            const campoSala =
                document.getElementById(
                    "sala"
                );

            const sala =
                campoSala
                    ? campoSala.value
                    : "";

            // Observação

            const campoObservacao =
                document.getElementById(
                    "observacao"
                );

            const observacao =
                campoObservacao
                    ? campoObservacao.value
                    : "";

            // Recupera pacientes

            const pacientes =
                JSON.parse(
                    localStorage.getItem(
                        "pacientes"
                    )
                ) || [];

            // Recupera paciente selecionado

            const codigoPaciente =
                localStorage.getItem(
                    "pacienteEmTriagem"
                );

            const paciente =
                pacientes.find(
                    function (p) {

                        return p.codigo ===
                            codigoPaciente;

                    }
                );

            if (!paciente) {

                alert(
                    "Erro: nenhum paciente selecionado para triagem."
                );

                return;

            }

            // Atualiza os dados

            paciente.prioridade =
                prioridadeSelecionada;

            paciente.especialidade =
                especialidade;

            paciente.sala =
                sala;

            paciente.observacao =
                observacao;

            paciente.status =
                "Aguardando atendimento";

            paciente.horarioTriagem =
                new Date().toISOString();

            // Salva

            localStorage.setItem(
                "pacientes",
                JSON.stringify(pacientes)
            );

            alert(
                "Classificação realizada com sucesso!\n\n" +

                "Código: " +
                paciente.codigo +

                "\nPaciente: " +
                paciente.nome +

                "\nClassificação: " +
                paciente.prioridade +

                "\nEspecialidade: " +
                paciente.especialidade +

                "\nSala: " +
                (
                    paciente.sala ||
                    "Não informada"
                )
            );

        }
    );

}


// FILA DE TRIAGEM

const listaPacientes =
    document.getElementById(
        "listaPacientes"
    );

const quantidadePacientes =
    document.getElementById(
        "quantidadePacientes"
    );

const semPacientes =
    document.getElementById(
        "semPacientes"
    );

if (listaPacientes) {

    carregarFilaTriagem();

}


function carregarFilaTriagem() {

    const pacientes =
        JSON.parse(
            localStorage.getItem(
                "pacientes"
            )
        ) || [];

    const pacientesAguardando =
        pacientes.filter(
            function (paciente) {

                return paciente.status ===
                    "Aguardando triagem";

            }
        );

    pacientesAguardando.sort(
        function (a, b) {

            return (
                new Date(
                    a.horarioChegada
                ) -

                new Date(
                    b.horarioChegada
                )
            );

        }
    );

    if (quantidadePacientes) {

        quantidadePacientes.textContent =
            pacientesAguardando.length;

    }

    listaPacientes.innerHTML = "";

    if (
        pacientesAguardando.length === 0
    ) {

        if (semPacientes) {

            semPacientes.style.display =
                "flex";

        }

        return;

    }

    if (semPacientes) {

        semPacientes.style.display =
            "none";

    }

    pacientesAguardando.forEach(
        function (paciente) {

            const card =
                document.createElement(
                    "div"
                );

            card.className =
                "patient-card";

            card.innerHTML = `

                <div class="patient-code">

                    <span>Senha</span>

                    <strong>
                        ${paciente.codigo}
                    </strong>

                </div>

                <div class="patient-info">

                    <h3>
                        ${paciente.nome}
                    </h3>

                    <p>
                        <i class="bi bi-chat-left-text"></i>
                        ${paciente.queixa}
                    </p>

                </div>

                <div class="patient-time">

                    <span>
                        Chegada
                    </span>

                    <strong>
                        ${formatarHorario(
                            paciente.horarioChegada
                        )}
                    </strong>

                    <small class="tempo-espera">

                        ${calcularEspera(
                            paciente.horarioChegada
                        )}

                    </small>

                </div>

                <div class="patient-actions">

                    <button
                        type="button"
                        class="btn-iniciar"
                        data-codigo="${paciente.codigo}">

                        <i class="bi bi-play-fill"></i>

                        Iniciar triagem

                    </button>

                    <button
                        type="button"
                        class="btn-remover"
                        data-codigo="${paciente.codigo}"
                        title="Remover paciente">

                        <i class="bi bi-trash3"></i>

                    </button>

                </div>

            `;

            listaPacientes.appendChild(
                card
            );

        }
    );

    const botoes =
        document.querySelectorAll(
            ".btn-iniciar"
        );

    botoes.forEach(
        function (botao) {

            botao.addEventListener(
                "click",
                function () {

                    const codigo =
                        botao.getAttribute(
                            "data-codigo"
                        );

                    localStorage.setItem(
                        "pacienteEmTriagem",
                        codigo
                    );

                    window.location.href =
                        "triagem.html";

                }
            );

        }
    );

    const botoesRemover =
        document.querySelectorAll(
            ".btn-remover"
        );

    botoesRemover.forEach(
        function (botao) {

            botao.addEventListener(
                "click",
                function () {

                    const codigo =
                        botao.getAttribute(
                            "data-codigo"
                        );

                    const pacientes =
                        JSON.parse(
                            localStorage.getItem(
                                "pacientes"
                            )
                        ) || [];

                    const paciente =
                        pacientes.find(
                            function (p) {

                                return p.codigo ===
                                    codigo;

                            }
                        );

                    if (!paciente) {

                        return;

                    }

                    const confirmar =
                        confirm(
                            "Tem certeza que deseja remover o paciente " +
                            paciente.nome +
                            " da fila?"
                        );

                    if (!confirmar) {

                        return;

                    }

                    const novaLista =
                        pacientes.filter(
                            function (p) {

                                return p.codigo !==
                                    codigo;

                            }
                        );

                    localStorage.setItem(
                        "pacientes",
                        JSON.stringify(
                            novaLista
                        )
                    );

                    carregarFilaTriagem();

                }
            );

        }
    );

}


// FORMATAR HORÁRIO

function formatarHorario(data) {

    const dataObj =
        new Date(data);

    const horas =
        String(
            dataObj.getHours()
        ).padStart(2, "0");

    const minutos =
        String(
            dataObj.getMinutes()
        ).padStart(2, "0");

    return `${horas}:${minutos}`;

}


// TEMPO DE ESPERA

function calcularEspera(data) {

    const chegada =
        new Date(data);

    const agora =
        new Date();

    const diferenca =
        agora - chegada;

    const minutos =
        Math.floor(
            diferenca / 60000
        );

    if (minutos < 1) {

        return "Agora";

    }

    if (minutos < 60) {

        return `${minutos} min aguardando`;

    }

    const horas =
        Math.floor(
            minutos / 60
        );

    const minutosRestantes =
        minutos % 60;

    if (minutosRestantes === 0) {

        return `${horas}h aguardando`;

    }

    return `${horas}h ${minutosRestantes}min aguardando`;

}


// DASHBOARD DA EQUIPE

const totalPacientes =
    document.getElementById("totalPacientes");

const aguardandoTriagem =
    document.getElementById("aguardandoTriagem");

const aguardandoAtendimento =
    document.getElementById("aguardandoAtendimento");

const emAtendimento =
    document.getElementById("emAtendimento");

const listaAtendimento =
    document.getElementById("listaAtendimento");

const semAtendimento =
    document.getElementById("semAtendimento");


// Variáveis dos gráficos

let graficoAtendimentos = null;
let graficoRisco = null;
let graficoQueixas = null;
let graficoIdade = null;


// Verifica a data atual

function obterDataHoje() {

    const hoje = new Date();

    const ano = hoje.getFullYear();

    const mes = String(
        hoje.getMonth() + 1
    ).padStart(2, "0");

    const dia = String(
        hoje.getDate()
    ).padStart(2, "0");

    return `${ano}-${mes}-${dia}`;
}


// Cria o resumo do dia

function criarResumoDoDia(pacientes, data) {

    const resumo = {

        data: data,

        total: pacientes.length,

        queixas: {},

        idades: {
            "0-12": 0,
            "13-17": 0,
            "18-29": 0,
            "30-59": 0,
            "60+": 0
        },

        riscos: {
            Vermelho: 0,
            Laranja: 0,
            Amarelo: 0,
            Verde: 0,
            Azul: 0
        }

    };


    pacientes.forEach(function (paciente) {

        // Conta as queixas

        const queixa =
            paciente.queixa || "Não informado";

        resumo.queixas[queixa] =
            (resumo.queixas[queixa] || 0) + 1;


        // Conta as idades

        const idade =
            Number(paciente.idade);

        if (idade <= 12) {

            resumo.idades["0-12"]++;

        } else if (idade <= 17) {

            resumo.idades["13-17"]++;

        } else if (idade <= 29) {

            resumo.idades["18-29"]++;

        } else if (idade <= 59) {

            resumo.idades["30-59"]++;

        } else if (idade >= 60) {

            resumo.idades["60+"]++;

        }


        // Conta as classificações

        if (
            paciente.prioridade &&
            resumo.riscos[paciente.prioridade] !== undefined
        ) {

            resumo.riscos[paciente.prioridade]++;

        }

    });

    return resumo;
}


// Guarda o dia anterior no histórico

function verificarNovoDia() {

    const hoje =
        obterDataHoje();

    const ultimaData =
        localStorage.getItem(
            "ultimaDataSistema"
        );


    // Primeiro acesso ao sistema

    if (!ultimaData) {

        localStorage.setItem(
            "ultimaDataSistema",
            hoje
        );

        return;
    }


    // Continua no mesmo dia

    if (ultimaData === hoje) {
        return;
    }


    // Recupera os pacientes do dia anterior

    const pacientes =
        JSON.parse(
            localStorage.getItem("pacientes")
        ) || [];


    // Cria o histórico

    if (pacientes.length > 0) {

        const historico =
            JSON.parse(
                localStorage.getItem(
                    "historicoAtendimentos"
                )
            ) || [];


        const resumo =
            criarResumoDoDia(
                pacientes,
                ultimaData
            );


        const indice =
            historico.findIndex(
                function (item) {

                    return item.data === ultimaData;

                }
            );


        if (indice >= 0) {

            historico[indice] =
                resumo;

        } else {

            historico.push(resumo);

        }


        localStorage.setItem(
            "historicoAtendimentos",
            JSON.stringify(historico)
        );

    }


    // Limpa os pacientes do dia anterior

    localStorage.setItem(
        "pacientes",
        JSON.stringify([])
    );


    // Define o novo dia

    localStorage.setItem(
        "ultimaDataSistema",
        hoje
    );

}


// Executa a verificação do dia

verificarNovoDia();


// Só executa se estiver no Dashboard

if (totalPacientes) {

    carregarDashboard();

}


// Carrega o Dashboard

function carregarDashboard() {

    const pacientes =
        JSON.parse(
            localStorage.getItem("pacientes")
        ) || [];


    // CONTADORES

    totalPacientes.textContent =
        pacientes.length;


    aguardandoTriagem.textContent =
        pacientes.filter(
            function (paciente) {

                return paciente.status ===
                    "Aguardando triagem";

            }
        ).length;


    aguardandoAtendimento.textContent =
        pacientes.filter(
            function (paciente) {

                return paciente.status ===
                    "Aguardando atendimento";

            }
        ).length;


    emAtendimento.textContent =
        pacientes.filter(
            function (paciente) {

                return paciente.status ===
                    "Em atendimento";

            }
        ).length;


    // CLASSIFICAÇÕES

    const vermelho =
        contarRisco(
            pacientes,
            "Vermelho"
        );

    const laranja =
        contarRisco(
            pacientes,
            "Laranja"
        );

    const amarelo =
        contarRisco(
            pacientes,
            "Amarelo"
        );

    const verde =
        contarRisco(
            pacientes,
            "Verde"
        );

    const azul =
        contarRisco(
            pacientes,
            "Azul"
        );


    document.getElementById(
        "totalVermelho"
    ).textContent = vermelho;

    document.getElementById(
        "totalLaranja"
    ).textContent = laranja;

    document.getElementById(
        "totalAmarelo"
    ).textContent = amarelo;

    document.getElementById(
        "totalVerde"
    ).textContent = verde;

    document.getElementById(
        "totalAzul"
    ).textContent = azul;


    // PACIENTES AGUARDANDO ATENDIMENTO

    const pacientesAtendimento =
        pacientes.filter(
            function (paciente) {

                return paciente.status ===
                    "Aguardando atendimento";

            }
        );


    listaAtendimento.innerHTML = "";


    if (pacientesAtendimento.length === 0) {

        semAtendimento.style.display =
            "block";

    } else {

        semAtendimento.style.display =
            "none";


        pacientesAtendimento.forEach(
            function (paciente) {

                const card =
                    document.createElement("div");


                card.className =
                    "dashboard-patient";


                card.innerHTML = `

                    <div class="dashboard-patient-code">
                        ${paciente.codigo}
                    </div>

                    <div class="dashboard-patient-info">

                        <strong>
                            ${paciente.nome}
                        </strong>

                        <span>
                            Queixa: ${paciente.queixa}
                        </span>

                    </div>

                    <div class="dashboard-patient-priority">
                        ${paciente.prioridade}
                    </div>

                    <div class="dashboard-patient-specialty">
                        ${paciente.especialidade || "Não informado"}
                    </div>

                    <div>

                        <span class="dashboard-status">
                            Aguardando atendimento
                        </span>

                    </div>

                `;


                listaAtendimento.appendChild(
                    card
                );

            }
        );

    }


    // Atualiza os gráficos

    atualizarGraficos(pacientes);

}


// Conta classificação de risco

function contarRisco(
    pacientes,
    risco
) {

    return pacientes.filter(
        function (paciente) {

            return paciente.prioridade ===
                risco;

        }
    ).length;

}


// Atualiza todos os gráficos

function atualizarGraficos(pacientes) {

    if (typeof Chart === "undefined") {

        console.error(
            "Chart.js não foi carregado."
        );

        return;
    }


    const historico =
        JSON.parse(
            localStorage.getItem(
                "historicoAtendimentos"
            )
        ) || [];


    const hoje =
        obterDataHoje();


    // Inclui o dia atual

    const resumoHoje =
        criarResumoDoDia(
            pacientes,
            hoje
        );


    const historicoCompleto =
        historico.filter(
            function (item) {

                return item.data !== hoje;

            }
        );


    historicoCompleto.push(
        resumoHoje
    );


    // Ordena por data

    historicoCompleto.sort(
        function (a, b) {

            return a.data.localeCompare(
                b.data
            );

        }
    );


    // Gráfico de atendimentos

    criarGraficoAtendimentos(
        historicoCompleto
    );


    // Gráfico de risco

    criarGraficoRisco(
        resumoHoje
    );


    // Gráfico de queixas

    criarGraficoQueixas(
        resumoHoje
    );


    // Gráfico de idade

    criarGraficoIdade(
        resumoHoje
    );


    // Indicadores

    atualizarIndicadores(
        resumoHoje,
        historicoCompleto
    );

}


// GRÁFICO DE ATENDIMENTOS

function criarGraficoAtendimentos(
    historico
) {

    const canvas =
        document.getElementById(
            "graficoAtendimentos"
        );


    if (!canvas) return;


    const labels =
        historico.map(
            function (item) {

                const partes =
                    item.data.split("-");

                return partes[2] +
                    "/" +
                    partes[1];

            }
        );


    const valores =
        historico.map(
            function (item) {

                return item.total;

            }
        );


    if (graficoAtendimentos) {

        graficoAtendimentos.destroy();

    }


    graficoAtendimentos =
        new Chart(
            canvas,
            {

                type: "bar",

                data: {

                    labels: labels,

                    datasets: [
                        {

                            label:
                                "Atendimentos",

                            data:
                                valores,

                            borderWidth: 1

                        }
                    ]

                },

                options: {

                    responsive: true,

                    maintainAspectRatio: false,

                    scales: {

                        y: {

                            beginAtZero: true,

                            ticks: {

                                precision: 0

                            }

                        }

                    }

                }

            }
        );

}


// GRÁFICO DE RISCO

function criarGraficoRisco(
    resumo
) {

    const canvas =
        document.getElementById(
            "graficoRisco"
        );


    if (!canvas) return;


    if (graficoRisco) {

        graficoRisco.destroy();

    }


    graficoRisco =
        new Chart(
            canvas,
            {

                type: "doughnut",

                data: {

                    labels: [
                        "Vermelho",
                        "Laranja",
                        "Amarelo",
                        "Verde",
                        "Azul"
                    ],

                   datasets: [
    {

        data: [

            resumo.riscos.Vermelho,

            resumo.riscos.Laranja,

            resumo.riscos.Amarelo,

            resumo.riscos.Verde,

            resumo.riscos.Azul

        ],

        backgroundColor: [
            "#dc3545",
            "#fd7e14",
            "#ffc107",
            "#198754",
            "#0d6efd"
        ],

        borderWidth: 1

    }
]

                },

                options: {

                    responsive: true,

                    maintainAspectRatio: false

                }

            }
        );

}


// GRÁFICO DE QUEIXAS

function criarGraficoQueixas(
    resumo
) {

    const canvas =
        document.getElementById(
            "graficoQueixas"
        );


    if (!canvas) return;


    if (graficoQueixas) {

        graficoQueixas.destroy();

    }


    const lista =
        Object.entries(
            resumo.queixas
        )
        .sort(
            function (a, b) {

                return b[1] - a[1];

            }
        )
        .slice(0, 5);


    graficoQueixas =
        new Chart(
            canvas,
            {

                type: "bar",

                data: {

                    labels:
                        lista.map(
                            function (item) {

                                return item[0];

                            }
                        ),

                    datasets: [
                        {

                            label:
                                "Quantidade",

                            data:
                                lista.map(
                                    function (item) {

                                        return item[1];

                                    }
                                ),

                            borderWidth: 1

                        }
                    ]

                },

                options: {

                    indexAxis: "y",

                    responsive: true,

                    maintainAspectRatio: false

                }

            }
        );

}


// GRÁFICO DE IDADE

function criarGraficoIdade(
    resumo
) {

    const canvas =
        document.getElementById(
            "graficoIdade"
        );


    if (!canvas) return;


    if (graficoIdade) {

        graficoIdade.destroy();

    }


    graficoIdade =
        new Chart(
            canvas,
            {

                type: "bar",

                data: {

                    labels: [
                        "0-12",
                        "13-17",
                        "18-29",
                        "30-59",
                        "60+"
                    ],

                    datasets: [
                        {

                            label:
                                "Pacientes",

                            data: [

                                resumo.idades["0-12"],

                                resumo.idades["13-17"],

                                resumo.idades["18-29"],

                                resumo.idades["30-59"],

                                resumo.idades["60+"]

                            ],

                            borderWidth: 1

                        }
                    ]

                },

                options: {

                    responsive: true,

                    maintainAspectRatio: false,

                    scales: {

                        y: {

                            beginAtZero: true,

                            ticks: {

                                precision: 0

                            }

                        }

                    }

                }

            }
        );

}


// INDICADORES

function atualizarIndicadores(
    resumo,
    historico
) {

    const idade =
        Object.entries(
            resumo.idades
        )
        .sort(
            function (a, b) {

                return b[1] - a[1];

            }
        )[0];


    const queixa =
        Object.entries(
            resumo.queixas
        )
        .sort(
            function (a, b) {

                return b[1] - a[1];

            }
        )[0];


    const risco =
        Object.entries(
            resumo.riscos
        )
        .sort(
            function (a, b) {

                return b[1] - a[1];

            }
        )[0];


    const idadeElemento =
        document.getElementById(
            "idadePredominante"
        );

    const queixaElemento =
        document.getElementById(
            "queixaPrincipal"
        );

    const riscoElemento =
        document.getElementById(
            "riscoPrincipal"
        );

    const historicoElemento =
        document.getElementById(
            "totalHistorico"
        );


    if (idadeElemento) {

        idadeElemento.textContent =
            idade && idade[1] > 0
                ? idade[0]
                : "-";

    }


    if (queixaElemento) {

        queixaElemento.textContent =
            queixa
                ? queixa[0]
                : "-";

    }


    if (riscoElemento) {

        riscoElemento.textContent =
            risco && risco[1] > 0
                ? risco[0]
                : "-";

    }


    if (historicoElemento) {

        const totalHistorico =
            historico.reduce(
                function (total, item) {

                    return total +
                        Number(item.total || 0);

                },
                0
            );

        historicoElemento.textContent =
            totalHistorico;

    }

}

// PAINEL DO PACIENTE

const painelBtnTriagem =
    document.getElementById(
        "btnTriagem"
    );

const painelBtnConsulta =
    document.getElementById(
        "btnConsulta"
    );

const painelBtnChamar =
    document.getElementById(
        "btnChamar"
    );

const painelBtnRechamar =
    document.getElementById(
        "btnRechamar"
    );

const painelSenha =
    document.getElementById(
        "senhaAtual"
    );

const painelNome =
    document.getElementById(
        "nomeAtual"
    );

const painelStatus =
    document.getElementById(
        "textoStatus"
    );

const painelDestino =
    document.getElementById(
        "destinoAtual"
    );

const painelTv =
    document.getElementById(
        "painelTv"
    );

const painelBtnTelaCheia =
    document.getElementById(
        "btnTelaCheia"
    );

if (
    painelBtnTriagem &&
    painelBtnConsulta &&
    painelBtnChamar
) {

    let tipoChamada =
        "triagem";

    let pacienteChamado =
        null;


    // TRIAGEM

    painelBtnTriagem.addEventListener(
        "click",
        function () {

            tipoChamada =
                "triagem";

            painelBtnTriagem.classList.add(
                "ativo"
            );

            painelBtnTriagem.classList.add(
                "active"
            );

            painelBtnConsulta.classList.remove(
                "ativo"
            );

            painelBtnConsulta.classList.remove(
                "active"
            );

            limparPainel();

        }
    );


    // CONSULTA

    painelBtnConsulta.addEventListener(
        "click",
        function () {

            tipoChamada =
                "consulta";

            painelBtnConsulta.classList.add(
                "ativo"
            );

            painelBtnConsulta.classList.add(
                "active"
            );

            painelBtnTriagem.classList.remove(
                "ativo"
            );

            painelBtnTriagem.classList.remove(
                "active"
            );

            limparPainel();

        }
    );


    // CHAMAR PRÓXIMO

    painelBtnChamar.addEventListener(
        "click",
        function () {

            const pacientes =
                JSON.parse(
                    localStorage.getItem(
                        "pacientes"
                    )
                ) || [];

            let pacientesDisponiveis =
                [];


            // FILA DA TRIAGEM

            if (
                tipoChamada ===
                "triagem"
            ) {

                pacientesDisponiveis =
                    pacientes.filter(
                        function (paciente) {

                            return paciente.status ===
                                "Aguardando triagem";

                        }
                    );

                pacientesDisponiveis.sort(
                    function (a, b) {

                        return (
                            new Date(
                                a.horarioChegada
                            ) -

                            new Date(
                                b.horarioChegada
                            )
                        );

                    }
                );

            }


            // FILA DE CONSULTA

            else {

                pacientesDisponiveis =
                    pacientes.filter(
                        function (paciente) {

                            return paciente.status ===
                                "Aguardando atendimento";

                        }
                    );

                pacientesDisponiveis.sort(
                    function (a, b) {

                        const prioridadeA =
                            pesoPrioridade(
                                a.prioridade
                            );

                        const prioridadeB =
                            pesoPrioridade(
                                b.prioridade
                            );

                        if (
                            prioridadeA !==
                            prioridadeB
                        ) {

                            return (
                                prioridadeA -
                                prioridadeB
                            );

                        }

                        return (
                            new Date(
                                a.horarioTriagem || 0
                            ) -

                            new Date(
                                b.horarioTriagem || 0
                            )
                        );

                    }
                );

            }


            // NENHUM PACIENTE

            if (
                pacientesDisponiveis.length ===
                0
            ) {

                alert(
                    tipoChamada === "triagem"

                        ? "Não há pacientes aguardando triagem."

                        : "Não há pacientes aguardando atendimento."
                );

                return;

            }


            // Pega o primeiro paciente

            pacienteChamado =
                pacientesDisponiveis[0];


            // Se for consulta,
            // muda o status para Em atendimento

            if (
                tipoChamada ===
                "consulta"
            ) {

                const indice =
                    pacientes.findIndex(
                        function (paciente) {

                            return paciente.codigo ===
                                pacienteChamado.codigo;

                        }
                    );

                if (indice !== -1) {

                    pacientes[indice].status =
                        "Em atendimento";

                    localStorage.setItem(
                        "pacientes",
                        JSON.stringify(
                            pacientes
                        )
                    );

                    // Atualiza o objeto mostrado

                    pacienteChamado =
                        pacientes[indice];

                }

            }


            mostrarPacientePainel(
                pacienteChamado
            );

        }
    );


    // RECHAMAR

    if (painelBtnRechamar) {

        painelBtnRechamar.addEventListener(
            "click",
            function () {

                if (!pacienteChamado) {

                    alert(
                        "Nenhum paciente foi chamado ainda."
                    );

                    return;

                }

                mostrarPacientePainel(
                    pacienteChamado
                );

            }
        );

    }


    // MOSTRA O PACIENTE

    function mostrarPacientePainel(
        paciente
    ) {

        if (!paciente) {

            return;

        }

        painelSenha.textContent =
            paciente.codigo;

        painelNome.textContent =
            paciente.nome;


        if (
            tipoChamada ===
            "triagem"
        ) {

            painelStatus.textContent =
                "PACIENTE CHAMADO PARA TRIAGEM";

            painelDestino.textContent =
                "TRIAGEM";

            falarChamada(
                paciente.codigo,
                paciente.nome,
                "triagem",
                ""
            );

        }


        else {

            painelStatus.textContent =
                "PACIENTE CHAMADO PARA CONSULTA";

            const sala =
                paciente.sala ||
                "Sala não informada";

            const especialidade =
                paciente.especialidade ||
                "Consulta";

            painelDestino.textContent =
                sala +
                " — " +
                especialidade;

            falarChamada(
                paciente.codigo,
                paciente.nome,
                "consulta",
                sala
            );

        }


        if (painelTv) {

            painelTv.classList.remove(
                "chamada-animada"
            );

            void painelTv.offsetWidth;

            painelTv.classList.add(
                "chamada-animada"
            );

        }

    }


    // LIMPA O PAINEL

    function limparPainel() {

        if (painelSenha) {

            painelSenha.textContent =
                "---";

        }

        if (painelNome) {

            painelNome.textContent =
                "Aguardando chamada";

        }

        if (
            tipoChamada ===
            "triagem"
        ) {

            painelStatus.textContent =
                "Próximo paciente para triagem";

            painelDestino.textContent =
                "Aguarde sua chamada";

        }

        else {

            painelStatus.textContent =
                "Próximo paciente para consulta";

            painelDestino.textContent =
                "Aguarde sua chamada";

        }

    }


    // CHAMADA POR VOZ

    function falarChamada(
        codigo,
        nome,
        tipo,
        sala
    ) {

        if (
            !("speechSynthesis" in window)
        ) {

            return;

        }

        window.speechSynthesis.cancel();

        let mensagem;


        if (tipo === "triagem") {

            mensagem =
                "Senha " +
                codigo +
                ". " +
                nome +
                ". Dirija-se à triagem.";

        }

        else {

            mensagem =
                "Senha " +
                codigo +
                ". " +
                nome +
                ". Dirija-se à " +
                (
                    sala ||
                    "consulta"
                ) +
                ".";

        }

        const fala =
            new SpeechSynthesisUtterance(
                mensagem
            );

        fala.lang =
            "pt-BR";

        fala.rate =
            0.9;

        fala.pitch =
            1;

        window.speechSynthesis.speak(
            fala
        );

    }


    // TELA CHEIA

    if (
        painelBtnTelaCheia &&
        painelTv
    ) {

        painelBtnTelaCheia.addEventListener(
            "click",
            async function (event) {

                event.preventDefault();

                event.stopPropagation();

                try {

                    if (
                        !document.fullscreenElement
                    ) {

                        await painelTv.requestFullscreen();

                    }

                    else {

                        await document.exitFullscreen();

                    }

                }

                catch (erro) {

                    console.error(
                        "Erro na tela cheia:",
                        erro
                    );

                }

            }
        );

    }


    // Atualiza ícone da tela cheia

    document.addEventListener(
        "fullscreenchange",
        function () {

            if (!painelBtnTelaCheia) {

                return;

            }

            if (
                document.fullscreenElement
            ) {

                painelBtnTelaCheia.innerHTML =
                    '<i class="bi bi-fullscreen-exit"></i>';

                painelBtnTelaCheia.title =
                    "Sair da tela cheia";

            }

            else {

                painelBtnTelaCheia.innerHTML =
                    '<i class="bi bi-arrows-fullscreen"></i>';

                painelBtnTelaCheia.title =
                    "Aumentar tela";

            }

        }
    );

}


// PESO DAS PRIORIDADES

function pesoPrioridade(
    prioridade
) {

    if (
        prioridade ===
        "Vermelho"
    ) {

        return 1;

    }

    if (
        prioridade ===
        "Laranja"
    ) {

        return 2;

    }

    if (
        prioridade ===
        "Amarelo"
    ) {

        return 3;

    }

    if (
        prioridade ===
        "Verde"
    ) {

        return 4;

    }

    if (
        prioridade ===
        "Azul"
    ) {

        return 5;

    }

    return 6;

}