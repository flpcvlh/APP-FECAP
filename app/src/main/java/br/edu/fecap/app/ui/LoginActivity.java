package br.edu.fecap.app.ui;

import android.content.Intent;
import android.content.SharedPreferences;
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
import br.edu.fecap.app.model.Usuario;
import br.edu.fecap.app.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputEditText etSenha;
    private Button btnLogin;
    private Button btnCadastro;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar componentes
        etEmail = findViewById(R.id.etEmail);
        etSenha = findViewById(R.id.etSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnCadastro = findViewById(R.id.btnCadastro);

        // Configurar listener do botão de login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String senha = etSenha.getText().toString().trim();
                if (email.isEmpty()) {
                    etEmail.setError("Por favor, insira seu e-mail");
                    return;
                }

                if (senha.isEmpty()) {
                    etSenha.setError("Por favor, insira sua senha");
                    return;
                }

                // Realizar login em uma thread separada
                realizarLogin(email, senha);
            }
        });

        // Configurar listener do botão de cadastro
        btnCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir tela de cadastro
                Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
                startActivity(intent);
            }
        });
    }

    private void realizarLogin(String email, String senha) {
        // Mostrar progresso
        btnLogin.setEnabled(false);
        btnLogin.setText(R.string.entrando);

        // Executar em thread separada para evitar bloqueio da UI
        executor.execute(() -> {
            Connection connection = null;
            try {
                // Tentar obter conexão com o banco
                connection = DatabaseHelper.getInstance().getConnection();

                // Este código nunca será executado no modo offline,
                // mas é mantido para compatibilidade futura
                String query = "SELECT * FROM usuarios WHERE email = ? AND senha = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, email);
                statement.setString(2, senha);

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    // Criar objeto Usuario com dados do banco
                    Usuario usuario = new Usuario();
                    usuario.setId(resultSet.getInt("id"));
                    usuario.setNome(resultSet.getString("nome"));
                    usuario.setEmail(resultSet.getString("email"));
                    usuario.setMatricula(resultSet.getString("matricula"));

                    // Atualizar UI na thread principal
                    handler.post(() -> {
                        // Login bem-sucedido, abrir tela principal
                        SharedPreferences prefs = getSharedPreferences("FECAPApp", MODE_PRIVATE);
                        prefs.edit().putInt("USUARIO_ID", usuario.getId()).apply();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("USUARIO_ID", usuario.getId());
                        startActivity(intent);
                        finish(); // Finaliza a activity de login
                    });
                } else {
                    // Atualizar UI na thread principal
                    handler.post(() -> {
                        // Credenciais inválidas
                        Toast.makeText(LoginActivity.this,
                                "E-mail ou senha incorretos",
                                Toast.LENGTH_SHORT).show();
                        btnLogin.setEnabled(true);
                        btnLogin.setText(R.string.entrar);
                    });
                }
            } catch (SQLException e) {
                // Verificar se é a exceção do modo offline
                if ("MODO_OFFLINE".equals(e.getMessage())) {
                    // Credenciais de teste para modo offline
                    if (("teste@fecap.br".equals(email) && "101002".equals(senha)) ||
                            (email.endsWith("@fecap.br") && senha.length() >= 6)) {

                        // Criar um usuário simulado
                        Usuario usuarioSimulado = new Usuario();
                        usuarioSimulado.setId(1);
                        usuarioSimulado.setNome("Estudante Teste");
                        usuarioSimulado.setEmail(email);
                        usuarioSimulado.setMatricula("202500001");

                        // Atualizar UI na thread principal
                        handler.post(() -> {
                            // Salvar ID do usuário
                            SharedPreferences prefs = getSharedPreferences("FECAPApp", MODE_PRIVATE);
                            prefs.edit().putInt("USUARIO_ID", usuarioSimulado.getId()).apply();

                            // Mostrar toast de modo offline
                            Toast.makeText(LoginActivity.this,
                                    "Modo offline ativado",
                                    Toast.LENGTH_SHORT).show();

                            // Abrir tela principal
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("USUARIO_ID", usuarioSimulado.getId());
                            startActivity(intent);
                            finish();
                        });
                    } else {
                        // Credenciais inválidas no modo offline
                        handler.post(() -> {
                            Toast.makeText(LoginActivity.this,
                                    "E-mail ou senha incorretos",
                                    Toast.LENGTH_SHORT).show();
                            btnLogin.setEnabled(true);
                            btnLogin.setText(R.string.entrar);
                        });
                    }
                } else {
                    // Outros erros de SQL
                    e.printStackTrace();
                    // Atualizar UI na thread principal
                    handler.post(() -> {
                        Toast.makeText(LoginActivity.this,
                                "Erro ao conectar ao banco de dados: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        btnLogin.setEnabled(true);
                        btnLogin.setText(R.string.entrar);
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