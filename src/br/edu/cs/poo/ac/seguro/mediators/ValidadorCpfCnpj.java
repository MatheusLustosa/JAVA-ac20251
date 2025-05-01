package br.edu.cs.poo.ac.seguro.mediators;

public class ValidadorCpfCnpj {

    public static String ehCpfValido(String cpf) {
        if (cpf == null || cpf.length() != 11 || !cpf.matches("\\d{11}")) {
            return "CPF inválido. Deve conter 11 dígitos numéricos.";
        }
        return null;
    }

    public static boolean ehCnpjValido(String cnpj) {
        if (cnpj == null || cnpj.length() != 14 || !cnpj.matches("\\d{14}")) {
            return false; // CNPJ inválido
        }
        return true; // CNPJ válido
    }
}
