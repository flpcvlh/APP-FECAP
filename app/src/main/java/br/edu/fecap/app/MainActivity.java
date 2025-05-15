package br.edu.fecap.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.edu.fecap.app.database.DatabaseHelper;
import br.edu.fecap.app.model.Usuario;
import br.edu.fecap.app.ui.BibliotecaActivity;
import br.edu.fecap.app.ui.BoletosActivity;
import br.edu.fecap.app.ui.LoginActivity;
import br.edu.fecap.app.ui.PerfilActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvNomeUsuario;
    private CardView cardBiblioteca;
    private CardView cardBoletos;
    private CardView cardPerfil;
    private int usuarioId;
    private Usuario usuarioLogado;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inicializar componentes
        tvNomeUsuario = findViewById(R.id.tvNomeUsuario);
        cardBiblioteca = findViewById(R.id.cardBiblioteca);
        cardBoletos = findViewById(R.id.cardBoletos);
        cardPerfil = findViewById(R.id.cardPerfil);

        // Verificar usuário logado
        SharedPreferences prefs = getSharedPreferences("FECAPApp", MODE_PRIVATE);
        usuarioId = prefs.getInt("USUARIO_ID", -1);

        // Se não houver usuário logado, redirecionar para tela de login
        if (usuarioId == -1) {
            // Verificar se foi passado pela Intent
            usuarioId = getIntent().getIntExtra("USUARIO_ID", -1);

            if (usuarioId == -1) {
                redirecionarParaLogin();
                return; // Importante: pare a execução aqui para evitar erros
            } else {
                // Salvar usuário logado
                prefs.edit().putInt("USUARIO_ID", usuarioId).apply();
            }
        }

        // Carregar dados do usuário
        carregarDadosUsuario();

        // Configurar click listeners
        configurarClickListeners();
    }

    private void configurarClickListeners() {
        // Biblioteca
        cardBiblioteca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BibliotecaActivity.class);
                intent.putExtra("USUARIO_ID", usuarioId);
                startActivity(intent);
            }
        });

        // Boletos
        cardBoletos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BoletosActivity.class);
                intent.putExtra("USUARIO_ID", usuarioId);
                startActivity(intent);
            }
        });

        // Perfil
        cardPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PerfilActivity.class);
                intent.putExtra("USUARIO_ID", usuarioId);
                startActivity(intent);
            }
        });
    }

    private void carregarDadosUsuario() {
        // Usar dados simulados para o modo offline
        handler.post(() -> {
            // Simular usuário para testes
            usuarioLogado = new Usuario();
            usuarioLogado.setId(usuarioId > 0 ? usuarioId : 1);
            usuarioLogado.setNome("Estudante FECAP");
            usuarioLogado.setEmail("estudante@fecap.br");
            usuarioLogado.setMatricula("202500001");

            // Atualizar nome do usuário na interface
            tvNomeUsuario.setText(usuarioLogado.getNome());

            Toast.makeText(MainActivity.this,
                    "Modo offline ativado",
                    Toast.LENGTH_SHORT).show();
        });

    /*
    // Código original - mantido para referência futura
    executor.execute(() -> {
        Connection connection = null;
        try {
            // Obter conexão com o banco
            connection = DatabaseHelper.getInstance().getConnection();

            // Preparar consulta SQL
            String query = "SELECT * FROM usuarios WHERE id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, usuarioId);

            // Executar consulta
            ResultSet resultSet = statement.executeQuery();

            // Verificar se encontrou o usuário
            if (resultSet.next()) {
                // Criar objeto Usuario com dados do banco
                usuarioLogado = new Usuario();
                usuarioLogado.setId(resultSet.getInt("id"));
                usuarioLogado.setNome(resultSet.getString("nome"));
                usuarioLogado.setEmail(resultSet.getString("email"));
                usuarioLogado.setMatricula(resultSet.getString("matricula"));

                // Atualizar UI na thread principal
                handler.post(() -> {
                    // Atualizar nome do usuário na interface
                    tvNomeUsuario.setText(usuarioLogado.getNome());
                });
            } else {
                // Usuário não encontrado, redirecionar para login
                handler.post(this::redirecionarParaLogin);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Atualizar UI na thread principal
            handler.post(() -> {
                Toast.makeText(MainActivity.this,
                              "Erro ao carregar dados do usuário: " + e.getMessage(),
                              Toast.LENGTH_LONG).show();
            });
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
    */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sair) {
            // Limpar preferências e sair
            SharedPreferences prefs = getSharedPreferences("FECAPApp", MODE_PRIVATE);
            prefs.edit().clear().apply();
            redirecionarParaLogin();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void redirecionarParaLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}