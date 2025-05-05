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
        if (nome.trim().length() > 100) {
            return "Tamanho do nome deve ser no máximo 100 caracteres";
        }
        return null;
    }


    public String validarEndereco(Endereco endereco) {
        if (endereco == null)
            return "Endereço deve ser informado";
        if (StringUtils.ehNuloOuBranco(endereco.getCep()))
            return "CEP deve ser informado";
        if (endereco.getCep().length() != 8)
            return "Tamanho do CEP deve ser 8 caracteres";
        if (!endereco.getCep().matches("\\d{8}"))
            return "CEP deve ter formato NNNNNNNN";
        if (StringUtils.ehNuloOuBranco(endereco.getCidade()))
            return "Cidade deve ser informada";
        if (endereco.getCidade().length() > 100)
            return "Tamanho da cidade deve ser no máximo 100 caracteres";
        if (!StringUtils.ehNuloOuBranco(endereco.getComplemento()) && endereco.getComplemento().length() > 30)
            return "Tamanho do complemento deve ser no máximo 30 caracteres";
        if (StringUtils.ehNuloOuBranco(endereco.getEstado()))
            return "Sigla do estado deve ser informada";
        if (endereco.getEstado().length() != 2)
            return "Tamanho da sigla do estado deve ser 2 caracteres";
        if (StringUtils.ehNuloOuBranco(endereco.getPais()))
            return "País deve ser informado";
        if (endereco.getPais().length() > 40)
            return "Tamanho do país deve ser no máximo 40 caracteres";
        if (StringUtils.ehNuloOuBranco(endereco.getLogradouro()))
            return "Logradouro deve ser informado";
        if (!StringUtils.ehNuloOuBranco(endereco.getNumero()) && endereco.getNumero().length() > 20)
            return "Tamanho do número deve ser no máximo 20 caracteres";
        return null;
    }



    public String validarDataCriacao(LocalDate dataCriacao) {
        if (dataCriacao == null) return "Data do nascimento deve ser informada";
        if (dataCriacao.isAfter(LocalDate.now())) return "Data da criação deve ser menor ou igual à data atual";
        return null;
    }

    public BigDecimal ajustarDebitoBonus(BigDecimal bonus, BigDecimal valorDebito) {
        if (bonus == null || valorDebito == null) {
            return BigDecimal.ZERO;
        }
        if (bonus.compareTo(valorDebito) <= 0) {
            return bonus;
        }
        return valorDebito;
    }
}
