package br.edu.fecap.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.edu.fecap.app.R;
import br.edu.fecap.app.database.DatabaseHelper;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText etNome;
    private TextInputEditText etEmail;
    private TextInputEditText etMatricula;
    private TextInputEditText etSenha;
    private TextInputEditText etConfirmarSenha;
    private Button btnCadastrar;
    private Button btnVoltar;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        // Inicializar componentes
        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etMatricula = findViewById(R.id.etMatricula);
        etSenha = findViewById(R.id.etSenha);
        etConfirmarSenha = findViewById(R.id.etConfirmarSenha);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnVoltar = findViewById(R.id.btnVoltar);

        // Configurar listener do botão de cadastro
        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarFormulario()) {
                    // Realizar cadastro
                    realizarCadastro();
                }
            }
        });

        // Configurar listener do botão voltar
        btnVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Voltar para a tela anterior
            }
        });
    }

    private boolean validarFormulario() {
        String nome = etNome.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String matricula = etMatricula.getText().toString().trim();
        String senha = etSenha.getText().toString();
        String confirmarSenha = etConfirmarSenha.getText().toString();

        // Validar campos vazios
        if (nome.isEmpty()) {
            etNome.setError("Por favor, insira seu nome");
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("Por favor, insira seu e-mail");
            return false;
        }

        if (matricula.isEmpty()) {
            etMatricula.setError("Por favor, insira sua matrícula");
            return false;
        }

        if (senha.isEmpty()) {
            etSenha.setError("Por favor, insira uma senha");
            return false;
        }

        if (confirmarSenha.isEmpty()) {
            etConfirmarSenha.setError("Por favor, confirme sua senha");
            return false;
        }

        // Validar formato de e-mail
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Insira um e-mail válido");
            return false;
        }

        // Validar se é e-mail institucional (termina com @fecap.br)
        if (!email.endsWith("@fecap.br")) {
            etEmail.setError("Insira um e-mail institucional (@fecap.br)");
            return false;
        }

        // Validar se senhas conferem
        if (!senha.equals(confirmarSenha)) {
            etConfirmarSenha.setError("As senhas não coincidem");
            return false;
        }

        // Validar tamanho da senha
        if (senha.length() < 6) {
            etSenha.setError("A senha deve ter pelo menos 6 caracteres");
            return false;
        }

        return true;
    }

    private void realizarCadastro() {
        // Obter dados do formulário
        String nome = etNome.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String matricula = etMatricula.getText().toString().trim();
        String senha = etSenha.getText().toString();

        // Desabilitar botão de cadastro
        btnCadastrar.setEnabled(false);
        btnCadastrar.setText(R.string.cadastrando);

        // Executar em thread separada
        executor.execute(() -> {
            Connection connection = null;
            try {
                // Obter conexão com o banco
                connection = DatabaseHelper.getInstance().getConnection();

                // Código original - mantido para compatibilidade futura
                // Verificar se o e-mail já existe
                String checkQuery = "SELECT * FROM usuarios WHERE email = ?";
                PreparedStatement checkStatement = connection.prepareStatement(checkQuery);
                checkStatement.setString(1, email);
                ResultSet resultSet = checkStatement.executeQuery();

                if (resultSet.next()) {
                    // E-mail já cadastrado
                    handler.post(() -> {
                        etEmail.setError("Este e-mail já está cadastrado");
                        btnCadastrar.setEnabled(true);
                        btnCadastrar.setText(R.string.cadastrar);
                    });
                    return;
                }

                // Verificar se a matrícula já existe
                checkQuery = "SELECT * FROM usuarios WHERE matricula = ?";
                checkStatement = connection.prepareStatement(checkQuery);
                checkStatement.setString(1, matricula);
                resultSet = checkStatement.executeQuery();

                if (resultSet.next()) {
                    // Matrícula já cadastrada
                    handler.post(() -> {
                        etMatricula.setError("Esta matrícula já está cadastrada");
                        btnCadastrar.setEnabled(true);
                        btnCadastrar.setText(R.string.cadastrar);
                    });
                    return;
                }

                // Inserir novo usuário
                String insertQuery = "INSERT INTO usuarios (nome, email, matricula, senha) VALUES (?, ?, ?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
                insertStatement.setString(1, nome);
                insertStatement.setString(2, email);
                insertStatement.setString(3, matricula);
                insertStatement.setString(4, senha);

                int rowsAffected = insertStatement.executeUpdate();

                // Verificar se inserção foi bem-sucedida
                if (rowsAffected > 0) {
                    // Cadastro bem-sucedido
                    handler.post(() -> {
                        Toast.makeText(CadastroActivity.this,
                                "Cadastro realizado com sucesso!",
                                Toast.LENGTH_SHORT).show();
                        finish(); // Voltar para tela de login
                    });
                } else {
                    // Falha ao inserir
                    handler.post(() -> {
                        Toast.makeText(CadastroActivity.this,
                                "Falha ao realizar cadastro. Tente novamente.",
                                Toast.LENGTH_SHORT).show();
                        btnCadastrar.setEnabled(true);
                        btnCadastrar.setText(R.string.cadastrar);
                    });
                }

            } catch (SQLException e) {
                // Verificar se é a exceção do modo offline
                if ("MODO_OFFLINE".equals(e.getMessage())) {
                    // Simular cadastro bem-sucedido no modo offline
                    handler.post(() -> {
                        Toast.makeText(CadastroActivity.this,
                                "Modo offline: Cadastro simulado com sucesso!",
                                Toast.LENGTH_SHORT).show();
                        finish(); // Voltar para tela de login
                    });
                } else {
                    e.printStackTrace();
                    // Atualizar UI na thread principal
                    handler.post(() -> {
                        Toast.makeText(CadastroActivity.this,
                                "Erro ao conectar ao banco de dados: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        btnCadastrar.setEnabled(true);
                        btnCadastrar.setText(R.string.cadastrar);
                    });
                }
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}