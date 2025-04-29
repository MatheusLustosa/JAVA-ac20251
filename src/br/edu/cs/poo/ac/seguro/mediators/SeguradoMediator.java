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
            return "Nome inválido.";
        }
        return null;
    }

    public String validarEndereco(Endereco endereco) {
        if (endereco == null) return "Endereço não pode ser nulo.";
        if (StringUtils.ehNuloOuBranco(endereco.getLogradouro())) {
            return "Logradouro inválido.";
        }
        if (StringUtils.ehNuloOuBranco(endereco.getNumero())) {
            return "Número do endereço inválido.";
        }
        if (StringUtils.ehNuloOuBranco(endereco.getCep())) {
            return "CEP inválido.";
        }
        if (StringUtils.ehNuloOuBranco(endereco.getCidade())) {
            return "Cidade inválida.";
        }
        if (StringUtils.ehNuloOuBranco(endereco.getEstado())) {
            return "Estado inválido.";
        }
        if (StringUtils.ehNuloOuBranco(endereco.getPais())) {
            return "País inválido.";
        }
        return null;
    }

    public String validarDataCriacao(LocalDate dataCriacao) {
        if (dataCriacao == null) return "Data de criação não pode ser nula.";
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
