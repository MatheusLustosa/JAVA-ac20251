package br.edu.cs.poo.ac.seguro.testes;

import br.edu.cs.poo.ac.seguro.daos.SeguradoEmpresaDAO;
import br.edu.cs.poo.ac.seguro.daos.SeguradoPessoaDAO;
import br.edu.cs.poo.ac.seguro.mediators.DadosSinistro;
import br.edu.cs.poo.ac.seguro.mediators.SinistroMediator;
import br.edu.cs.poo.ac.seguro.daos.VeiculoDAO;
import br.edu.cs.poo.ac.seguro.daos.ApoliceDAO;
import br.edu.cs.poo.ac.seguro.entidades.*;
import br.edu.cs.poo.ac.seguro.excecoes.ExcecaoValidacaoDados;
import br.edu.cs.poo.ac.seguro.mediators.ValidadorCpfCnpj;
import br.edu.cs.poo.ac.seguro.mediators.StringUtils;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TelaInclusaoSinistroFX extends Application {

    private SinistroMediator mediator;
    private SeguradoPessoaDAO seguradoPessoaDAO;
    private SeguradoEmpresaDAO seguradoEmpresaDAO;
    private VeiculoDAO veiculoDAO;

    private TextField txtPlaca;
    private TextField txtDataHoraSinistro;
    private TextField txtCpfCnpjSegurado;
    private TextField txtUsuarioRegistro;
    private TextField txtValorSinistro;
    private ComboBox<TipoSinistro> cmbTipoSinistro;

    private Button btnBuscarSegurado;
    private Button btnIncluir;
    private Button btnLimpar;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final DecimalFormat CURRENCY_FORMATTER_BR = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("pt", "BR"));

    static {
        CURRENCY_FORMATTER_BR.applyPattern("#,##0.00");
        CURRENCY_FORMATTER_BR.setParseBigDecimal(true);
    }

    public TelaInclusaoSinistroFX() {
        this.mediator = SinistroMediator.getInstancia();
        this.seguradoPessoaDAO = new SeguradoPessoaDAO();
        this.seguradoEmpresaDAO = new SeguradoEmpresaDAO();
        this.veiculoDAO = new VeiculoDAO();
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Inclusão de Sinistro");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        initComponents();
        setupLayout(grid);
        addListeners();
        setupTabOrder();

        Scene scene = new Scene(grid, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initComponents() {
        txtPlaca = new TextField();
        txtPlaca.setPromptText("Ex: ABC1234");
        txtPlaca.setMaxWidth(120);

        txtDataHoraSinistro = new TextField();
        txtDataHoraSinistro.setPromptText("dd/MM/yyyy HH:mm:ss");
        setupDateTimeMask(txtDataHoraSinistro);

        txtCpfCnpjSegurado = new TextField();
        txtCpfCnpjSegurado.setPromptText("CPF: 123.456.789-00 ou CNPJ: 11.222.333/0001-44");
        txtCpfCnpjSegurado.setMaxWidth(220);
        setupCpfCnpjMask(txtCpfCnpjSegurado);

        txtUsuarioRegistro = new TextField();
        txtUsuarioRegistro.setPromptText("Nome do Usuário/Segurado");
        txtUsuarioRegistro.setEditable(false);

        txtValorSinistro = new TextField();
        txtValorSinistro.setPromptText("Ex: 1.234,56");
        setupCurrencyMask(txtValorSinistro);

        cmbTipoSinistro = new ComboBox<>();
        cmbTipoSinistro.getItems().addAll(
                Arrays.stream(TipoSinistro.values())
                        .sorted(Comparator.comparing(TipoSinistro::getNome))
                        .collect(Collectors.toList())
        );
        cmbTipoSinistro.setConverter(new StringConverter<TipoSinistro>() {
            @Override
            public String toString(TipoSinistro tipo) {
                return tipo != null ? tipo.getNome() : "";
            }

            @Override
            public TipoSinistro fromString(String string) {
                return null;
            }
        });
        if (!cmbTipoSinistro.getItems().isEmpty()) {
            cmbTipoSinistro.getSelectionModel().selectFirst();
        }

        btnBuscarSegurado = new Button("Buscar Segurado");
        btnIncluir = new Button("Incluir");
        btnLimpar = new Button("Limpar");
    }

    private void setupLayout(GridPane grid) {
        int row = 0;
        grid.add(new Label("Placa:"), 0, row);
        grid.add(txtPlaca, 1, row);
        row++;

        grid.add(new Label("Data/Hora Sinistro:"), 0, row);
        grid.add(txtDataHoraSinistro, 1, row);
        row++;

        grid.add(new Label("CPF/CNPJ Segurado:"), 0, row);
        HBox hbCpfCnpj = new HBox(5);
        hbCpfCnpj.getChildren().addAll(txtCpfCnpjSegurado, btnBuscarSegurado);
        grid.add(hbCpfCnpj, 1, row);
        row++;

        grid.add(new Label("Usuário Registro:"), 0, row);
        grid.add(txtUsuarioRegistro, 1, row);
        row++;

        grid.add(new Label("Valor Sinistro:"), 0, row);
        grid.add(txtValorSinistro, 1, row);
        row++;

        grid.add(new Label("Tipo Sinistro:"), 0, row);
        grid.add(cmbTipoSinistro, 1, row);
        row++;

        HBox hbButtons = new HBox(10);
        hbButtons.setAlignment(Pos.BOTTOM_RIGHT);
        hbButtons.getChildren().addAll(btnIncluir, btnLimpar);
        grid.add(hbButtons, 1, row);
    }

    private void addListeners() {
        btnIncluir.setOnAction(e -> incluirSinistro());
        btnLimpar.setOnAction(e -> limparCampos());
        btnBuscarSegurado.setOnAction(e -> buscarNomeSegurado());

        txtCpfCnpjSegurado.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtCpfCnpjSegurado.getText().trim().isEmpty()) {
                buscarNomeSegurado();
            }
        });

        txtPlaca.setOnKeyPressed(e -> txtPlaca.setStyle(""));
        txtDataHoraSinistro.setOnKeyPressed(e -> txtDataHoraSinistro.setStyle(""));
        txtCpfCnpjSegurado.setOnKeyPressed(e -> txtCpfCnpjSegurado.setStyle(""));
        txtValorSinistro.setOnKeyPressed(e -> txtValorSinistro.setStyle(""));
        cmbTipoSinistro.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cmbTipoSinistro.setStyle("");
            }
        });
    }

    private void setupTabOrder() {
        txtPlaca.setFocusTraversable(true);
        txtDataHoraSinistro.setFocusTraversable(true);
        txtCpfCnpjSegurado.setFocusTraversable(true);
        btnBuscarSegurado.setFocusTraversable(true);
        txtUsuarioRegistro.setFocusTraversable(true);
        txtValorSinistro.setFocusTraversable(true);
        cmbTipoSinistro.setFocusTraversable(true);
        btnIncluir.setFocusTraversable(true);
        btnLimpar.setFocusTraversable(true);
    }

    private void setupDateTimeMask(TextField textField) {
        final String format = "dd/MM/yyyy HH:mm:ss";
        Pattern inputPattern = Pattern.compile("[0-9/ :]*");

        UnaryOperator<Change> filter = c -> {
            String newText = c.getControlNewText();
            if (inputPattern.matcher(newText).matches()) {
                if (newText.length() > format.length()) {
                    return null;
                }
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
                        LocalDateTime parsedDateTime = LocalDateTime.parse(text, DATE_TIME_FORMATTER);
                        textField.setText(parsedDateTime.format(DATE_TIME_FORMATTER));
                        textField.setStyle("");
                    } catch (DateTimeParseException e) {
                        textField.setStyle("-fx-border-color: red;");
                        showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Data/Hora do sinistro inválida. Use o formato " + format + ".");
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
                        showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Valor do sinistro inválido. Por favor, use o formato brasileiro (ex: 1.234,56).");
                        return;
                    }
                } else {
                    textField.setStyle("");
                }
            }
        });
    }

    private void setupCpfCnpjMask(TextField textField) {
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

    private void buscarNomeSegurado() {
        String idSegurado = txtCpfCnpjSegurado.getText().trim();
        String cleanIdSegurado = idSegurado.replaceAll("\\D", "");
        txtUsuarioRegistro.clear();

        if (StringUtils.ehNuloOuBranco(cleanIdSegurado)) {
            showAlert(Alert.AlertType.WARNING, "Busca de Segurado", "Informe o CPF ou CNPJ do segurado para buscar.");
            txtCpfCnpjSegurado.requestFocus();
            txtCpfCnpjSegurado.setStyle("-fx-border-color: orange;");
            return;
        }

        Segurado segurado = null;
        boolean idValido = false;

        if (cleanIdSegurado.length() == 11) {
            idValido = ValidadorCpfCnpj.ehCpfValido(cleanIdSegurado);
            if (idValido) {
                segurado = seguradoPessoaDAO.buscar(cleanIdSegurado);
            } else {
                showAlert(Alert.AlertType.ERROR, "Busca de Segurado", "CPF inválido.");
            }
        } else if (cleanIdSegurado.length() == 14) {
            idValido = ValidadorCpfCnpj.ehCnpjValido(cleanIdSegurado);
            if (idValido) {
                segurado = seguradoEmpresaDAO.buscar(cleanIdSegurado);
            } else {
                showAlert(Alert.AlertType.ERROR, "Busca de Segurado", "CNPJ inválido.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Busca de Segurado", "Formato de CPF/CNPJ inválido. Use 11 dígitos para CPF ou 14 para CNPJ.");
        }

        if (segurado != null) {
            txtUsuarioRegistro.setText(segurado.getNome());
            txtCpfCnpjSegurado.setStyle("");
            showAlert(Alert.AlertType.INFORMATION, "Busca de Segurado", "Segurado encontrado: " + segurado.getNome());
        } else if (idValido) {
            txtUsuarioRegistro.clear();
            txtCpfCnpjSegurado.setStyle("-fx-border-color: red;");
            showAlert(Alert.AlertType.INFORMATION, "Busca de Segurado", "Segurado não encontrado para o CPF/CNPJ informado.");
        }
    }

    private void incluirSinistro() {
        List<String> errosFormato = new ArrayList<>();

        String placa = txtPlaca.getText().trim();
        Pattern finalPlatePattern = Pattern.compile("[A-Z]{3}\\d{4}");
        Veiculo veiculoEncontrado = null;

        if (placa.isEmpty()) {
            txtPlaca.setStyle("-fx-border-color: red;");
            errosFormato.add("Placa é obrigatória.");
        } else if (!finalPlatePattern.matcher(placa).matches()) {
            txtPlaca.setStyle("-fx-border-color: red;");
            errosFormato.add("Placa inválida. Deve ter 3 letras maiúsculas seguidas de 4 números (Ex: ABC1234).");
        } else {
            txtPlaca.setStyle("");
            veiculoEncontrado = veiculoDAO.buscar(placa);
            if (veiculoEncontrado == null) {
                txtPlaca.setStyle("-fx-border-color: red;");
                errosFormato.add("Veículo com a placa '" + placa + "' não encontrado no cadastro.");
            } else {
                txtPlaca.setStyle("");
            }
        }

        String cpfCnpjDigitado = txtCpfCnpjSegurado.getText().trim();
        String cleanCpfCnpjDigitado = cpfCnpjDigitado.replaceAll("\\D", "");
        String usuarioRegistro = txtUsuarioRegistro.getText().trim();

        if (StringUtils.ehNuloOuBranco(cleanCpfCnpjDigitado)) {
            txtCpfCnpjSegurado.setStyle("-fx-border-color: red;");
            errosFormato.add("CPF/CNPJ do segurado é obrigatório.");
        } else {
            boolean idDigitadoValido = false;
            if (cleanCpfCnpjDigitado.length() == 11) {
                idDigitadoValido = ValidadorCpfCnpj.ehCpfValido(cleanCpfCnpjDigitado);
            } else if (cleanCpfCnpjDigitado.length() == 14) {
                idDigitadoValido = ValidadorCpfCnpj.ehCnpjValido(cleanCpfCnpjDigitado);
            } else {
                txtCpfCnpjSegurado.setStyle("-fx-border-color: red;");
                errosFormato.add("Formato de CPF/CNPJ inválido. Digite 11 dígitos para CPF ou 14 para CNPJ.");
                idDigitadoValido = false;
            }

            if (!idDigitadoValido) {
                txtCpfCnpjSegurado.setStyle("-fx-border-color: red;");
                if (!errosFormato.contains("Formato de CPF/CNPJ inválido. Digite 11 dígitos para CPF ou 14 para CNPJ.")) {
                    errosFormato.add("CPF ou CNPJ do segurado inválido.");
                }
            } else if (StringUtils.ehNuloOuBranco(usuarioRegistro)) {
                txtCpfCnpjSegurado.setStyle("-fx-border-color: red;");
                errosFormato.add("Segurado não encontrado para o CPF/CNPJ informado. Utilize o botão 'Buscar Segurado'.");
            } else {
                txtCpfCnpjSegurado.setStyle("");
            }
        }

        LocalDateTime dataHoraSinistro = null;
        if (StringUtils.ehNuloOuBranco(txtDataHoraSinistro.getText())) {
            errosFormato.add("Data/Hora do sinistro é obrigatória.");
            txtDataHoraSinistro.setStyle("-fx-border-color: red;");
        } else {
            try {
                dataHoraSinistro = LocalDateTime.parse(txtDataHoraSinistro.getText().trim(), DATE_TIME_FORMATTER);
                txtDataHoraSinistro.setStyle("");
            } catch (DateTimeParseException e) {
                txtDataHoraSinistro.setStyle("-fx-border-color: red;");
                errosFormato.add("Data/Hora do sinistro inválida. Use o formato dd/MM/yyyy HH:mm:ss.");
            }
        }

        BigDecimal valorSinistro = null;
        if (StringUtils.ehNuloOuBranco(txtValorSinistro.getText())) {
            errosFormato.add("Valor do sinistro é obrigatório.");
            txtValorSinistro.setStyle("-fx-border-color: red;");
        } else {
            try {
                Number parsedNumber = CURRENCY_FORMATTER_BR.parse(txtValorSinistro.getText().trim());
                valorSinistro = new BigDecimal(parsedNumber.doubleValue()).setScale(2, RoundingMode.HALF_UP);
                if (valorSinistro.compareTo(BigDecimal.ZERO) <= 0) {
                    errosFormato.add("Valor do sinistro deve ser maior que zero.");
                }
                txtValorSinistro.setStyle("");
            } catch (ParseException | NumberFormatException e) {
                txtValorSinistro.setStyle("-fx-border-color: red;");
                errosFormato.add("Valor do sinistro inválido. Por favor, use o formato brasileiro (ex: 1.234,56).");
            }
        }

        TipoSinistro tipoSinistroSelecionado = cmbTipoSinistro.getSelectionModel().getSelectedItem();
        if (tipoSinistroSelecionado == null) {
            errosFormato.add("Tipo de sinistro é obrigatório.");
            cmbTipoSinistro.setStyle("-fx-border-color: red;");
        } else {
            cmbTipoSinistro.setStyle("");
        }

        if (!errosFormato.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Entrada", "Corrija os campos com formato inválido:\n" + String.join("\n", errosFormato));
            return;
        }

        if (veiculoEncontrado != null && veiculoEncontrado.getProprietario() != null) {
            String idSeguradoDaPlaca = null;
            if (veiculoEncontrado.getProprietario() instanceof SeguradoPessoa) {
                idSeguradoDaPlaca = ((SeguradoPessoa) veiculoEncontrado.getProprietario()).getCpf();
            } else if (veiculoEncontrado.getProprietario() instanceof SeguradoEmpresa) {
                idSeguradoDaPlaca = ((SeguradoEmpresa) veiculoEncontrado.getProprietario()).getCnpj();
            }

            if (idSeguradoDaPlaca != null && !idSeguradoDaPlaca.equals(cleanCpfCnpjDigitado)) {
                txtPlaca.setStyle("-fx-border-color: red;");
                txtCpfCnpjSegurado.setStyle("-fx-border-color: red;");
                showAlert(Alert.AlertType.ERROR, "Erro de Validação",
                        "O CPF/CNPJ (" + (cleanCpfCnpjDigitado.length() == 11 ? formatCpf(cleanCpfCnpjDigitado) : formatCnpj(cleanCpfCnpjDigitado)) + ") informado não corresponde ao segurado proprietário do veículo de placa " + placa + " (CPF/CNPJ do proprietário: " + (idSeguradoDaPlaca.length() == 11 ? formatCpf(idSeguradoDaPlaca) : formatCnpj(idSeguradoDaPlaca)) + ").");
                return;
            }
        } else if (veiculoEncontrado != null && veiculoEncontrado.getProprietario() == null) {
            errosFormato.add("O veículo com placa " + placa + " não possui um segurado associado no cadastro.");
        }

        if (!errosFormato.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Entrada", "Corrija os campos com formato inválido:\n" + String.join("\n", errosFormato));
            return;
        }

        double valorSinistroDouble = valorSinistro.doubleValue();

        int codigoTipoSinistro = tipoSinistroSelecionado.getCodigo();

        DadosSinistro dados = new DadosSinistro(placa, dataHoraSinistro, usuarioRegistro, valorSinistroDouble, codigoTipoSinistro);

        try {
            String numeroSinistro = mediator.incluirSinistro(dados, LocalDateTime.now());

            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Sinistro incluído com sucesso! Anote o número do sinistro: " + numeroSinistro);
            limparCampos();
        } catch (ExcecaoValidacaoDados e) {
            String mensagensErro = String.join("\n", e.getMensagens());
            showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Problemas na inclusão do sinistro:\n" + mensagensErro);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erro Inesperado", "Ocorreu um erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void limparCampos() {
        txtPlaca.clear();
        txtDataHoraSinistro.clear();
        txtCpfCnpjSegurado.clear();
        txtUsuarioRegistro.clear();
        txtValorSinistro.clear();
        if (!cmbTipoSinistro.getItems().isEmpty()) {
            cmbTipoSinistro.getSelectionModel().selectFirst();
        }

        txtPlaca.setStyle("");
        txtDataHoraSinistro.setStyle("");
        txtCpfCnpjSegurado.setStyle("");
        txtUsuarioRegistro.setStyle("");
        txtValorSinistro.setStyle("");
        cmbTipoSinistro.setStyle("");
        txtPlaca.requestFocus();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinWidth(600);
        alert.showAndWait();
    }

}