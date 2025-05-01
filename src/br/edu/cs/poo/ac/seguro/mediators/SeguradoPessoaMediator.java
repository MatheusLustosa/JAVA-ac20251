package br.edu.cs.poo.ac.seguro.mediators;

import br.edu.cs.poo.ac.seguro.daos.SeguradoPessoaDAO;
import br.edu.cs.poo.ac.seguro.entidades.SeguradoPessoa;

public class SeguradoPessoaMediator {

    private static final SeguradoPessoaMediator instancia = new SeguradoPessoaMediator();
    private final SeguradoMediator seguradoMediator = SeguradoMediator.getInstancia();
    private final SeguradoPessoaDAO seguradoPessoaDAO = new SeguradoPessoaDAO();

    private SeguradoPessoaMediator() {}

    public static SeguradoPessoaMediator getInstancia() {
        return instancia;
    }

    public String validarCpf(String cpf) {
        return ValidadorCpfCnpj.ehCpfValido(cpf);
    }

    public String validarSeguradoPessoa(SeguradoPessoa seg) {
        String msg;

        msg = seguradoMediator.validarNome(seg.getNome());
        if (msg != null) return msg;

        msg = seguradoMediator.validarEndereco(seg.getEndereco());
        if (msg != null) return msg;

        msg = seguradoMediator.validarDataCriacao(seg.getDataNascimento());
        if (msg != null) return msg;

        String cpf = seg.getCpf();
        msg = validarCpf(cpf);
        if (msg != null) return msg;

        if (seg.getRenda() <= 0) {
            return "Renda deve ser maior que zero.";
        }

        return null;
    }
    public String validarRenda(double renda) {
        if (renda < 0)
            return "Renda deve ser maior ou igual à zero";
        return null;
    }

    public String incluirSeguradoPessoa(SeguradoPessoa seg) {
        String msg = validarSeguradoPessoa(seg);
        if (msg != null) return msg;

        boolean sucesso = seguradoPessoaDAO.incluir(seg);
        if (!sucesso) {
            return "Segurado pessoa já existente.";
        }
        return null;
    }

    public String alterarSeguradoPessoa(SeguradoPessoa seg) {
        String msg = validarSeguradoPessoa(seg);
        if (msg != null) return msg;

        boolean sucesso = seguradoPessoaDAO.alterar(seg);
        if (!sucesso) {
            return "Segurado pessoa não encontrado.";
        }
        return null;
    }

    public String excluirSeguradoPessoa(String cpf) {
        boolean sucesso = seguradoPessoaDAO.excluir(cpf);
        if (!sucesso) {
            return "Segurado pessoa não encontrado.";
        }
        return null;
    }

    public SeguradoPessoa buscarSeguradoPessoa(String cpf) {
        return seguradoPessoaDAO.buscar(cpf);
    }
}
