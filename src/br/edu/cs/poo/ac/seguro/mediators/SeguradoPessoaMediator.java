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
        if (StringUtils.ehNuloOuBranco(cpf))
            return "CPF deve ser informado";
        if (cpf.length() != 11)
            return "CPF deve ter 11 caracteres";
        if (!ValidadorCpfCnpj.ehCpfValido(cpf))
            return "CPF com d�gito inv�lido";

        return null;
    }

    public String validarSeguradoPessoa(SeguradoPessoa seg) {
        if (seg == null) return "Segurado não pode ser nulo";
        if (StringUtils.ehNuloOuBranco(seg.getNome()))
            return "Nome deve ser informado";
        if (seg.getEndereco() == null)
            return "Endere�o deve ser informado";
        if (seg.getDataNascimento() == null)
            return "Data do nascimento deve ser informada";
        String erroCpf = validarCpf(seg.getCpf());
        if (erroCpf != null)
            return erroCpf;
        String erroRenda = validarRenda(seg.getRenda());
        if (erroRenda != null)
            return erroRenda;
        return null;
    }
    public String validarRenda(double renda) {
        if (renda < 0)
            return "Renda deve ser maior ou igual � zero";
        return null;
    }

    public String incluirSeguradoPessoa(SeguradoPessoa seg) {
        String msg = validarSeguradoPessoa(seg);
        if (msg != null) return msg;

        boolean sucesso = seguradoPessoaDAO.incluir(seg);
        if (!sucesso) {
            return "CPF do segurado pessoa j� existente";
        }
        return null;
    }

    public String alterarSeguradoPessoa(SeguradoPessoa seg) {
        String msg = validarSeguradoPessoa(seg);
        if (msg != null) return msg;

        boolean sucesso = seguradoPessoaDAO.alterar(seg);
        if (!sucesso) {
            return "CPF do segurado pessoa n�o existente";
        }
        return null;
    }

    public String excluirSeguradoPessoa(String cpf) {
        boolean sucesso = seguradoPessoaDAO.excluir(cpf);
        if (!sucesso) {
            return "CPF do segurado pessoa n�o existente";
        }
        return null;
    }

    public SeguradoPessoa buscarSeguradoPessoa(String cpf) {
        return seguradoPessoaDAO.buscar(cpf);
    }
}
