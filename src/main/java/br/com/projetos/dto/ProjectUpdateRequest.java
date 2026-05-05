package br.com.projetos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectUpdateRequest(
        @NotBlank
        @Size(max = 255)
        String nome,

        @NotNull
        LocalDate dataInicio,

        @NotNull
        LocalDate previsaoTermino,

        LocalDate dataRealTermino,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal orcamentoTotal,

        @NotBlank
        @Size(max = 4000)
        String descricao,

        @NotNull
        Long gerenteId
) {
}
