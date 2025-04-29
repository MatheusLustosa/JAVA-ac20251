package br.edu.cs.poo.ac.seguro.mediators;

import br.edu.cs.poo.ac.seguro.daos.SeguradoPessoaDAO;
import br.edu.cs.poo.ac.seguro.entidades.SeguradoPessoa;

public class SeguradoPessoaMediator {

    private static SeguradoPessoaMediator instancia = new SeguradoPessoaMediator();

    public static SeguradoPessoaMediator getInstancia() {
        return instancia;
    }

    private final SeguradoMediator seguradoMediator = SeguradoMediator.getInstancia();
    private final SeguradoPessoaDAO seguradoPessoaDAO = new SeguradoPessoaDAO();

    public String validarSeguradoPessoa(SeguradoPessoa seg) {
        String msg;

        msg = seguradoMediator.validarNome(seg.getNome());
        if (msg != null) return msg;

        msg = seguradoMediator.validarEndereco(seg.getEndereco());
        if (msg != null) return msg;

        msg = seguradoMediator.validarDataCriacao(seg.getDataNascimento());
        if (msg != null) return msg;

        String cpf = seg.getCpf();
        if (cpf == null || cpf.trim().length() != 11 || !ValidadorCpfCnpj.ehCpfValido(cpf)) {
            return "CPF inválido. Deve conter 11 dígitos numéricos válidos.";
        }

        if (seg.getRenda() <= 0) {
            return "Renda deve ser maior que zero.";
        }

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
