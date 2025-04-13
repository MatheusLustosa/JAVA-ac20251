package br.edu.cs.poo.ac.seguro.mediators;

import br.edu.cs.poo.ac.seguro.daos.SeguradoPessoaDAO;
import br.edu.cs.poo.ac.seguro.entidades.SeguradoPessoa;

public class SeguradoPessoaMediator {
    SeguradoMediator seguradoMediator= new SeguradoMediator()


    public static SeguradoPessoaMediator getInstancia() {
    }

    public String validarCpf(String cpf) {
        return null;
    }
    public String validarRenda(double renda) {
        return null;
    }
    public String incluirSeguradoPessoa(SeguradoPessoa seg) {
        return msg;
    }
    public String alterarSeguradoPessoa(SeguradoPessoa seg) {
        return null;
    }
    public String excluirSeguradoPessoa(String cpf) {
        return null;
    }
    public SeguradoPessoa buscarSeguradoPessoa(String cpf) {
        return null;
    }
    public String validarSeguradoPessoa(SeguradoPessoa seg) {
        return null;
    }
}
