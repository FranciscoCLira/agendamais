package com.agendademais.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.agendademais.entities.Instituicao;

/**
 * Entidade que representa uma Região.
 * Uma Região compreende um agrupamento de cidades em um Estado de um País.
 * 
 * Formato do CodRegiao: "PPEENN" (6 caracteres)
 * - PP = Sigla do país (ISO 3166-1 alpha-2: ex. BR, DE)
 * - EE = Sigla do estado (2 letras)
 * - NN = Número da região (2 dígitos)
 * Exemplos: BR SP 01, BR RJ 02, DE BY 01
 */
@Entity
@Table(
    name = "regiao",
    uniqueConstraints = {
        @UniqueConstraint(name = "ux_regiao_cod_instituicao", columnNames = {"cod_regiao", "id_instituicao"})
    }
)
public class Regiao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código da região no formato PPEENN (6 caracteres)
     * Validação: 4 letras (posições 1-4) + 2 números (posições 5-6)
     * Nota: Múltiplas regiões podem ter o mesmo codRegiao (ex: BR SP 01, BR SP 02)
     * A unicidade é garantida pela combinação (codRegiao + pais + estado)
     */
    @Column(name = "cod_regiao", length = 6, nullable = false)
    private String codRegiao;

    /**
     * Nome da região (máximo 100 caracteres)
     */
    @Column(length = 100, nullable = false)
    private String nomeRegiao;

    /**
     * Referência ao País (Local com tipoLocal=1)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_pais", nullable = false)
    private Local pais;

    /**
     * Referência ao Estado (Local com tipoLocal=2)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_estado", nullable = false)
    private Local estado;

    /**
     * Cidades que compõem essa região (Local com tipoLocal=3)
     * Uma região pode conter várias cidades
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "regiao_cidades", joinColumns = @JoinColumn(name = "id_regiao"), inverseJoinColumns = @JoinColumn(name = "id_cidade"))
    private List<Local> cidades = new ArrayList<>();

    /**
        * Instituição proprietária desta região
        */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "id_instituicao", nullable = false)
        private Instituicao instituicao;

        /**
     * Data da última atualização do registro
     */
    private LocalDate dataUltimaAtualizacao;

    // --- Constructors ---

    public Regiao() {
        this.dataUltimaAtualizacao = LocalDate.now();
        this.cidades = new ArrayList<>();
    }

    public Regiao(String codRegiao, String nomeRegiao, Local pais, Local estado) {
        this.codRegiao = codRegiao;
        this.nomeRegiao = nomeRegiao;
        this.pais = pais;
        this.estado = estado;
        this.dataUltimaAtualizacao = LocalDate.now();
        this.cidades = new ArrayList<>();
    }

    // --- Validation Methods ---

    /**
     * Valida o formato do código da região (PPEENN)
     * 
     * @return true se válido, false caso contrário
     */
    public boolean isCodigoValido() {
        if (codRegiao == null || codRegiao.length() != 6) {
            return false;
        }

        // Verifica se os 4 primeiros caracteres são letras
        String letras = codRegiao.substring(0, 4);
        if (!letras.matches("^[A-Za-z]{4}$")) {
            return false;
        }

        // Verifica se os 2 últimos caracteres são números
        String numeros = codRegiao.substring(4, 6);
        if (!numeros.matches("^[0-9]{2}$")) {
            return false;
        }

        return true;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodRegiao() {
        return codRegiao;
    }

    public void setCodRegiao(String codRegiao) {
        this.codRegiao = codRegiao;
        this.dataUltimaAtualizacao = LocalDate.now();
    }

    public String getNomeRegiao() {
        return nomeRegiao;
    }

    public void setNomeRegiao(String nomeRegiao) {
        this.nomeRegiao = nomeRegiao;
        this.dataUltimaAtualizacao = LocalDate.now();
    }

    public Local getPais() {
        return pais;
    }

    public void setPais(Local pais) {
        this.pais = pais;
        this.dataUltimaAtualizacao = LocalDate.now();
    }

    public Local getEstado() {
        return estado;
    }

    public void setEstado(Local estado) {
        this.estado = estado;
        this.dataUltimaAtualizacao = LocalDate.now();
    }

    public List<Local> getCidades() {
        return cidades;
    }

    public void setCidades(List<Local> cidades) {
        this.cidades = cidades;
        this.dataUltimaAtualizacao = LocalDate.now();
    }

    public Instituicao getInstituicao() {
        return instituicao;
    }

    public void setInstituicao(Instituicao instituicao) {
        this.instituicao = instituicao;
    }

    public void adicionarCidade(Local cidade) {
        if (!this.cidades.contains(cidade)) {
            this.cidades.add(cidade);
            this.dataUltimaAtualizacao = LocalDate.now();
        }
    }

    public void removerCidade(Local cidade) {
        this.cidades.remove(cidade);
        this.dataUltimaAtualizacao = LocalDate.now();
    }

    public LocalDate getDataUltimaAtualizacao() {
        return dataUltimaAtualizacao;
    }

    public void setDataUltimaAtualizacao(LocalDate dataUltimaAtualizacao) {
        this.dataUltimaAtualizacao = dataUltimaAtualizacao;
    }

    @Override
    public String toString() {
        return "Regiao{" +
                "id=" + id +
                ", codRegiao='" + codRegiao + '\'' +
                ", nomeRegiao='" + nomeRegiao + '\'' +
                ", pais=" + (pais != null ? pais.getNomeLocal() : "null") +
                ", estado=" + (estado != null ? estado.getNomeLocal() : "null") +
                ", qtdCidades=" + cidades.size() +
                ", dataUltimaAtualizacao=" + dataUltimaAtualizacao +
                '}';
    }
}
