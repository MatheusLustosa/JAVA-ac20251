package br.edu.cs.poo.ac.seguro.telas;

import br.edu.cs.poo.ac.seguro.mediators.ApoliceMediator;
import br.edu.cs.poo.ac.seguro.mediators.DadosVeiculo;
import br.edu.cs.poo.ac.seguro.mediators.RetornoInclusaoApolice;
import br.edu.cs.poo.ac.seguro.entidades.CategoriaVeiculo;
import br.edu.cs.poo.ac.seguro.entidades.Endereco;
import br.edu.cs.poo.ac.seguro.entidades.SeguradoEmpresa;
import br.edu.cs.poo.ac.seguro.entidades.SeguradoPessoa;
import br.edu.cs.poo.ac.seguro.daos.SeguradoEmpresaDAO;
import br.edu.cs.poo.ac.seguro.daos.SeguradoPessoaDAO;

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
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList; // Import for ArrayList
import java.util.Arrays;
import java.util.Comparator;
import java.util.List; // Import for List
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class TelaInclusaoApoliceFX extends Application {

    private ApoliceMediator mediator;

    private TextField txtCpfCnpj;
    private TextField txtPlaca;
    private TextField txtAno;
    private TextField txtValorMaximoSegurado;
    private ComboBox<CategoriaVeiculo> cmbCategoriaVeiculo;

    private Button btnIncluir;
    private Button btnLimpar;

    // Formatters para valor monetário e ano
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");

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
        txtCpfCnpj.setPromptText("CPF ou CNPJ");
        txtCpfCnpj.setMaxWidth(150);
        setupCpfCnpjMaskAndValidation(txtCpfCnpj); // Apply new CPF/CNPJ validation

        txtPlaca = new TextField();
        txtPlaca.setPromptText("Ex: ABC1234");
        txtPlaca.setMaxWidth(100);
        //setupPlateMaskAndValidation(txtPlaca);

        txtAno = new TextField();
        txtAno.setPromptText("Ex: 2023");
        txtAno.setMaxWidth(80);
        setupYearMask(txtAno);

        txtValorMaximoSegurado = new TextField();
        txtValorMaximoSegurado.setPromptText("Ex: 100000,00");
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

    // --- Máscaras e Validações ---

    private void setupCpfCnpjMaskAndValidation(TextField textField) {
        Pattern pattern = Pattern.compile("\\d*"); // Allow only digits
        UnaryOperator<Change> filter = c -> {
            String newText = c.getControlNewText();
            if (pattern.matcher(newText).matches() && newText.length() <= 14) { // Max 14 digits
                return c;
            } else {
                return null;
            }
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        textField.setTextFormatter(textFormatter);

        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // If focus is lost
                String text = textField.getText().trim();
                if (!text.isEmpty()) {
                    if (text.length() == 11 || text.length() == 14) {
                        textField.setStyle("");
                    } else {
                        textField.setStyle("-fx-border-color: red;");
                    }
                } else {
                    textField.setStyle(""); // Clear error style if empty
                }
            }
        });
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
        Pattern pattern = Pattern.compile("[0-9.,]*");
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
                        String cleanText = text.replace(".", "").replace(",", ".");
                        BigDecimal value = new BigDecimal(cleanText);
                        textField.setText(DECIMAL_FORMAT.format(value));
                        textField.setStyle("");
                    } catch (NumberFormatException e) {
                        textField.setStyle("-fx-border-color: red;");
                    }
                } else {
                    textField.setStyle("");
                }
            }
        });
    }

    private void setupPlateMaskAndValidation(TextField textField) {
        Pattern platePattern = Pattern.compile("[A-Z]{0,3}\\d{0,4}");
        UnaryOperator<Change> filter = c -> {
            String newText = c.getControlNewText().toUpperCase(); // Convert to uppercase immediately
            if (newText.length() > 7) {
                return null;
            }
            if (platePattern.matcher(newText).matches()) {
                c.setText(newText); // Set the text to uppercase
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

    // --- Lógica de Negócio e UI ---

    private void incluirApolice() {
        List<String> errosFormato = new ArrayList<>();

        // Validate CPF/CNPJ
        String cpfCnpj = txtCpfCnpj.getText().trim();
        if (cpfCnpj.isEmpty()) {
            errosFormato.add("CPF/CNPJ do segurado é obrigatório.");
            txtCpfCnpj.setStyle("-fx-border-color: red;");
        } else if (cpfCnpj.length() != 11 && cpfCnpj.length() != 14) {
            errosFormato.add("CPF/CNPJ inválido. Digite 11 dígitos para CPF ou 14 para CNPJ.");
            txtCpfCnpj.setStyle("-fx-border-color: red;");
        } else {
            txtCpfCnpj.setStyle("");
        }

        // Validate Placa
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

        // Validate Ano
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

        // Validate Valor Máximo Segurado
        BigDecimal valorMaximoSegurado = null;
        String valorMaximoSeguradoText = txtValorMaximoSegurado.getText().trim();
        if (valorMaximoSeguradoText.isEmpty()) {
            errosFormato.add("Valor Máximo Segurado é obrigatório.");
            txtValorMaximoSegurado.setStyle("-fx-border-color: red;");
        } else {
            try {
                String cleanValor = valorMaximoSeguradoText.replace(".", "").replace(",", ".");
                valorMaximoSegurado = new BigDecimal(cleanValor);
                txtValorMaximoSegurado.setStyle("");
            } catch (NumberFormatException e) {
                errosFormato.add("Valor Máximo Segurado inválido. Use apenas números, vírgula para centavos e ponto para milhares (opcional).");
                txtValorMaximoSegurado.setStyle("-fx-border-color: red;");
            }
        }

        // Check if TipoSinistro is selected (if it were a ComboBox validation)
        CategoriaVeiculo categoriaSelecionada = cmbCategoriaVeiculo.getSelectionModel().getSelectedItem();
        if (categoriaSelecionada == null) {
            errosFormato.add("Categoria do veículo é obrigatória.");
            cmbCategoriaVeiculo.setStyle("-fx-border-color: red;");
        } else {
            cmbCategoriaVeiculo.setStyle("");
        }


        // If there are format errors, display all of them and stop
        if (!errosFormato.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erro de Entrada", "Corrija os campos com formato inválido:\n" + String.join("\n", errosFormato));
            return;
        }

        // Proceed with business logic if no format errors
        try {
            int codigoCategoria = (categoriaSelecionada != null) ? categoriaSelecionada.getCodigo() : 0;
            DadosVeiculo dados = new DadosVeiculo(cpfCnpj, placa, ano, valorMaximoSegurado, codigoCategoria);
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
        txtCpfCnpj.requestFocus();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinWidth(1000); // Added for better readability of longer error messages
        alert.showAndWait();
    }

    public static void main(String[] args) {
        try {
            SeguradoPessoaDAO segPesDAO = new SeguradoPessoaDAO();
            SeguradoEmpresaDAO segEmpDAO = new SeguradoEmpresaDAO();

            Endereco endPessoa = new Endereco("Rua P", "11111-111", "10", "", "Brasil", "PE", "Recife");
            SeguradoPessoa pessoa = new SeguradoPessoa("Cliente Pessoa", endPessoa, LocalDate.of(1980, 5, 10), new BigDecimal("500.00"),"12345678909",1000);
            if (segPesDAO.buscar(pessoa.getIdUnico()) == null) {
                if (segPesDAO.incluir(pessoa)) {
                    System.out.println("Segurado Pessoa 12345678909 incluído.");
                } else {
                    System.out.println("Erro ao incluir Segurado Pessoa 12345678909.");
                }
            } else {
                System.out.println("Segurado Pessoa 12345678909 já existe.");
            }

            Endereco endEmpresa = new Endereco("Av. E", "22222-222", "200", "Sala 1", "Brasil", "SP", "São Paulo");
            SeguradoEmpresa empresa = new SeguradoEmpresa( "Empresa Teste", endEmpresa, LocalDate.of(2000, 1, 1), new BigDecimal("1000.00"), "11222333000144",1000,false);
            if (segEmpDAO.buscar(empresa.getIdUnico()) == null) {
                if (segEmpDAO.incluir(empresa)) {
                    System.out.println("Segurado Empresa 11222333000144 incluído.");
                } else {
                    System.out.println("Erro ao incluir Segurado Empresa 11222333000144.");
                }
            } else {
                System.out.println("Segurado Empresa 11222333000144 já existe.");
            }

        } catch (RuntimeException e) {
            System.err.println("Erro na inicialização dos dados de teste: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erro inesperado na inicialização: " + e.getMessage());
            e.printStackTrace();
        }

        launch(args);
    }
}