package br.edu.cs.poo.ac.seguro.entidades;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class Veiculo implements Serializable {
    private String placa;
    private int ano;
    private SeguradoEmpresa proprietarioEmpresa;
    private SeguradoPessoa proprietarioPessoa;
    private CategoriaVeiculo categoria;

    public void setSeguradoPessoa(SeguradoPessoa sp) {
    }

    public void setSeguradoEmpresa(SeguradoEmpresa se) {
    }
}
