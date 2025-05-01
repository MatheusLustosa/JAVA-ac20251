package br.edu.cs.poo.ac.seguro.testes;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.edu.cs.poo.ac.seguro.daos.ApoliceDAO;
import br.edu.cs.poo.ac.seguro.entidades.Apolice;
import br.edu.cs.poo.ac.seguro.entidades.Veiculo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TesteApoliceDAO extends TesteDAO {
    private ApoliceDAO dao = new ApoliceDAO();
    private Veiculo veiculo = new Veiculo("Jdsdasfasfa", 2005, null, null, null);

    @Override
    protected Class getClasse() {
        return Apolice.class;
    }

    @Test
    public void teste01() {
        String numero = "00000000";
        Apolice apolice = new Apolice(numero, veiculo, BigDecimal.TEN, BigDecimal.ONE, new BigDecimal("50000"), LocalDate.now());
        cadastro.incluir(apolice, numero);
        Apolice buscado = dao.buscar(numero);
        Assertions.assertNotNull(buscado);
    }

    @Test
    public void teste02() {
        String numero = "10000000";
        Apolice apolice = new Apolice(numero, veiculo, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now());
        cadastro.incluir(apolice, numero);
        Apolice buscado = dao.buscar("11000000");
        Assertions.assertNull(buscado);
    }

    @Test
    public void teste03() {
        String numero = "22000000";
        Apolice apolice = new Apolice(numero, veiculo, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now());
        cadastro.incluir(apolice, numero);
        boolean ret = dao.excluir(numero);
        Assertions.assertTrue(ret);
    }

    @Test
    public void teste04() {
        String numero = "33000000";
        Apolice apolice = new Apolice(numero, veiculo, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now());
        cadastro.incluir(apolice, numero);
        boolean ret = dao.excluir("33100000");
        Assertions.assertFalse(ret);
    }

    @Test
    public void teste05() {
        String numero = "44000000";
        Apolice apolice = new Apolice(numero, veiculo, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now());
        boolean ret = dao.incluir(apolice);
        Assertions.assertTrue(ret);
        Apolice buscado = dao.buscar(numero);
        Assertions.assertNotNull(buscado);
    }

    @Test
    public void teste06() {
        String numero = "55000000";
        Apolice apolice = new Apolice(numero, veiculo, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now());
        cadastro.incluir(apolice, numero);
        boolean ret = dao.incluir(apolice);
        Assertions.assertFalse(ret);
    }

    @Test
    public void teste07() {
        String numero = "66000000";
        Apolice apolice = new Apolice(numero, veiculo, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now());
        boolean ret = dao.alterar(apolice);
        Assertions.assertFalse(ret);
        Apolice buscado = dao.buscar(numero);
        Assertions.assertNull(buscado);
    }

    @Test
    public void teste08() {
        String numero = "77000000";
        Apolice apolice = new Apolice(numero, veiculo, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, LocalDate.now());
        cadastro.incluir(apolice, numero);
        Apolice novo = new Apolice(numero, veiculo, new BigDecimal("100"), new BigDecimal("50"), new BigDecimal("200000"), LocalDate.now());
        boolean ret = dao.alterar(novo);
        Assertions.assertTrue(ret);
    }
}
