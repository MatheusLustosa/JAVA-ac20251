package br.edu.cs.poo.ac.seguro.entidades;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PrecoAno {
    private int ano;
    private double preco;

    public PrecoAno(int ano, double preco) {
        super();
        this.ano = ano;
        this.preco = preco;
    }
    public int getAno() {
        return ano;
    }
    public double getPreco() {
        return preco;
    }

    public BigDecimal getValor() {
        return BigDecimal.valueOf(preco).setScale(2, RoundingMode.HALF_UP);
    }
}