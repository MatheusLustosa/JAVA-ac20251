package br.edu.cs.poo.ac.seguro.mediators;

import br.edu.cs.poo.ac.seguro.daos.SeguradoEmpresaDAO;
import br.edu.cs.poo.ac.seguro.entidades.SeguradoEmpresa;

public class SeguradoEmpresaMediator {

    private static SeguradoEmpresaMediator instancia = new SeguradoEmpresaMediator();

    public static SeguradoEmpresaMediator getInstancia() {
        return instancia;
    }

    private final SeguradoMediator seguradoMediator = SeguradoMediator.getInstancia();
    private final SeguradoEmpresaDAO seguradoEmpresaDAO = new SeguradoEmpresaDAO();

    public String validarSeguradoEmpresa(SeguradoEmpresa seg) {
        String msg;

        msg = seguradoMediator.validarNome(seg.getNome());
        if (msg != null) return msg;

        msg = seguradoMediator.validarEndereco(seg.getEndereco());
        if (msg != null) return msg;

        msg = seguradoMediator.validarDataCriacao(seg.getDataAbertura());
        if (msg != null) return msg;

        String cnpj = seg.getCnpj();
        if (cnpj == null || cnpj.trim().length() != 14 || !ValidadorCpfCnpj.ehCnpjValido(cnpj)) {
            return "CNPJ inválido. Deve conter 14 dígitos numéricos válidos.";
        }

        if (seg.getFaturamento() <= 0) {
            return "Faturamento deve ser maior que zero.";
        }

        return null;
    }

    public String incluirSeguradoEmpresa(SeguradoEmpresa seg) {
        String msg = validarSeguradoEmpresa(seg);
        if (msg != null) return msg;

        boolean sucesso = seguradoEmpresaDAO.incluir(seg);
        if (!sucesso) {
            return "Segurado empresa já existente.";
        }
        return null;
    }

    public String alterarSeguradoEmpresa(SeguradoEmpresa seg) {
        String msg = validarSeguradoEmpresa(seg);
        if (msg != null) return msg;

        boolean sucesso = seguradoEmpresaDAO.alterar(seg);
        if (!sucesso) {
            return "Segurado empresa não encontrado.";
        }
        return null;
    }

    public String excluirSeguradoEmpresa(String cnpj) {
        boolean sucesso = seguradoEmpresaDAO.excluir(cnpj);
        if (!sucesso) {
            return "Segurado empresa não encontrado.";
        }
        return null;
    }

    public SeguradoEmpresa buscarSeguradoEmpresa(String cnpj) {
        return seguradoEmpresaDAO.buscar(cnpj);
    }
}
