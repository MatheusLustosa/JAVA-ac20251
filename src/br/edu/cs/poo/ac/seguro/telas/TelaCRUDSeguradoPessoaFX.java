package br.edu.cs.poo.ac.seguro.testes;

import br.edu.cs.poo.ac.seguro.daos.SeguradoPessoaDAO;
import br.edu.cs.poo.ac.seguro.entidades.Endereco;
import br.edu.cs.poo.ac.seguro.entidades.SeguradoPessoa;
import br.edu.cs.poo.ac.seguro.mediators.StringUtils;
import br.edu.cs.poo.ac.seguro.mediators.ValidadorCpfCnpj;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.control.TextFormatter.Change;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class TelaCRUDSeguradoPessoaFX extends Application {

    private SeguradoPessoaDAO seguradoPessoaDAO;

    private TextField txtCpf;
    private TextField txtNome;
    private TextField txtDataNascimento;
    private TextField txtBonus;
    private TextField txtRenda;

    private TextField txtLogradouro;
    private TextField txtNumero;
    private TextField txtComplemento;

    private TextField txtCidade;
    private TextField txtEstado;
    private TextField txtCep;
    private TextField txtPais;

    private Button btnBuscar;
    private Button btnIncluir;
    private Button btnAlterar;
    private Button btnExcluir;
    private Button btnLimpar;

    private enum EstadoTela {
        INICIAL, BUSCA_SUCESSO, INCLUSAO_NOVO
    }
    private EstadoTela estadoAtual;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DecimalFormat CURRENCY_FORMATTER_BR = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("pt", "BR"));

    static {
        CURRENCY_FORMATTER_BR.applyPattern("#,##0.00");
        CURRENCY_FORMATTER_BR.setParseBigDecimal(true);
    }

    public TelaCRUDSeguradoPessoaFX() {
        this.seguradoPessoaDAO = new SeguradoPessoaDAO();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CRUD de Segurado Pessoa");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        initComponents();
        setupLayout(grid);
        addListeners();
        setupTabOrder();
        setEstado(EstadoTela.INICIAL);

        Scene scene = new Scene(grid, 600, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initComponents() {
        txtCpf = new TextField();
        txtCpf.setPromptText("Ex: 123.456.789-00");
        txtCpf.setMaxWidth(150);
        setupCpfMask(txtCpf);

        txtNome = new TextField();
        txtNome.setPromptText("Nome Completo");
        txtNome.setMaxWidth(250);

        txtDataNascimento = new TextField();
        txtDataNascimento.setPromptText("dd/MM/yyyy");
        txtDataNascimento.setMaxWidth(100);
        setupDateMask(txtDataNascimento);

        txtBonus = new TextField();
        txtBonus.setPromptText("Ex: 100,00");
        txtBonus.setMaxWidth(100);
        setupCurrencyMask(txtBonus);

        txtRenda = new TextField();
        txtRenda.setPromptText("Ex: 5.000,00");
        txtRenda.setMaxWidth(100);
        setupCurrencyMask(txtRenda);

        txtLogradouro = new TextField();
        txtLogradouro.setPromptText("Rua, Av., Alameda...");
        txtLogradouro.setMaxWidth(250);

        txtNumero = new TextField();
        txtNumero.setPromptText("Número");
        txtNumero.setMaxWidth(80);

        txtComplemento = new TextField();
        txtComplemento.setPromptText("Apto, Bloco, Casa");
        txtComplemento.setMaxWidth(150);

        txtCidade = new TextField();
        txtCidade.setPromptText("Cidade");
        txtCidade.setMaxWidth(150);

        txtEstado = new TextField();
        txtEstado.setPromptText("UF (Ex: PE)");
        txtEstado.setMaxWidth(80);

        txtCep = new TextField();
        txtCep.setPromptText("Ex: 12345-678");
        txtCep.setMaxWidth(100);
        setupCepMask(txtCep);

        txtPais = new TextField();
        txtPais.setPromptText("País");
        txtPais.setMaxWidth(150);

        btnBuscar = new Button("Buscar");
        btnIncluir = new Button("Incluir");
        btnAlterar = new Button("Alterar");
        btnExcluir = new Button("Excluir");
        btnLimpar = new Button("Limpar");
    }

    private void setupLayout(GridPane grid) {
        int row = 0;
        grid.add(new Label("CPF:"), 0, row);
        grid.add(txtCpf, 1, row);
        grid.add(btnBuscar, 2, row);
        row++;

        grid.add(new Label("Nome:"), 0, row);
        grid.add(txtNome, 1, row, 2, 1);
        row++;

        grid.add(new Label("Data Nasc.:"), 0, row);
        grid.add(txtDataNascimento, 1, row);
        row++;

        grid.add(new Label("Bônus:"), 0, row);
        grid.add(txtBonus, 1, row);
        row++;

        grid.add(new Label("Renda:"), 0, row);
        grid.add(txtRenda, 1, row);
        row++;

        Label lblEndereco = new Label("Endereço:");
        lblEndereco.setStyle("-fx-font-weight: bold;");
        grid.add(lblEndereco, 0, row, 3, 1);
        row++;

        grid.add(new Label("Logradouro:"), 0, row);
        grid.add(txtLogradouro, 1, row, 2, 1);
        row++;

        grid.add(new Label("Número:"), 0, row);
        grid.add(txtNumero, 1, row);
        row++;

        grid.add(new Label("Complemento:"), 0, row);
        grid.add(txtComplemento, 1, row, 2, 1);
        row++;

        grid.add(new Label("Cidade:"), 0, row);
        grid.add(txtCidade, 1, row);
        row++;

        grid.add(new Label("Estado (UF):"), 0, row);
        grid.add(txtEstado, 1, row);
        row++;

        grid.add(new Label("CEP:"), 0, row);
        grid.add(txtCep, 1, row);
        row++;

        grid.add(new Label("País:"), 0, row);
        grid.add(txtPais, 1, row);
        row++;

        HBox hbButtons = new HBox(10);
        hbButtons.setAlignment(Pos.BOTTOM_RIGHT);
        hbButtons.getChildren().addAll(btnIncluir, btnAlterar, btnExcluir, btnLimpar);
        grid.add(hbButtons, 1, row, 2, 1);
    }

    private void addListeners() {
        btnBuscar.setOnAction(e -> buscarSegurado());
        btnIncluir.setOnAction(e -> incluirSegurado());
        btnAlterar.setOnAction(e -> alterarSegurado());
        btnExcluir.setOnAction(e -> excluirSegurado());
        btnLimpar.setOnAction(e -> limparCampos());

        txtCpf.setOnKeyPressed(e -> txtCpf.setStyle(""));
        txtNome.setOnKeyPressed(e -> txtNome.setStyle(""));
        txtDataNascimento.setOnKeyPressed(e -> txtDataNascimento.setStyle(""));
        txtBonus.setOnKeyPressed(e -> txtBonus.setStyle(""));
        txtRenda.setOnKeyPressed(e -> txtRenda.setStyle(""));
        txtLogradouro.setOnKeyPressed(e -> txtLogradouro.setStyle(""));
        txtNumero.setOnKeyPressed(e -> txtNumero.setStyle(""));
        txtComplemento.setOnKeyPressed(e -> txtComplemento.setStyle(""));
        txtCidade.setOnKeyPressed(e -> txtCidade.setStyle(""));
        txtEstado.setOnKeyPressed(e -> txtEstado.setStyle(""));
        txtCep.setOnKeyPressed(e -> txtCep.setStyle(""));
        txtPais.setOnKeyPressed(e -> txtPais.setStyle(""));
    }

    private void setupTabOrder() {
        txtCpf.setFocusTraversable(true);
        txtNome.setFocusTraversable(true);
        txtDataNascimento.setFocusTraversable(true);
        txtBonus.setFocusTraversable(true);
        txtRenda.setFocusTraversable(true);
        txtLogradouro.setFocusTraversable(true);
        txtNumero.setFocusTraversable(true);
        txtComplemento.setFocusTraversable(true);

        txtCidade.setFocusTraversable(true);
        txtEstado.setFocusTraversable(true);
        txtCep.setFocusTraversable(true);
        txtPais.setFocusTraversable(true);
        btnBuscar.setFocusTraversable(true);
        btnIncluir.setFocusTraversable(true);
        btnAlterar.setFocusTraversable(true);
        btnExcluir.setFocusTraversable(true);
        btnLimpar.setFocusTraversable(true);
    }

    private void setEstado(EstadoTela estado) {
        this.estadoAtual = estado;
        boolean cpfEditavel = false;
        boolean camposEditaveis = false;
        boolean btnBuscarHabilitado = false;
        boolean btnIncluirHabilitado = false;
        boolean btnAlterarHabilitado = false;
        boolean btnExcluirHabilitado = false;
        boolean btnLimparHabilitado = true;

        switch (estado) {
            case INICIAL:
                cpfEditavel = true;
                btnBuscarHabilitado = true;
                break;
            case BUSCA_SUCESSO:
                cpfEditavel = false;
                camposEditaveis = true;
                btnAlterarHabilitado = true;
                btnExcluirHabilitado = true;
                break;
            case INCLUSAO_NOVO:
                cpfEditavel = false;
                camposEditaveis = true;
                btnIncluirHabilitado = true;
                break;
        }

        txtCpf.setEditable(cpfEditavel);
        txtNome.setEditable(camposEditaveis);
        txtDataNascimento.setEditable(camposEditaveis);
        txtBonus.setEditable(camposEditaveis);
        txtRenda.setEditable(camposEditaveis);
        txtLogradouro.setEditable(camposEditaveis);
        txtNumero.setEditable(camposEditaveis);
        txtComplemento.setEditable(camposEditaveis);

        txtCidade.setEditable(camposEditaveis);
        txtEstado.setEditable(camposEditaveis);
        txtCep.setEditable(camposEditaveis);
        txtPais.setEditable(camposEditaveis);

        btnBuscar.setDisable(!btnBuscarHabilitado);
        btnIncluir.setDisable(!btnIncluirHabilitado);
        btnAlterar.setDisable(!btnAlterarHabilitado);
        btnExcluir.setDisable(!btnExcluirHabilitado);
        btnLimpar.setDisable(!btnLimparHabilitado);
    }

    private void setupCpfMask(TextField textField) {
        textField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            String cleanedText = newText.replaceAll("\\D", "");

            if (cleanedText.length() > 11) {
                return null;
            }

            StringBuilder formattedText = new StringBuilder();
            for (int i = 0; i < cleanedText.length(); i++) {
                formattedText.append(cleanedText.charAt(i));
                if (i == 2 || i == 5) {
                    if (cleanedText.length() > i + 1) formattedText.append(".");
                } else if (i == 8) {
                    if (cleanedText.length() > i + 1) formattedText.append("-");
                }
            }

            change.setText(formattedText.toString());
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(formattedText.length());
            change.setAnchor(formattedText.length());

            return change;
        }));

        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String text = textField.getText().trim();
                String cleanCpf = text.replaceAll("\\D", "");
                if (!cleanCpf.isEmpty()) {
                    if (ValidadorCpfCnpj.ehCpfValido(cleanCpf)) {
                        textField.setStyle("");
                        if (cleanCpf.length() == 11) {
                            textField.setText(formatCpf(cleanCpf));
                        }
                    } else {
                        textField.setStyle("-fx-border-color: red;");
                        showAlert(Alert.AlertType.ERROR, "Erro de Validação", "CPF inválido.");
                    }
                } else {
                    textField.setStyle("");
                }
            }
        });
    }

    private String formatCpf(String cpf) {
        if (cpf == null || cpf.replaceAll("\\D", "").length() != 11) {
            return cpf;
        }
        String cleanCpf = cpf.replaceAll("\\D", "");
        return cleanCpf.substring(0, 3) + "." +
                cleanCpf.substring(3, 6) + "." +
                cleanCpf.substring(6, 9) + "-" +
                cleanCpf.substring(9, 11);
    }

    private void setupDateMask(TextField textField) {
        final String format = "dd/MM/yyyy";
        Pattern pattern = Pattern.compile("[0-9/]*");
        UnaryOperator<Change> filter = c -> {
            if (pattern.matcher(c.getControlNewText()).matches()) {
                if (c.getControlNewText().length() > 10) return null;
                return c;
            } else {
                return null;
            }
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        textField.setTextFormatter(textFormatter);

        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String text = textField.getText().trim();
                if (!text.isEmpty()) {
                    try {
                        LocalDate.parse(text, DATE_FORMATTER);
                        textField.setStyle("");
                    } catch (DateTimeParseException e) {
                        textField.setStyle("-fx-border-color: red;");
                        showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Data inválida. Use o formato " + format);
                    }
                } else {
                    textField.setStyle("");
                }
            }
        });
    }

    private void setupCurrencyMask(TextField textField) {
        Pattern pattern = Pattern.compile("[0-9.,]*");
        UnaryOperator<Change> filter = c -> {
            String newText = c.getControlNewText();
            if (newText.matches("\\d*([.]\\d{3})*([,]\\d{0,2})?")) {
                return c;
            } else {
                return null;
            }
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        textField.setTextFormatter(textFormatter);

        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String text = textField.getText().trim();
                if (!text.isEmpty()) {
                    try {
                        Number parsedNumber = CURRENCY_FORMATTER_BR.parse(text);
                        BigDecimal value = new BigDecimal(parsedNumber.doubleValue()).setScale(2, RoundingMode.HALF_UP);
                        textField.setText(CURRENCY_FORMATTER_BR.format(value));
                        textField.setStyle("");
                    } catch (ParseException | NumberFormatException e) {
                        textField.setStyle("-fx-border-color: red;");
                        showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Valor inválido. Por favor, use o formato brasileiro (ex: 1.234,56).");
                        return;
                    }
                } else {
                    textField.setStyle("");
                }
            }
        });
    }

    private void setupCepMask(TextField textField) {
        textField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            String cleanedText = newText.replaceAll("\\D", "");

            if (cleanedText.length() > 8) {
                return null;
            }

            StringBuilder formattedText = new StringBuilder();
            for (int i = 0; i < cleanedText.length(); i++) {
                formattedText.append(cleanedText.charAt(i));
                if (i == 4 && cleanedText.length() > 5) {
                    formattedText.append("-");
                }
            }

            change.setText(formattedText.toString());
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(formattedText.length());
            change.setAnchor(formattedText.length());

            return change;
        }));

        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                String rawCep = textField.getText().trim();
                String cleanCep = rawCep.replaceAll("\\D", "");
                if (!cleanCep.isEmpty()) {
                    if (cleanCep.length() == 8) {
                        textField.setText(formatCep(cleanCep));
                        textField.setStyle("");
                    } else {
                        textField.setStyle("-fx-border-color: red;");
                        showAlert(Alert.AlertType.ERROR, "Erro de Validação", "CEP deve ter 8 dígitos.");
                    }
                } else {
                    textField.setStyle("");
                }
            }
        });
    }

    private String formatCep(String cep) {
        if (cep == null || cep.replaceAll("\\D", "").length() != 8) {
            return cep;
        }
        String cleanCep = cep.replaceAll("\\D", "");
        return cleanCep.substring(0, 5) + "-" + cleanCep.substring(5, 8);
    }

    private String cleanCep(String cep) {
        return cep != null ? cep.replaceAll("\\D", "") : null;
    }

    private void buscarSegurado() {
        String cpf = txtCpf.getText().trim();

        if (StringUtils.ehNuloOuBranco(cpf)) {
            showAlert(Alert.AlertType.WARNING, "Busca", "CPF deve ser informado para a busca.");
            txtCpf.setStyle("-fx-border-color: orange;");
            setEstado(EstadoTela.INICIAL);
            return;
        }

        String cleanCpf = cpf.replaceAll("\\D", "");
        if (!ValidadorCpfCnpj.ehCpfValido(cleanCpf)) {
            showAlert(Alert.AlertType.ERROR, "Busca", "CPF inválido.");
            txtCpf.setStyle("-fx-border-color: red;");
            setEstado(EstadoTela.INICIAL);
            return;
        }

        SeguradoPessoa segurado = seguradoPessoaDAO.buscar(cleanCpf);

        if (segurado != null) {
            preencherCampos(segurado);
            setEstado(EstadoTela.BUSCA_SUCESSO);
            showAlert(Alert.AlertType.INFORMATION, "Busca", "Segurado Pessoa encontrado!");
        } else {
            limparCamposComCPF();
            txtCpf.setText(formatCpf(cleanCpf));
            setEstado(EstadoTela.INCLUSAO_NOVO);
            showAlert(Alert.AlertType.INFORMATION, "Busca", "Segurado Pessoa não encontrado. Você pode incluí-lo.");
            txtNome.requestFocus();
        }
    }

    private void incluirSegurado() {
        String cpf = txtCpf.getText().trim().replaceAll("\\D", "");
        if (validarCamposComuns() && validarCamposEndereco() && validarCpfParaInclusao(cpf)) {
            try {
                Endereco endereco = criarObjetoEndereco();
                LocalDate dataNascimento = LocalDate.parse(txtDataNascimento.getText().trim(), DATE_FORMATTER);

                BigDecimal bonus = (BigDecimal) CURRENCY_FORMATTER_BR.parse(txtBonus.getText().trim());
                BigDecimal rendaBigDecimal = (BigDecimal) CURRENCY_FORMATTER_BR.parse(txtRenda.getText().trim());
                double renda = rendaBigDecimal.doubleValue();

                SeguradoPessoa novoSegurado = new SeguradoPessoa(
                        txtNome.getText().trim(),
                        endereco,
                        dataNascimento,
                        bonus,
                        cpf,
                        renda
                );

                if (seguradoPessoaDAO.incluir(novoSegurado)) {
                    showAlert(Alert.AlertType.INFORMATION, "Inclusão", "Segurado Pessoa incluído com sucesso!");
                    limparCampos();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Inclusão", "Erro ao incluir segurado Pessoa (CPF já existe ou erro de persistência).");
                }
            } catch (ParseException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Verifique os formatos de Data de Nascimento, Bônus e Renda. Use dd/MM/yyyy para data e formato brasileiro para valores monetários (ex: 1.234,56).");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro inesperado: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void alterarSegurado() {
        String cpf = txtCpf.getText().trim().replaceAll("\\D", "");
        if (validarCamposComuns() && validarCamposEndereco()) {
            try {
                Endereco endereco = criarObjetoEndereco();
                LocalDate dataNascimento = LocalDate.parse(txtDataNascimento.getText().trim(), DATE_FORMATTER);

                BigDecimal bonus = (BigDecimal) CURRENCY_FORMATTER_BR.parse(txtBonus.getText().trim());
                BigDecimal rendaBigDecimal = (BigDecimal) CURRENCY_FORMATTER_BR.parse(txtRenda.getText().trim());
                double renda = rendaBigDecimal.doubleValue();

                SeguradoPessoa seguradoAlterado = new SeguradoPessoa(
                        txtNome.getText().trim(),
                        endereco,
                        dataNascimento,
                        bonus,
                        cpf,
                        renda
                );

                if (seguradoPessoaDAO.alterar(seguradoAlterado)) {
                    showAlert(Alert.AlertType.INFORMATION, "Alteração", "Segurado Pessoa alterado com sucesso!");
                    limparCampos();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Alteração", "Erro ao alterar segurado Pessoa (CPF não encontrado ou erro de persistência).");
                }
            } catch (ParseException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Verifique os formatos de Data de Nascimento, Bônus e Renda. Use dd/MM/yyyy para data e formato brasileiro para valores monetários (ex: 1.234,56).");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro inesperado: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void excluirSegurado() {
        String cpf = txtCpf.getText().trim().replaceAll("\\D", "");
        if (StringUtils.ehNuloOuBranco(cpf) || !ValidadorCpfCnpj.ehCpfValido(cpf)) {
            showAlert(Alert.AlertType.ERROR, "Exclusão", "CPF inválido para exclusão.");
            txtCpf.setStyle("-fx-border-color: red;");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmar Exclusão",
                ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Tem certeza que deseja excluir o segurado pessoa " + formatCpf(cpf) + "?");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            if (seguradoPessoaDAO.excluir(cpf)) {
                showAlert(Alert.AlertType.INFORMATION, "Exclusão", "Segurado Pessoa excluído com sucesso!");
                limparCampos();
            } else {
                showAlert(Alert.AlertType.ERROR, "Exclusão", "Erro ao excluir segurado Pessoa (CPF não encontrado ou erro de persistência).");
            }
        }
    }

    private void limparCampos() {
        txtCpf.clear();
        txtNome.clear();
        txtDataNascimento.clear();
        txtBonus.clear();
        txtRenda.clear();
        txtLogradouro.clear();
        txtNumero.clear();
        txtComplemento.clear();

        txtCidade.clear();
        txtEstado.clear();
        txtCep.clear();
        txtPais.clear();
        setEstado(EstadoTela.INICIAL);
        txtCpf.requestFocus();
        clearAllFieldStyles();
    }

    private void limparCamposComCPF() {
        txtNome.clear();
        txtDataNascimento.clear();
        txtBonus.clear();
        txtRenda.clear();
        txtLogradouro.clear();
        txtNumero.clear();
        txtComplemento.clear();

        txtCidade.clear();
        txtEstado.clear();
        txtCep.clear();
        txtPais.clear();
        clearAllFieldStylesExceptCpf();
    }

    private void preencherCampos(SeguradoPessoa segurado) {
        txtCpf.setText(formatCpf(segurado.getCpf()));
        txtNome.setText(segurado.getNome());
        txtDataNascimento.setText(segurado.getDataNascimento().format(DATE_FORMATTER));
        txtBonus.setText(CURRENCY_FORMATTER_BR.format(segurado.getBonus()));
        txtRenda.setText(CURRENCY_FORMATTER_BR.format(segurado.getRenda()));

        Endereco endereco = segurado.getEndereco();
        if (endereco != null) {
            txtLogradouro.setText(endereco.getLogradouro());
            txtNumero.setText(endereco.getNumero());
            txtComplemento.setText(endereco.getComplemento());
            txtCidade.setText(endereco.getCidade());
            txtEstado.setText(endereco.getEstado());
            txtCep.setText(formatCep(endereco.getCep()));
            txtPais.setText(endereco.getPais());
        } else {
            txtLogradouro.clear();
            txtNumero.clear();
            txtComplemento.clear();
            txtCidade.clear();
            txtEstado.clear();
            txtCep.clear();
            txtPais.clear();
        }
        clearAllFieldStyles();
    }

    private Endereco criarObjetoEndereco() {
        return new Endereco(
                txtLogradouro.getText().trim(),
                cleanCep(txtCep.getText()),
                txtNumero.getText().trim(),
                txtComplemento.getText().trim(),
                txtPais.getText().trim(),
                txtEstado.getText().trim(),
                txtCidade.getText().trim()
        );
    }

    private boolean validarCamposComuns() {
        boolean isValid = true;

        if (StringUtils.ehNuloOuBranco(txtNome.getText())) {
            txtNome.setStyle("-fx-border-color: red;");
            showAlert(Alert.AlertType.ERROR, "Validação", "Nome completo deve ser informado.");
            isValid = false;
        } else {
            txtNome.setStyle("");
        }

        if (StringUtils.ehNuloOuBranco(txtDataNascimento.getText())) {
            txtDataNascimento.setStyle("-fx-border-color: red;");
            showAlert(Alert.AlertType.ERROR, "Validação", "Data de Nascimento deve ser informada.");
            isValid = false;
        } else {
            try {
                LocalDate.parse(txtDataNascimento.getText().trim(), DATE_FORMATTER);
                txtDataNascimento.setStyle("");
            } catch (DateTimeParseException e) {
                txtDataNascimento.setStyle("-fx-border-color: red;");
                showAlert(Alert.AlertType.ERROR, "Validação", "Data de Nascimento inválida. Use o formato dd/MM/yyyy.");
                isValid = false;
            }
        }

        if (StringUtils.ehNuloOuBranco(txtBonus.getText())) {
            txtBonus.setStyle("-fx-border-color: red;");
            showAlert(Alert.AlertType.ERROR, "Validação", "Bônus deve ser informado.");
            isValid = false;
        } else {
            try {
                BigDecimal bonus = (BigDecimal) CURRENCY_FORMATTER_BR.parse(txtBonus.getText().trim());
                if (bonus.compareTo(BigDecimal.ZERO) < 0) {
                    txtBonus.setStyle("-fx-border-color: red;");
                    showAlert(Alert.AlertType.ERROR, "Validação", "Bônus não pode ser negativo.");
                    isValid = false;
                } else {
                    txtBonus.setStyle("");
                }
            } catch (ParseException | NumberFormatException e) {
                txtBonus.setStyle("-fx-border-color: red;");
                showAlert(Alert.AlertType.ERROR, "Validação", "Formato de bônus inválido. Use o formato brasileiro (ex: 1.234,56).");
                isValid = false;
            }
        }

        if (StringUtils.ehNuloOuBranco(txtRenda.getText())) {
            txtRenda.setStyle("-fx-border-color: red;");
            showAlert(Alert.AlertType.ERROR, "Validação", "Renda deve ser informada.");
            isValid = false;
        } else {
            try {
                BigDecimal renda = (BigDecimal) CURRENCY_FORMATTER_BR.parse(txtRenda.getText().trim());
                if (renda.compareTo(BigDecimal.ZERO) <= 0) {
                    txtRenda.setStyle("-fx-border-color: red;");
                    showAlert(Alert.AlertType.ERROR, "Validação", "Renda deve ser maior que zero.");
                    isValid = false;
                } else {
                    txtRenda.setStyle("");
                }
            } catch (ParseException | NumberFormatException e) {
                txtRenda.setStyle("-fx-border-color: red;");
                showAlert(Alert.AlertType.ERROR, "Validação", "Formato de renda inválido. Use o formato brasileiro (ex: 1.234.567,89).");
                isValid = false;
            }
        }

        if (!validarCamposEndereco()) {
            isValid = false;
        }

        return isValid;
    }

    private boolean validarCamposEndereco() {
        boolean isValid = true;

        if (StringUtils.ehNuloOuBranco(txtLogradouro.getText())) {
            txtLogradouro.setStyle("-fx-border-color: red;");
            showAlert(Alert.AlertType.ERROR, "Validação", "Logradouro deve ser informado.");
            isValid = false;
        } else {
            txtLogradouro.setStyle("");
        }

        if (StringUtils.ehNuloOuBranco(txtNumero.getText())) {
            txtNumero.setStyle("-fx-border-color: red;");
            showAlert(Alert.AlertType.ERROR, "Validação", "Número deve ser informado.");
            isValid = false;
        } else {
            txtNumero.setStyle("");
        }

        String cleanCep = txtCep.getText().trim().replaceAll("\\D", "");
        if (StringUtils.ehNuloOuBranco(txtCep.getText())) {
            txtCep.setStyle("-fx-border-color: red;");
            showAlert(Alert.AlertType.ERROR, "Validação", "CEP deve ser informado.");
            isValid = false;
        } else if (cleanCep.length() != 8) {
            txtCep.setStyle("-fx-border-color: red;");
            showAlert(Alert.AlertType.ERROR, "Validação", "CEP deve ter 8 dígitos.");
            isValid = false;
        } else {
            txtCep.setStyle("");
        }

        if (StringUtils.ehNuloOuBranco(txtCidade.getText())) {
            txtCidade.setStyle("-fx-border-color: red;");
            showAlert(Alert.AlertType.ERROR, "Validação", "Cidade deve ser informada.");
            isValid = false;
        } else {
            txtCidade.setStyle("");
        }

        if (StringUtils.ehNuloOuBranco(txtEstado.getText()) || txtEstado.getText().trim().length() != 2) {
            txtEstado.setStyle("-fx-border-color: red;");
            showAlert(Alert.AlertType.ERROR, "Validação", "Estado deve ser informado com 2 letras (UF).");
            isValid = false;
        } else {
            txtEstado.setStyle("");
        }

        if (StringUtils.ehNuloOuBranco(txtPais.getText())) {
            txtPais.setStyle("-fx-border-color: red;");
            showAlert(Alert.AlertType.ERROR, "Validação", "País deve ser informado.");
            isValid = false;
        } else {
            txtPais.setStyle("");
        }
        return isValid;
    }

    private boolean validarCpfParaInclusao(String cpf) {
        if (StringUtils.ehNuloOuBranco(cpf)) {
            showAlert(Alert.AlertType.ERROR, "Validação", "CPF deve ser informado.");
            txtCpf.setStyle("-fx-border-color: red;");
            return false;
        }
        if (!ValidadorCpfCnpj.ehCpfValido(cpf)) {
            showAlert(Alert.AlertType.ERROR, "Validação", "CPF inválido.");
            txtCpf.setStyle("-fx-border-color: red;");
            return false;
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearAllFieldStyles() {
        txtCpf.setStyle("");
        txtNome.setStyle("");
        txtDataNascimento.setStyle("");
        txtBonus.setStyle("");
        txtRenda.setStyle("");
        txtLogradouro.setStyle("");
        txtNumero.setStyle("");
        txtComplemento.setStyle("");
        txtCidade.setStyle("");
        txtEstado.setStyle("");
        txtCep.setStyle("");
        txtPais.setStyle("");
    }

    private void clearAllFieldStylesExceptCpf() {
        txtNome.setStyle("");
        txtDataNascimento.setStyle("");
        txtBonus.setStyle("");
        txtRenda.setStyle("");
        txtLogradouro.setStyle("");
        txtNumero.setStyle("");
        txtComplemento.setStyle("");
        txtCidade.setStyle("");
        txtEstado.setStyle("");
        txtCep.setStyle("");
        txtPais.setStyle("");
    }

}