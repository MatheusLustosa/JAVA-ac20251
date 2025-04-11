package br.edu.cs.poo.ac.seguro.testes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import br.edu.cs.poo.ac.seguro.entidades.Sinistro;
import br.edu.cs.poo.ac.seguro.entidades.TipoSinistro;
import br.edu.cs.poo.ac.seguro.entidades.Veiculo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import br.edu.cs.poo.ac.seguro.daos.SinistroDAO;

public class TesteSinistroDAO extends TesteDAO {
    private SinistroDAO dao = new SinistroDAO();
    private Veiculo veiculo = new Veiculo("Jdsdasfasfa",2005,null,null,null);
    TipoSinistro tiposinistro = TipoSinistro.COLISAO;
    TipoSinistro tipo = TipoSinistro.getTipoSinistro(tiposinistro.getCodigo());
    protected Class getClasse() {
        return Sinistro.class;
    }

    @Test
    public void teste01() {
        String cnpj = "00000000";
        cadastro.incluir(new Sinistro(veiculo, LocalDateTime.now(), LocalDateTime.now(), "asfda", new BigDecimal("10"),
                 tipo), cnpj);
        Sinistro seg = dao.buscar(cnpj);
        Assertions.assertNotNull(seg);
    }
    @Test
    public void teste02() {
        String cnpj = "10000000";
        cadastro.incluir(new Sinistro(veiculo, LocalDateTime.now(), LocalDateTime.now(), "asfda", new BigDecimal("10"),
                tipo), cnpj);
        Sinistro seg = dao.buscar("11000000");
        Assertions.assertNull(seg);
    }
    @Test
    public void teste03() {
        String cnpj = "22000000";
        cadastro.incluir(new Sinistro(veiculo, LocalDateTime.now(), LocalDateTime.now(), "asfda", new BigDecimal("10"),
                tipo), cnpj);
        boolean ret = dao.excluir(cnpj);
        Assertions.assertTrue(ret);
    }
    @Test
    public void teste04() {
        String cnpj = "33000000";
        cadastro.incluir(new Sinistro(veiculo, LocalDateTime.now(), LocalDateTime.now(), "asfda", new BigDecimal("10"),
                tipo), cnpj);
        boolean ret = dao.excluir("33100000");
        Assertions.assertFalse(ret);
    }
    @Test
    public void teste05() {
        String cnpj = "44000000";
        boolean ret = dao.incluir(new Sinistro(veiculo, LocalDateTime.now(), LocalDateTime.now(), "asfda", new BigDecimal("10"),
                tipo));
        Assertions.assertTrue(ret);
        Sinistro seg = dao.buscar(cnpj);
        Assertions.assertNotNull(seg);
    }

    @Test
    public void teste06() {
        String cnpj = "55000000";
        Sinistro seg = new Sinistro(veiculo, LocalDateTime.now(), LocalDateTime.now(), "asfda", new BigDecimal("10"),
        tipo);
        cadastro.incluir(seg, cnpj);
        boolean ret = dao.incluir(seg);
        Assertions.assertFalse(ret);
    }
    @Test
    public void teste07() {
        String cnpj = "66000000";
        boolean ret = dao.alterar(new Sinistro(veiculo, LocalDateTime.now(), LocalDateTime.now(), "asfda", new BigDecimal("10"),
                tipo));
        Assertions.assertFalse(ret);
        Sinistro seg = dao.buscar(cnpj);
        Assertions.assertNull(seg);
    }

    @Test
    public void teste08() {
        String cnpj = "77000000";
        Sinistro seg = new Sinistro(veiculo, LocalDateTime.now(), LocalDateTime.now(), "asfda", new BigDecimal("10"),
                tipo);
        cadastro.incluir(seg, cnpj);
        seg = new Sinistro(veiculo, LocalDateTime.now(), LocalDateTime.now(), "asfda", new BigDecimal("10"),
                tipo);
        boolean ret = dao.alterar(seg);
        Assertions.assertTrue(ret);
    }
}