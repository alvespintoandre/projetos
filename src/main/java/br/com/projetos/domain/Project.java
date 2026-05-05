package br.com.projetos.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@NamedEntityGraph(
        name = "Project.details",
        attributeNodes = {
                @NamedAttributeNode("gerente"),
                @NamedAttributeNode("membrosAlocados")
        }
)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nome;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate previsaoTermino;

    @Column
    private LocalDate dataRealTermino;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal orcamentoTotal;

    @Column(nullable = false, length = 4000)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "gerente_id", nullable = false)
    private Member gerente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ProjectStatus status;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "project_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id")
    )
    private Set<Member> membrosAlocados = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getPrevisaoTermino() {
        return previsaoTermino;
    }

    public void setPrevisaoTermino(LocalDate previsaoTermino) {
        this.previsaoTermino = previsaoTermino;
    }

    public LocalDate getDataRealTermino() {
        return dataRealTermino;
    }

    public void setDataRealTermino(LocalDate dataRealTermino) {
        this.dataRealTermino = dataRealTermino;
    }

    public BigDecimal getOrcamentoTotal() {
        return orcamentoTotal;
    }

    public void setOrcamentoTotal(BigDecimal orcamentoTotal) {
        this.orcamentoTotal = orcamentoTotal;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Member getGerente() {
        return gerente;
    }

    public void setGerente(Member gerente) {
        this.gerente = gerente;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public Set<Member> getMembrosAlocados() {
        return membrosAlocados;
    }

    public void setMembrosAlocados(Set<Member> membrosAlocados) {
        this.membrosAlocados = membrosAlocados;
    }
}
