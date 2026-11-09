package br.com.triagem;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Caminho do paciente, igual ao do site:
 * recepção (sem cor, "Aguardando triagem") → classificação ("Aguardando atendimento") → chamada ("Em atendimento").
 */
public class Paciente {
    private static final Pattern CODIGO = Pattern.compile("[Pp]?(\\d{1,9})");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Recepção
    private final int codigo;
    private final String nome;
    private final int idade;
    private final String telefone;
    private final String endereco;
    private final String queixa;
    private final LocalDateTime horarioChegada;
    private final long ordemChegada;

    // Triagem
    private Prioridade prioridade;
    private String especialidade = "";
    private String sala = "";
    private String observacao = "";
    private LocalDateTime horarioTriagem;

    // Atendimento
    private StatusPaciente status = StatusPaciente.AGUARDANDO_TRIAGEM;
    private LocalDateTime horarioAtendimento;

    /** Paciente que acabou de chegar na recepção: ainda sem cor. */
    public Paciente(int codigo, String nome, int idade, String telefone, String endereco, String queixa,
                    LocalDateTime horarioChegada, long ordemChegada) {
        this.codigo = codigo;
        this.nome = nome;
        this.idade = idade;
        this.telefone = texto(telefone);
        this.endereco = texto(endereco);
        this.queixa = texto(queixa);
        this.horarioChegada = horarioChegada;
        this.ordemChegada = ordemChegada;
    }

    /** Paciente já classificado, triado no instante da chegada. Usado pelo benchmark e pelos testes das filas. */
    public Paciente(int codigo, String nome, int idade, Prioridade prioridade,
                    LocalDateTime horarioChegada, long ordemChegada) {
        this(codigo, nome, idade, "", "", "", horarioChegada, ordemChegada);
        classificar(prioridade, "", "", "", horarioChegada);
    }

    /** "P025", "p25" ou "25" → 25. */
    public static int lerCodigo(String texto) {
        Matcher m = texto == null ? null : CODIGO.matcher(texto.trim());

        if (m == null || !m.matches() || Integer.parseInt(m.group(1)) <= 0) {
            throw new IllegalArgumentException("Código inválido: \"" + texto + "\". Use P025 ou 25.");
        }

        return Integer.parseInt(m.group(1));
    }

    public static String formatarCodigo(int codigo) {
        return String.format("P%03d", codigo);
    }

    void classificar(Prioridade prioridade, String especialidade, String sala, String observacao,
                     LocalDateTime horario) {
        if (status != StatusPaciente.AGUARDANDO_TRIAGEM) {
            throw new IllegalStateException("O paciente " + getCodigoFormatado() + " já foi classificado.");
        }

        this.prioridade = prioridade;
        this.especialidade = texto(especialidade);
        this.sala = texto(sala);
        this.observacao = texto(observacao);
        this.horarioTriagem = horario;
        this.status = StatusPaciente.AGUARDANDO_ATENDIMENTO;
    }

    public void iniciarAtendimento(LocalDateTime horario) {
        this.status = StatusPaciente.EM_ATENDIMENTO;
        this.horarioAtendimento = horario;
    }

    /** Da chegada na recepção até o início do atendimento (spec 4.8). */
    public Duration getTempoEspera() {
        if (status != StatusPaciente.EM_ATENDIMENTO) {
            throw new IllegalStateException("O paciente " + getCodigoFormatado() + " ainda não foi atendido.");
        }

        return Duration.between(horarioChegada, horarioAtendimento);
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    public int getCodigo() {
        return codigo;
    }

    public String getCodigoFormatado() {
        return formatarCodigo(codigo);
    }

    public String getNome() {
        return nome;
    }

    public int getIdade() {
        return idade;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public String getQueixa() {
        return queixa;
    }

    public LocalDateTime getHorarioChegada() {
        return horarioChegada;
    }

    public long getOrdemChegada() {
        return ordemChegada;
    }

    /** null enquanto o paciente não passou pela triagem. */
    public Prioridade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(Prioridade prioridade) {
        this.prioridade = prioridade;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public String getSala() {
        return sala;
    }

    public String getObservacao() {
        return observacao;
    }

    public LocalDateTime getHorarioTriagem() {
        return horarioTriagem;
    }

    public StatusPaciente getStatus() {
        return status;
    }

    public LocalDateTime getHorarioAtendimento() {
        return horarioAtendimento;
    }

    @Override
    public String toString() {
        String cor = prioridade == null ? "sem classificação" : prioridade.getNivel() + " - " + prioridade.getDescricao();
        return String.format("Código: %s | Nome: %s | Idade: %d | Prioridade: %s | Chegada: %s | Status: %s",
                getCodigoFormatado(), nome, idade, cor, horarioChegada.format(HORA), status.getDescricao());
    }
}
