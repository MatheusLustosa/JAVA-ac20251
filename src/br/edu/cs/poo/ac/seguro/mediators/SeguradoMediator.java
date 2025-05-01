package br.edu.cs.poo.ac.seguro.mediators;

import java.math.BigDecimal;
import java.time.LocalDate;
import br.edu.cs.poo.ac.seguro.entidades.Endereco;
import lombok.Getter;

public class SeguradoMediator {

    private static final SeguradoMediator INSTANCIA = new SeguradoMediator();

    public static SeguradoMediator getInstancia() {
        return INSTANCIA;
    }

    public String validarNome(String nome) {
        if (StringUtils.ehNuloOuBranco(nome)) {
            return "Nome deve ser informado";
        }
        return null;
    }

    public String validarEndereco(Endereco endereco) {
        if (endereco == null) return "Endereço deve ser informado";
        if (StringUtils.ehNuloOuBranco(endereco.getLogradouro())) {
            return "Logradouro deve ser informado";
        }
        if (StringUtils.ehNuloOuBranco(endereco.getNumero())) {
            return "Sigla do estado deve ser informada";
        }
        if (StringUtils.ehNuloOuBranco(endereco.getCep())) {
            return "CEP deve ser informado";
        }
        if (StringUtils.ehNuloOuBranco(endereco.getCidade())) {
            return "Cidade deve ser informada";
        }
        if (StringUtils.ehNuloOuBranco(endereco.getEstado())) {
            return "Sigla do estado deve ser informada";
        }
        if (StringUtils.ehNuloOuBranco(endereco.getPais())) {
            return "País inválido.";
        }
        return null;
    }

    public String validarDataCriacao(LocalDate dataCriacao) {
        if (dataCriacao == null) return "Data do nascimento deve ser informada";
        if (dataCriacao.isAfter(LocalDate.now())) return "Data de criação não pode ser futura.";
        return null;
    }

    public BigDecimal ajustarDebitoBonus(BigDecimal bonus, BigDecimal valorDebito) {
        if (bonus == null) bonus = BigDecimal.ZERO;
        if (valorDebito == null) valorDebito = BigDecimal.ZERO;
        BigDecimal result = valorDebito.subtract(bonus);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }
}
