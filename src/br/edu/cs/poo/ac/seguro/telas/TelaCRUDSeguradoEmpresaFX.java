package br.edu.cs.poo.ac.seguro.testes;
import br.edu.cs.poo.ac.seguro.daos.SeguradoEmpresaDAO;
import br.edu.cs.poo.ac.seguro.entidades.Endereco;
import br.edu.cs.poo.ac.seguro.entidades.SeguradoEmpresa;
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

public class TelaCRUDSeguradoEmpresaFX extends Application {

    private SeguradoEmpresaDAO seguradoEmpresaDAO;

    private TextField txtCnpj;
    private TextField txtNome;
    private TextField txtDataAbertura;
    private TextField txtBonus;
    private TextField txtFaturamento;
    private CheckBox chkEhLocadoraDeVeiculos;

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

    public TelaCRUDSeguradoEmpresaFX() {
        this.seguradoEmpresaDAO = new SeguradoEmpresaDAO();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CRUD de Segurado Empresa");

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

        Scene scene = new Scene(grid, 600, 680);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initComponents() {
        txtCnpj = new TextField();
        txtCnpj.setPromptText("Ex: 11.222.333/0001-44");
        txtCnpj.setMaxWidth(180);
        setupCnpjMask(txtCnpj);

        txtNome = new TextField();
        txtNome.setPromptText("Razão Social ou Nome Fantasia");
        txtNome.setMaxWidth(250);

        txtDataAbertura = new TextField();
        txtDataAbertura.setPromptText("dd/MM/yyyy");
        txtDataAbertura.setMaxWidth(100);
        setupDateMask(txtDataAbertura);

        txtBonus = new TextField();
        txtBonus.setPromptText("Ex: 100,00");
        txtBonus.setMaxWidth(100);
        setupCurrencyMask(txtBonus);

        txtFaturamento = new TextField();
        txtFaturamento.setPromptText("Ex: 50.000,00");
        txtFaturamento.setMaxWidth(100);
        setupCurrencyMask(txtFaturamento);

        chkEhLocadoraDeVeiculos = new CheckBox("É Locadora de Veículos");

        txtLogradouro = new TextField();
        txtLogradouro.setPromptText("Rua, Av., Alameda...");
        txtLogradouro.setMaxWidth(250);

        txtNumero = new TextField();
        txtNumero.setPromptText("Número");
        txtNumero.setMaxWidth(80);

        txtComplemento = new TextField();
        txtComplemento.setPromptText("Apto, Bloco, Sala");
        txtComplemento.setMaxWidth(150);

        txtCidade = new TextField();
        txtCidade.setPromptText("Cidade");
        txtCidade.setMaxWidth(150);

        txtEstado = new TextField();
        txtEstado.setPromptText("UF (Ex: SP)");
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
        grid.add(new Label("CNPJ:"), 0, row);
        grid.add(txtCnpj, 1, row);
        grid.add(btnBuscar, 2, row);
        row++;

        grid.add(new Label("Nome/Razão Social:"), 0, row);
        grid.add(txtNome, 1, row, 2, 1);
        row++;

        grid.add(new Label("Data Abertura:"), 0, row);
        grid.add(txtDataAbertura, 1, row);
        row++;

        grid.add(new Label("Bônus:"), 0, row);
        grid.add(txtBonus, 1, row);
        row++;

        grid.add(new Label("Faturamento:"), 0, row);
        grid.add(txtFaturamento, 1, row);
        row++;

        grid.add(chkEhLocadoraDeVeiculos, 1, row, 2, 1);
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

        txtCnpj.setOnKeyPressed(e -> txtCnpj.setStyle(""));
        txtNome.setOnKeyPressed(e -> txtNome.setStyle(""));
        txtDataAbertura.setOnKeyPressed(e -> txtDataAbertura.setStyle(""));
        txtBonus.setOnKeyPressed(e -> txtBonus.setStyle(""));
        txtFaturamento.setOnKeyPressed(e -> txtFaturamento.setStyle(""));
        chkEhLocadoraDeVeiculos.selectedProperty().addListener((obs, oldVal, newVal) -> chkEhLocadoraDeVeiculos.setStyle(""));
        txtLogradouro.setOnKeyPressed(e -> txtLogradouro.setStyle(""));
        txtNumero.setOnKeyPressed(e -> txtNumero.setStyle(""));
        txtComplemento.setOnKeyPressed(e -> txtComplemento.setStyle(""));
        txtCidade.setOnKeyPressed(e -> txtCidade.setStyle(""));
        txtEstado.setOnKeyPressed(e -> txtEstado.setStyle(""));
        txtCep.setOnKeyPressed(e -> txtCep.setStyle(""));
        txtPais.setOnKeyPressed(e -> txtPais.setStyle(""));
    }

    private void setupTabOrder() {
        txtCnpj.setFocusTraversable(true);
        txtNome.setFocusTraversable(true);
        txtDataAbertura.setFocusTraversable(true);
        txtBonus.setFocusTraversable(true);
        txtFaturamento.setFocusTraversable(true);
        chkEhLocadoraDeVeiculos.setFocusTraversable(true);
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
        boolean cnpjEditavel = false;
        boolean camposEditaveis = false;
        boolean btnBuscarHabilitado = false;
        boolean btnIncluirHabilitado = false;
        boolean btnAlterarHabilitado = false;
        boolean btnExcluirHabilitado = false;
        boolean btnLimparHabilitado = true;

        switch (estado) {
            case INICIAL:
                cnpjEditavel = true;
                btnBuscarHabilitado = true;
                break;
            case BUSCA_SUCESSO:
                cnpjEditavel = false;
                camposEditaveis = true;
                btnAlterarHabilitado = true;
                btnExcluirHabilitado = true;
                break;
            case INCLUSAO_NOVO:
                cnpjEditavel = false;
                camposEditaveis = true;
                btnIncluirHabilitado = true;
                break;
        }

        txtCnpj.setEditable(cnpjEditavel);
        txtNome.setEditable(camposEditaveis);
        txtDataAbertura.setEditable(camposEditaveis);
        txtBonus.setEditable(camposEditaveis);
        txtFaturamento.setEditable(camposEditaveis);
        chkEhLocadoraDeVeiculos.setDisable(!camposEditaveis);
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

    private void setupCnpjMask(TextField textField) {
        textField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            String cleanedText = newText.replaceAll("\\D", "");

            if (cleanedText.length() > 14) {
                return null;
            }

            StringBuilder formattedText = new StringBuilder();
            for (int i = 0; i < cleanedText.length(); i++) {
                formattedText.append(cleanedText.charAt(i));
                if (i == 1 || i == 4) {
                    if (cleanedText.length() > i + 1) formattedText.append(".");
                } else if (i == 7) {
                    if (cleanedText.length() > i + 1) formattedText.append("/");
                } else if (i == 11) {
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
                String cleanCnpj = text.replaceAll("\\D", "");
                if (!cleanCnpj.isEmpty()) {
                    if (ValidadorCpfCnpj.ehCnpjValido(cleanCnpj)) {
                        textField.setStyle("");
                        if (cleanCnpj.length() == 14) {
                            textField.setText(formatCnpj(cleanCnpj));
                        }
                    } else {
                        textField.setStyle("-fx-border-color: red;");
                        showAlert(Alert.AlertType.ERROR, "Erro de Validação", "CNPJ inválido.");
                    }
                } else {
                    textField.setStyle("");
                }
            }
        });
    }

    private String formatCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) {
            return cnpj;
        }
        return cnpj.substring(0, 2) + "." +
                cnpj.substring(2, 5) + "." +
                cnpj.substring(5, 8) + "/" +
                cnpj.substring(8, 12) + "-" +
                cnpj.substring(12, 14);
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
                        showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Valor inválido. Por favor, use o formato brasileiro (ex: 1.234.567,89).");
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
        if (cep == null || cep.length() != 8) {
            return cep;
        }
        return cep.substring(0, 5) + "-" + cep.substring(5, 8);
    }

    private String cleanCep(String cep) {
        return cep != null ? cep.replaceAll("\\D", "") : null;
    }

    private void buscarSegurado() {
        String cnpj = txtCnpj.getText().trim();

        if (StringUtils.ehNuloOuBranco(cnpj)) {
            showAlert(Alert.AlertType.WARNING, "Busca", "CNPJ deve ser informado para a busca.");
            txtCnpj.setStyle("-fx-border-color: orange;");
            setEstado(EstadoTela.INICIAL);
            return;
        }

        String cleanCnpj = cnpj.replaceAll("\\D", "");
        if (!ValidadorCpfCnpj.ehCnpjValido(cleanCnpj)) {
            showAlert(Alert.AlertType.ERROR, "Busca", "CNPJ inválido.");
            txtCnpj.setStyle("-fx-border-color: red;");
            setEstado(EstadoTela.INICIAL);
            return;
        }

        SeguradoEmpresa segurado = seguradoEmpresaDAO.buscar(cleanCnpj);

        if (segurado != null) {
            preencherCampos(segurado);
            setEstado(EstadoTela.BUSCA_SUCESSO);
            showAlert(Alert.AlertType.INFORMATION, "Busca", "Segurado Empresa encontrado!");
        } else {
            limparCamposComCNPJ();
            txtCnpj.setText(formatCnpj(cleanCnpj));
            setEstado(EstadoTela.INCLUSAO_NOVO);
            showAlert(Alert.AlertType.INFORMATION, "Busca", "Segurado Empresa não encontrado. Você pode incluí-lo.");
            txtNome.requestFocus();
        }
    }

    private void incluirSegurado() {
        String cnpj = txtCnpj.getText().trim().replaceAll("\\D", "");
        if (validarCamposComuns() && validarCnpjParaInclusao(cnpj)) {
            try {
                Endereco endereco = criarObjetoEndereco();
                LocalDate dataAbertura = LocalDate.parse(txtDataAbertura.getText().trim(), DATE_FORMATTER);

                BigDecimal bonus = (BigDecimal) CURRENCY_FORMATTER_BR.parse(txtBonus.getText().trim());
                BigDecimal faturamentoBigDecimal = (BigDecimal) CURRENCY_FORMATTER_BR.parse(txtFaturamento.getText().trim());
                double faturamento = faturamentoBigDecimal.doubleValue();

                boolean ehLocadora = chkEhLocadoraDeVeiculos.isSelected();

                SeguradoEmpresa novaEmpresa = new SeguradoEmpresa(
                        txtNome.getText().trim(),
                        endereco,
                        dataAbertura,
                        bonus,
                        cnpj,
                        faturamento,
                        ehLocadora
                );

                if (seguradoEmpresaDAO.incluir(novaEmpresa)) {
                    showAlert(Alert.AlertType.INFORMATION, "Inclusão", "Segurado Empresa incluído com sucesso!");
                    limparCampos();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Inclusão", "Erro ao incluir segurado Empresa (CNPJ já existe ou erro de persistência).");
                }
            } catch (ParseException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Verifique os formatos de Data de Abertura, Bônus e Faturamento. Use dd/MM/yyyy para data e formato brasileiro para valores monetários (ex: 1.234,56).");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro inesperado: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void alterarSegurado() {
        String cnpj = txtCnpj.getText().trim().replaceAll("\\D", "");
        if (validarCamposComuns()) {
            try {
                Endereco endereco = criarObjetoEndereco();
                LocalDate dataAbertura = LocalDate.parse(txtDataAbertura.getText().trim(), DATE_FORMATTER);

                BigDecimal bonus = (BigDecimal) CURRENCY_FORMATTER_BR.parse(txtBonus.getText().trim());
                BigDecimal faturamentoBigDecimal = (BigDecimal) CURRENCY_FORMATTER_BR.parse(txtFaturamento.getText().trim());
                double faturamento = faturamentoBigDecimal.doubleValue();

                boolean ehLocadora = chkEhLocadoraDeVeiculos.isSelected();

                SeguradoEmpresa empresaAlterada = new SeguradoEmpresa(
                        txtNome.getText().trim(),
                        endereco,
                        dataAbertura,
                        bonus,
                        cnpj,
                        faturamento,
                        ehLocadora
                );

                if (seguradoEmpresaDAO.alterar(empresaAlterada)) {
                    showAlert(Alert.AlertType.INFORMATION, "Alteração", "Segurado Empresa alterado com sucesso!");
                    limparCampos();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Alteração", "Erro ao alterar segurado Empresa (CNPJ não encontrado ou erro de persistência).");
                }
            } catch (ParseException e) {
                showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Verifique os formatos de Data de Abertura, Bônus e Faturamento. Use dd/MM/yyyy para data e formato brasileiro para valores monetários (ex: 1.234,56).");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro inesperado: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void excluirSegurado() {
        String cnpj = txtCnpj.getText().trim().replaceAll("\\D", "");
        if (StringUtils.ehNuloOuBranco(cnpj) || !ValidadorCpfCnpj.ehCnpjValido(cnpj)) {
            showAlert(Alert.AlertType.ERROR, "Exclusão", "CNPJ inválido para exclusão.");
            txtCnpj.setStyle("-fx-border-color: red;");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Confirmar Exclusão",
                ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Tem certeza que deseja excluir o segurado empresa " + formatCnpj(cnpj) + "?");
        alert.showAndWait();

        if (alert.getResult() == ButtonType.YES) {
            if (seguradoEmpresaDAO.excluir(cnpj)) {
                showAlert(Alert.AlertType.INFORMATION, "Exclusão", "Segurado Empresa excluído com sucesso!");
                limparCampos();
            } else {
                showAlert(Alert.AlertType.ERROR, "Exclusão", "Erro ao excluir segurado Empresa (CNPJ não encontrado ou erro de persistência).");
            }
        }
    }

    private void limparCampos() {
        txtCnpj.clear();
        txtNome.clear();
        txtDataAbertura.clear();
        txtBonus.clear();
        txtFaturamento.clear();
        chkEhLocadoraDeVeiculos.setSelected(false);
        txtLogradouro.clear();
        txtNumero.clear();
        txtComplemento.clear();

        txtCidade.clear();
        txtEstado.clear();
        txtCep.clear();
        txtPais.clear();
        setEstado(EstadoTela.INICIAL);
        txtCnpj.requestFocus();
        clearAllFieldStyles();
    }

    private void limparCamposComCNPJ() {
        txtNome.clear();
        txtDataAbertura.clear();
        txtBonus.clear();
        txtFaturamento.clear();
        chkEhLocadoraDeVeiculos.setSelected(false);
        txtLogradouro.clear();
        txtNumero.clear();
        txtComplemento.clear();

        txtCidade.clear();
        txtEstado.clear();
        txtCep.clear();
        txtPais.clear();
        clearAllFieldStylesExceptCnpj();
    }

    private void preencherCampos(SeguradoEmpresa segurado) {
        txtCnpj.setText(formatCnpj(segurado.getCnpj()));
        txtNome.setText(segurado.getNome());
        txtDataAbertura.setText(segurado.getDataAbertura().format(DATE_FORMATTER));
        txtBonus.setText(CURRENCY_FORMATTER_BR.format(segurado.getBonus()));
        txtFaturamento.setText(CURRENCY_FORMATTER_BR.format(segurado.getFaturamento()));
        chkEhLocadoraDeVeiculos.setSelected(segurado.getEhLocadoraDeVeiculos());

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
            showAlert(Alert.AlertType.ERROR, "Validação", "Nome/Razão Social deve ser informado.");
            isValid = false;
        } else {
            txtNome.setStyle("");
        }

        if (StringUtils.ehNuloOuBranco(txtDataAbertura.getText())) {
            txtDataAbertura.setStyle("-fx-border-color: red;");
            showAlert(Alert.AlertType.ERROR, "Validação", "Data de Abertura deve ser informada.");
            isValid = false;
        } else {
            try {
                LocalDate.parse(txtDataAbertura.getText().trim(), DATE_FORMATTER);
                txtDataAbertura.setStyle("");
            } catch (DateTimeParseException e) {
                txtDataAbertura.setStyle("-fx-border-color: red;");
                showAlert(Alert.AlertType.ERROR, "Validação", "Data de Abertura inválida. Use o formato dd/MM/yyyy.");
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

        if (StringUtils.ehNuloOuBranco(txtFaturamento.getText())) {
            txtFaturamento.setStyle("-fx-border-color: red;");
            showAlert(Alert.AlertType.ERROR, "Validação", "Faturamento deve ser informado.");
            isValid = false;
        } else {
            try {
                BigDecimal faturamento = (BigDecimal) CURRENCY_FORMATTER_BR.parse(txtFaturamento.getText().trim());
                if (faturamento.compareTo(BigDecimal.ZERO) <= 0) {
                    txtFaturamento.setStyle("-fx-border-color: red;");
                    showAlert(Alert.AlertType.ERROR, "Validação", "Faturamento deve ser maior que zero.");
                    isValid = false;
                } else {
                    txtFaturamento.setStyle("");
                }
            } catch (ParseException | NumberFormatException e) {
                txtFaturamento.setStyle("-fx-border-color: red;");
                showAlert(Alert.AlertType.ERROR, "Validação", "Formato de faturamento inválido. Use o formato brasileiro (ex: 1.234.567,89).");
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

    private boolean validarCnpjParaInclusao(String cnpj) {
        if (StringUtils.ehNuloOuBranco(cnpj)) {
            showAlert(Alert.AlertType.ERROR, "Validação", "CNPJ deve ser informado.");
            txtCnpj.setStyle("-fx-border-color: red;");
            return false;
        }
        if (!ValidadorCpfCnpj.ehCnpjValido(cnpj)) {
            showAlert(Alert.AlertType.ERROR, "Validação", "CNPJ inválido.");
            txtCnpj.setStyle("-fx-border-color: red;");
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
        txtCnpj.setStyle("");
        txtNome.setStyle("");
        txtDataAbertura.setStyle("");
        txtBonus.setStyle("");
        txtFaturamento.setStyle("");
        chkEhLocadoraDeVeiculos.setStyle("");
        txtLogradouro.setStyle("");
        txtNumero.setStyle("");
        txtComplemento.setStyle("");
        txtCidade.setStyle("");
        txtEstado.setStyle("");
        txtCep.setStyle("");
        txtPais.setStyle("");
    }

    private void clearAllFieldStylesExceptCnpj() {
        txtNome.setStyle("");
        txtDataAbertura.setStyle("");
        txtBonus.setStyle("");
        txtFaturamento.setStyle("");
        chkEhLocadoraDeVeiculos.setStyle("");
        txtLogradouro.setStyle("");
        txtNumero.setStyle("");
        txtComplemento.setStyle("");
        txtCidade.setStyle("");
        txtEstado.setStyle("");
        txtCep.setStyle("");
        txtPais.setStyle("");
    }

}