package br.edu.cs.poo.ac.seguro.testes;

import br.edu.cs.poo.ac.seguro.mediators.ApoliceMediator;
import br.edu.cs.poo.ac.seguro.mediators.DadosVeiculo;
import br.edu.cs.poo.ac.seguro.mediators.RetornoInclusaoApolice;
import br.edu.cs.poo.ac.seguro.entidades.CategoriaVeiculo;
import br.edu.cs.poo.ac.seguro.entidades.Endereco;
import br.edu.cs.poo.ac.seguro.entidades.SeguradoEmpresa;
import br.edu.cs.poo.ac.seguro.entidades.SeguradoPessoa;
import br.edu.cs.poo.ac.seguro.daos.SeguradoEmpresaDAO;
import br.edu.cs.poo.ac.seguro.daos.SeguradoPessoaDAO;
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
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors; // Necessário para .collect(Collectors.toList())

public class TelaInclusaoApoliceFX extends Application {

    private ApoliceMediator mediator;

    private TextField txtCpfCnpj;
    private TextField txtPlaca;
    private TextField txtAno;
    private TextField txtValorMaximoSegurado;
    private ComboBox<CategoriaVeiculo> cmbCategoriaVeiculo;

    private Button btnIncluir;
    private Button btnLimpar;

    private static final NumberFormat CURRENCY_PARSER_BR = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
    private static final NumberFormat CURRENCY_PARSER_US = NumberFormat.getNumberInstance(Locale.US);
    private static final DecimalFormat CURRENCY_FORMATTER_BR = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("pt", "BR"));

    static {
        CURRENCY_FORMATTER_BR.applyPattern("#,##0.00");
        if (CURRENCY_PARSER_BR instanceof DecimalFormat) {
            ((DecimalFormat) CURRENCY_PARSER_BR).setParseBigDecimal(true);
        }
        if (CURRENCY_PARSER_US instanceof DecimalFormat) {
            ((DecimalFormat) CURRENCY_PARSER_US).setParseBigDecimal(true);
        }
    }

    public TelaInclusaoApoliceFX() {
        this.mediator = ApoliceMediator.getInstancia();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Inclusão de Apólice");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        initComponents();
        setupLayout(grid);
        addListeners();
        setupTabOrder();

        Scene scene = new Scene(grid, 500, 380);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initComponents() {
        txtCpfCnpj = new TextField();
        txtCpfCnpj.setPromptText("CPF: 123.456.789-00 ou CNPJ: 11.222.333/0001-44");
        txtCpfCnpj.setMaxWidth(220);
        setupCpfCnpjMaskAndValidation(txtCpfCnpj);

        txtPlaca = new TextField();
        txtPlaca.setPromptText("Ex: ABC1234");
        txtPlaca.setMaxWidth(100);

        txtAno = new TextField();
        txtAno.setPromptText("Ex: 2023");
        txtAno.setMaxWidth(80);
        setupYearMask(txtAno);

        txtValorMaximoSegurado = new TextField();
        txtValorMaximoSegurado.setPromptText("Ex: 100.000,00");
        setupCurrencyMask(txtValorMaximoSegurado);

        cmbCategoriaVeiculo = new ComboBox<>();
        cmbCategoriaVeiculo.getItems().addAll(
                Arrays.stream(CategoriaVeiculo.values())
                        .sorted(Comparator.comparing(CategoriaVeiculo::getNome))
                        .collect(java.util.stream.Collectors.toList())
        );
        cmbCategoriaVeiculo.setConverter(new StringConverter<CategoriaVeiculo>() {
            @Override
            public String toString(CategoriaVeiculo categoria) {
                return categoria != null ? categoria.getNome() : "";
            }

            @Override
            public CategoriaVeiculo fromString(String string) {
                return null;
            }
        });
        if (!cmbCategoriaVeiculo.getItems().isEmpty()) {
            cmbCategoriaVeiculo.getSelectionModel().selectFirst();
        }

        btnIncluir = new Button("Incluir");
        btnLimpar = new Button("Limpar");
    }

    private void setupLayout(GridPane grid) {
        grid.add(new Label("CPF/CNPJ Segurado:"), 0, 0);
        grid.add(txtCpfCnpj, 1, 0);

        grid.add(new Label("Placa Veículo:"), 0, 1);
        grid.add(txtPlaca, 1, 1);

        grid.add(new Label("Ano Veículo:"), 0, 2);
        grid.add(txtAno, 1, 2);

        grid.add(new Label("Valor Máximo Segurado:"), 0, 3);
        grid.add(txtValorMaximoSegurado, 1, 3);

        grid.add(new Label("Categoria Veículo:"), 0, 4);
        grid.add(cmbCategoriaVeiculo, 1, 4);

        HBox hbButtons = new HBox(10);
        hbButtons.setAlignment(Pos.BOTTOM_RIGHT);
        hbButtons.getChildren().addAll(btnIncluir, btnLimpar);
        grid.add(hbButtons, 1, 5);
    }

    private void addListeners() {
        btnIncluir.setOnAction(e -> incluirApolice());
        btnLimpar.setOnAction(e -> limparCampos());

        txtCpfCnpj.setOnKeyPressed(e -> txtCpfCnpj.setStyle(""));
        txtPlaca.setOnKeyPressed(e -> txtPlaca.setStyle(""));
        txtAno.setOnKeyPressed(e -> txtAno.setStyle(""));
        txtValorMaximoSegurado.setOnKeyPressed(e -> txtValorMaximoSegurado.setStyle(""));
        cmbCategoriaVeiculo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cmbCategoriaVeiculo.setStyle("");
            }
        });
    }

    private void setupTabOrder() {
        txtCpfCnpj.setFocusTraversable(true);
        txtPlaca.setFocusTraversable(true);
        txtAno.setFocusTraversable(true);
        txtValorMaximoSegurado.setFocusTraversable(true);
        cmbCategoriaVeiculo.setFocusTraversable(true);
        btnIncluir.setFocusTraversable(true);
        btnLimpar.setFocusTraversable(true);
    }

    private void setupCpfCnpjMaskAndValidation(TextField textField) {
        textField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            String cleanedText = newText.replaceAll("\\D", "");

            if (cleanedText.length() > 14) {
                return null;
            }

            StringBuilder formattedText = new StringBuilder();
            if (cleanedText.length() <= 11) {
                for (int i = 0; i < cleanedText.length(); i++) {
                    formattedText.append(cleanedText.charAt(i));
                    if (i == 2 || i == 5) {
                        if (cleanedText.length() > i + 1) formattedText.append(".");
                    } else if (i == 8) {
                        if (cleanedText.length() > i + 1) formattedText.append("-");
                    }
                }
            } else {
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
                String cleanId = text.replaceAll("\\D", "");
                if (!cleanId.isEmpty()) {
                    boolean isValid = false;
                    if (cleanId.length() == 11) {
                        isValid = ValidadorCpfCnpj.ehCpfValido(cleanId);
                        if (isValid) textField.setText(formatCpf(cleanId));
                    } else if (cleanId.length() == 14) {
                        isValid = ValidadorCpfCnpj.ehCnpjValido(cleanId);
                        if (isValid) textField.setText(formatCnpj(cleanId));
                    }

                    if (!isValid) {
                        textField.setStyle("-fx-border-color: red;");
                        showAlert(Alert.AlertType.ERROR, "Erro de Validação", "CPF/CNPJ inválido ou incompleto.");
                    } else {
                        textField.setStyle("");
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

    private String formatCnpj(String cnpj) {
        if (cnpj == null || cnpj.replaceAll("\\D", "").length() != 14) {
            return cnpj;
        }
        String cleanCnpj = cnpj.replaceAll("\\D", "");
        return cleanCnpj.substring(0, 2) + "." +
                cleanCnpj.substring(2, 5) + "." +
                cleanCnpj.substring(5, 8) + "/" +
                cleanCnpj.substring(8, 12) + "-" +
                cleanCnpj.substring(12, 14);
    }

    private void setupYearMask(TextField textField) {
        Pattern pattern = Pattern.compile("\\d{0,4}");
        UnaryOperator<Change> filter = c -> {
            if (pattern.matcher(c.getControlNewText()).matches()) {
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
                        int ano = Integer.parseInt(text);
                        if (ano < 2020 || ano > 2025) {
                            textField.setStyle("-fx-border-color: red;");
                        } else {
                            textField.setStyle("");
                        }
                    } catch (NumberFormatException e) {
                        textField.setStyle("-fx-border-color: red;");
                    }
                } else {
                    textField.setStyle("");
                }
            }
        });
    }

    private void setupCurrencyMask(TextField textField) {
        Pattern pattern = Pattern.compile("[0-9,.]*");
        UnaryOperator<Change> filter = c -> {
            if (pattern.matcher(c.getControlNewText()).matches()) {
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
                    BigDecimal value = null;
                    try {
                        Number parsedNumber = CURRENCY_PARSER_BR.parse(text);
                        value = new BigDecimal(parsedNumber.doubleValue()).setScale(2, RoundingMode.HALF_UP);
                    } catch (ParseException eBr) {
                        try {
                            Number parsedNumber = CURRENCY_PARSER_US.parse(text);
                            value = new BigDecimal(parsedNumber.doubleValue()).setScale(2, RoundingMode.HALF_UP);
                        } catch (ParseException eUs) {
                            textField.setStyle("-fx-border-color: red;");
                            showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Valor monetário inválido. Digite no formato brasileiro (ex: 100.000,00) ou americano (ex: 100,000.00).");
                            return;
                        }
                    }

                    textField.setText(CURRENCY_FORMATTER_BR.format(value));
                    textField.setStyle("");

                } else {
                    textField.setStyle("");
                }
            }
        });
    }

    private void setupPlateMaskAndValidation(TextField textField) {
        Pattern platePattern = Pattern.compile("[A-Z]{0,3}\\d{0,4}");
        UnaryOperator<Change> filter = c -> {
            String newText = c.getControlNewText().toUpperCase();
            if (newText.length() > 7) {
                return null;
            }
            if (platePattern.matcher(newText).matches()) {
                c.setText(newText);
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
                Pattern finalPlatePattern = Pattern.compile("[A-Z]{3}\\d{4}");

                if (!text.isEmpty()) {
                    if (finalPlatePattern.matcher(text).matches()) {
                        textField.setStyle("");
                    } else {
                        textField.setStyle("-fx-border-color: red;");
                    }
                } else {
                    textField.setStyle("");
                }
            }
        });
    }


    private void incluirApolice() {
        List<String> errosFormato = new ArrayList<>();

        String cpfCnpj = txtCpfCnpj.getText().trim();
        String cleanCpfCnpj = cpfCnpj.replaceAll("\\D", "");

        if (cleanCpfCnpj.isEmpty()) {
            errosFormato.add("CPF/CNPJ do segurado é obrigatório.");
            txtCpfCnpj.setStyle("-fx-border-color: red;");
        } else if (cleanCpfCnpj.length() == 11) {
            if (!ValidadorCpfCnpj.ehCpfValido(cleanCpfCnpj)) {
                errosFormato.add("CPF inválido.");
                txtCpfCnpj.setStyle("-fx-border-color: red;");
            } else {
                txtCpfCnpj.setStyle("");
            }
        } else if (cleanCpfCnpj.length() == 14) {
            if (!ValidadorCpfCnpj.ehCnpjValido(cleanCpfCnpj)) {
                errosFormato.add("CNPJ inválido.");
                txtCpfCnpj.setStyle("-fx-border-color: red;");
            } else {
                txtCpfCnpj.setStyle("");
            }
        } else {
            errosFormato.add("CPF/CNPJ inválido. Digite 11 dígitos para CPF ou 14 para CNPJ.");
            txtCpfCnpj.setStyle("-fx-border-color: red;");
        }


        String placa = txtPlaca.getText().trim();
        Pattern finalPlatePattern = Pattern.compile("[A-Z]{3}\\d{4}");
        if (placa.isEmpty()) {
            errosFormato.add("Placa do veículo é obrigatória.");
            txtPlaca.setStyle("-fx-border-color: red;");
        } else if (!finalPlatePattern.matcher(placa).matches()) {
            errosFormato.add("Placa inválida. Deve ter 3 letras maiúsculas seguidas de 4 números (Ex: ABC1234).");
            txtPlaca.setStyle("-fx-border-color: red;");
        } else {
            txtPlaca.setStyle("");
        }

        int ano = 0;
        String anoText = txtAno.getText().trim();
        if (anoText.isEmpty()) {
            errosFormato.add("Ano do veículo é obrigatório.");
            txtAno.setStyle("-fx-border-color: red;");
        } else {
            try {
                ano = Integer.parseInt(anoText);
                if (ano < 2020 || ano > 2025) {
                    errosFormato.add("Ano tem que estar entre 2020 e 2025, incluindo estes.");
                    txtAno.setStyle("-fx-border-color: red;");
                } else {
                    txtAno.setStyle("");
                }
            } catch (NumberFormatException e) {
                errosFormato.add("Ano inválido. Digite apenas números.");
                txtAno.setStyle("-fx-border-color: red;");
            }
        }

        BigDecimal valorMaximoSegurado = null;
        String valorMaximoSeguradoText = txtValorMaximoSegurado.getText().trim();
        if (valorMaximoSeguradoText.isEmpty()) {
            errosFormato.add("Valor Máximo Segurado é obrigatório.");
            txtValorMaximoSegurado.setStyle("-fx-border-color: red;");
        } else {
            try {
                Number parsedNumber = CURRENCY_FORMATTER_BR.parse(valorMaximoSeguradoText);
                valorMaximoSegurado = new BigDecimal(parsedNumber.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP);
                txtValorMaximoSegurado.setStyle("");
            } catch (ParseException | NumberFormatException e) {
                errosFormato.add("Valor Máximo Segurado inválido. Verifique o formato.");
                txtValorMaximoSegurado.setStyle("-fx-border-color: red;");
            }
        }

        CategoriaVeiculo categoriaSelecionada = cmbCategoriaVeiculo.getSelectionModel().getSelectedItem();
        if (categoriaSelecionada == null) {
            errosFormato.add("Categoria do veículo é obrigatória.");
            cmbCategoriaVeiculo.setStyle("-fx-border-color: red;");
        } else {
            cmbCategoriaVeiculo.setStyle("");
        }

        if (!errosFormato.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Entrada", "Corrija os campos com formato inválido:\n" + String.join("\n", errosFormato));
            return;
        }

        try {
            int codigoCategoria = (categoriaSelecionada != null) ? categoriaSelecionada.getCodigo() : 0;
            DadosVeiculo dados = new DadosVeiculo(cleanCpfCnpj, placa, ano, valorMaximoSegurado, codigoCategoria);
            RetornoInclusaoApolice retorno = mediator.incluirApolice(dados);

            if (retorno.getMensagemErro() == null) {
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Apólice incluída com sucesso! Anote o número da apólice: " + retorno.getNumeroApolice());
                limparCampos();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Problemas na inclusão da apólice:\n" + retorno.getMensagemErro());
            }

        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Erro Interno", "Erro inesperado na construção do retorno: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro Inesperado", "Ocorreu um erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void limparCampos() {
        txtCpfCnpj.clear();
        txtPlaca.clear();
        txtAno.clear();
        txtValorMaximoSegurado.clear();
        if (!cmbCategoriaVeiculo.getItems().isEmpty()) {
            cmbCategoriaVeiculo.getSelectionModel().selectFirst();
        }
        txtCpfCnpj.setStyle("");
        txtPlaca.setStyle("");
        txtAno.setStyle("");
        txtValorMaximoSegurado.setStyle("");
        cmbCategoriaVeiculo.setStyle("");
        txtCpfCnpj.requestFocus();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinWidth(1000);
        alert.showAndWait();
    }

}