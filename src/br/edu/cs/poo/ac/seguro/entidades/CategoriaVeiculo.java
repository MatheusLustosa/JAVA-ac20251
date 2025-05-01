package br.edu.cs.poo.ac.seguro.entidades;

import static br.edu.cs.poo.ac.seguro.entidades.PrecosAnosCategoria.*;

public enum CategoriaVeiculo {

    BASICO(1, "Veículo econômico", PA_BASICO),
    INTERMEDIARIO(2, "Veículo de categoria média", PA_INTERMEDIARIO),
    LUXO(3, "Veículo de luxo", PA_LUXO),
    SUPER_LUXO(4, "Veículo exclusivo", PA_SUPER_LUXO),
    ESPORTIVO(5, "Veículo esportivo", PA_ESPORTIVO);

    private final int codigo;
    private final String nome;
    private final PrecoAno[] precosAnos;

    CategoriaVeiculo(int codigo, String nome, PrecoAno[] precosAnos) {
        this.codigo = codigo;
        this.nome = nome;
        this.precosAnos = precosAnos;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public PrecoAno[] getPrecosAnos() {
        return precosAnos;
    }

    public static CategoriaVeiculo getPorCodigo(int codigo) {
        for (CategoriaVeiculo categoria : values()) {
            if (categoria.getCodigo() == codigo) return categoria;
        }
        return null;
    }

    public PrecoAno getPrecoAno(int ano) {
        for (PrecoAno pa : precosAnos) {
            if (pa.getAno() == ano) return pa;
        }
        return null;
    }
}
