package br.edu.cs.poo.ac.seguro.entidades;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class Apolice implements Serializable {
    private String numero;
    private Veiculo veiculo;
    private BigDecimal valorFranquia;
    private BigDecimal valorPremio;
    private BigDecimal valorMaximoSegurado;
    private LocalDate dataInicioVigencia;

    public Apolice(String numero, Veiculo veiculo, BigDecimal valorFranquia,
                   BigDecimal valorPremio, BigDecimal valorMaximoSegurado, LocalDate dataInicioVigencia) {
        this.numero = numero;
        this.veiculo = veiculo;
        this.valorFranquia = valorFranquia;
        this.valorPremio = valorPremio;
        this.valorMaximoSegurado = valorMaximoSegurado;
        this.dataInicioVigencia = dataInicioVigencia;
    }

    public LocalDate getDataInicioVigencia() {
        return this.dataInicioVigencia;
    }

    public void setDataInicioVigencia(LocalDate dataInicioVigencia) {
        this.dataInicioVigencia = dataInicioVigencia;
    }


}