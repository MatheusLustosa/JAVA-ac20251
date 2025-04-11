package br.edu.cs.poo.ac.seguro.entidades;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SeguradoPessoa extends Segurado {
    private String cpf;
    private double renda;

    public String getCpf() {
        return cpf;
    }

    public SeguradoPessoa(String nome, Endereco endereco, LocalDate dataNascimento, BigDecimal bonus, String cpf, double renda) {
        super(nome, endereco, dataNascimento, bonus);
        this.cpf = cpf;
        this.renda = renda;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public double getRenda() {
        return renda;
    }

    public void setRenda(double renda) {
        this.renda = renda;
    }
    public LocalDate getDataNascimento(){
        return getDataCriacao();
    }
    public void setDataNascimento(LocalDate dataAbertura){
        setDataCriacao(dataAbertura);
    }
}
