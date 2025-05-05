package br.edu.cs.poo.ac.seguro.mediators;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import br.edu.cs.poo.ac.seguro.daos.*;
import br.edu.cs.poo.ac.seguro.entidades.*;

public class ApoliceMediator {
    private static final ApoliceMediator instancia = new ApoliceMediator();

    public static ApoliceMediator getInstancia() {
        return instancia;
    }

    private SeguradoPessoaDAO daoSegPes = new SeguradoPessoaDAO();
    private SeguradoEmpresaDAO daoSegEmp = new SeguradoEmpresaDAO();
    private VeiculoDAO daoVel = new VeiculoDAO();
    private ApoliceDAO daoApo = new ApoliceDAO();
    private SinistroDAO daoSin = new SinistroDAO();

    private ApoliceMediator() {}

    public RetornoInclusaoApolice incluirApolice(DadosVeiculo dados) {
        if (dados == null) {
            return new RetornoInclusaoApolice(null, "Placa do veículo deve ser informada");
        }
        if (dados.getPlaca() == null || dados.getPlaca().trim().isEmpty()) {
            return new RetornoInclusaoApolice(null, "Placa do veículo deve ser informada");
        }

        if (dados.getCpfOuCnpj() == null || dados.getCpfOuCnpj().trim().isEmpty()) {
            return new RetornoInclusaoApolice(null, "CPF ou CNPJ deve ser informado");
        }

        boolean isCpf = dados.getCpfOuCnpj().length() == 11;
        if (isCpf && !ValidadorCpfCnpj.ehCpfValido(dados.getCpfOuCnpj())) {
            return new RetornoInclusaoApolice(null, "CPF inválido");
        }
        if (!isCpf && !ValidadorCpfCnpj.ehCnpjValido(dados.getCpfOuCnpj())) {
            return new RetornoInclusaoApolice(null, "CNPJ inválido");
        }

        if (dados.getValorMaximoSegurado() == null) {
            return new RetornoInclusaoApolice(null, "Valor máximo segurado deve ser informado");
        }

        CategoriaVeiculo categoria = CategoriaVeiculo.getPorCodigo(dados.getCodigoCategoria());
        if (categoria == null) {
            return new RetornoInclusaoApolice(null, "Categoria inválida");
        }

        BigDecimal valorMaximoPermitido = obterValorMaximoPermitido(dados.getAno(), dados.getCodigoCategoria());
        if (valorMaximoPermitido == null) {
            return new RetornoInclusaoApolice(null, "Valor máximo segurado deve ser informado");
        }

        BigDecimal minimoPermitido = valorMaximoPermitido.multiply(BigDecimal.valueOf(0.75));
        if (dados.getValorMaximoSegurado().compareTo(minimoPermitido) < 0 ||
                dados.getValorMaximoSegurado().compareTo(valorMaximoPermitido) > 0) {
            return new RetornoInclusaoApolice(null, "Valor máximo segurado deve estar entre 75% e 100% do valor do carro encontrado na categoria");
        }

        SeguradoPessoa sp = null;
        SeguradoEmpresa se = null;

        if (isCpf) {
            sp = daoSegPes.buscar(dados.getCpfOuCnpj());
            if (sp == null) return new RetornoInclusaoApolice(null, "CPF inexistente no cadastro de pessoas");
        } else {
            se = daoSegEmp.buscar(dados.getCpfOuCnpj());
            if (se == null) return new RetornoInclusaoApolice(null, "CNPJ inexistente no cadastro de empresas");
        }

        String numero = gerarNumeroApolice(dados);
        if (daoApo.buscar(numero) != null) {
            return new RetornoInclusaoApolice(null, "Apólice já existente para ano atual e veículo");
        }

        final LocalDate dataInicio = LocalDate.now();
        BigDecimal vpa = dados.getValorMaximoSegurado().multiply(BigDecimal.valueOf(0.03));
        BigDecimal vpb = vpa;
        if (se != null && se.isEhLocadoraDeVeiculos()) {
            vpb = vpa.multiply(BigDecimal.valueOf(1.2));
        }

        BigDecimal bonus = isCpf ? sp.getBonus() : se.getBonus();
        BigDecimal vpc = vpb.subtract(bonus.divide(BigDecimal.TEN, 2, RoundingMode.HALF_UP));
        BigDecimal premio = vpc.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : vpc;
        BigDecimal franquia = vpb.multiply(BigDecimal.valueOf(1.3));

        premio = premio.setScale(2, RoundingMode.HALF_UP);
        franquia = franquia.setScale(2, RoundingMode.HALF_UP);

        Veiculo veiculo = daoVel.buscar(dados.getPlaca());
        if (veiculo == null) {
            veiculo = new Veiculo(dados.getPlaca(), dados.getAno(), se, sp, categoria);
            daoVel.incluir(veiculo);
        } else {
            veiculo.setProprietarioPessoa(sp);
            veiculo.setProprietarioEmpresa(se);
            daoVel.alterar(veiculo);
        }

        Apolice apolice = new Apolice(numero, veiculo, franquia, premio, dados.getValorMaximoSegurado(), dataInicio);
        daoApo.incluir(apolice);

        List<Sinistro> sinistros = daoSin.buscarTodos();
        final String placa = veiculo.getPlaca();
        boolean teveSinistroAnterior = sinistros.stream()
                .anyMatch(s -> s.getVeiculo().getPlaca().equals(placa)
                        && s.getDataHora().getYear() == dataInicio.minusYears(1).getYear());
        boolean teveSinistroAnoAtual = sinistros.stream()
                .anyMatch(s -> s.getVeiculo().getPlaca().equals(placa)
                        && s.getDataHora().getYear() == dataInicio.getYear());
        if (isCpf) {
            if (!teveSinistroAnterior && !teveSinistroAnoAtual) {
                BigDecimal bonusAdicional = premio.multiply(BigDecimal.valueOf(0.3)).setScale(2, RoundingMode.HALF_UP);
                sp.creditarBonus(bonusAdicional);
                daoSegPes.alterar(sp);
            }
        } else {
            if (!teveSinistroAnterior) {
                BigDecimal bonusAdicional = premio.multiply(BigDecimal.valueOf(0.3)).setScale(2, RoundingMode.HALF_UP);
                se.creditarBonus(bonusAdicional);
                daoSegEmp.alterar(se);
            }
        }

        return new RetornoInclusaoApolice(numero, null);
    }

    public Apolice buscarApolice(String numero) {
        return daoApo.buscar(numero);
    }

    public String excluirApolice(String numero) {
        if (numero == null || numero.trim().isEmpty()) return "Número deve ser informado";
        Apolice apolice = daoApo.buscar(numero);
        if (apolice == null) return "Apólice inexistente";

        List<Sinistro> sinistros = daoSin.buscarTodos();
        final Veiculo veiculo = apolice.getVeiculo();
        final int ano = apolice.getDataInicioVigencia().getYear();

        boolean conflito = sinistros.stream()
                .anyMatch(s -> s.getVeiculo().equals(veiculo)
                        && s.getDataHora().getYear() == ano);

        if (conflito) {
            return "Existe sinistro cadastrado para o veículo em questão e no mesmo ano da apólice";
        }

        daoApo.excluir(numero);
        return null;
    }

    private String gerarNumeroApolice(DadosVeiculo dados) {
        int anoAtual = LocalDate.now().getYear();
        return dados.getCpfOuCnpj().length() == 11 ?
                anoAtual + "000" + dados.getCpfOuCnpj() + dados.getPlaca() :
                anoAtual + dados.getCpfOuCnpj() + dados.getPlaca();
    }

    private BigDecimal obterValorMaximoPermitido(int ano, int codigoCat) {
        CategoriaVeiculo categoria = CategoriaVeiculo.getPorCodigo(codigoCat);
        if (categoria == null) return null;
        PrecoAno precoAno = categoria.getPrecoAno(ano);
        return precoAno != null ? precoAno.getValor() : null;
    }
}