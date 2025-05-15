package br.edu.fecap.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

public class PerfilActivity extends AppCompatActivity {

    private TextView tvNomeUsuario;
    private TextView tvMatricula;
    private TextInputEditText etNome;
    private TextInputEditText etEmail;
    private TextInputEditText etSenhaAtual;
    private TextInputEditText etNovaSenha;
    private TextInputEditText etConfirmarSenha;
    private Button btnSalvar;
    private ProgressBar progressBar;

    private int usuarioId;
    private Usuario usuario;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        // Obter ID do usuário
        usuarioId = getIntent().getIntExtra("USUARIO_ID", -1);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Inicializar componentes
        tvNomeUsuario = findViewById(R.id.tvNomeUsuario);
        tvMatricula = findViewById(R.id.tvMatricula);
        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
        etSenhaAtual = findViewById(R.id.etSenhaAtual);
        etNovaSenha = findViewById(R.id.etNovaSenha);
        etConfirmarSenha = findViewById(R.id.etConfirmarSenha);
        btnSalvar = findViewById(R.id.btnSalvar);
        progressBar = findViewById(R.id.progressBar);

        // Configurar listener do botão salvar
        btnSalvar.setOnClickListener(v -> {
            if (validarFormulario()) {
                salvarAlteracoes();
            }
        });

        // Carregar dados do usuário
        carregarDadosUsuario();
    }

    private void carregarDadosUsuario() {
        // Mostrar progresso
        progressBar.setVisibility(View.VISIBLE);

        // No modo offline, usar dados simulados
        handler.post(() -> {
            progressBar.setVisibility(View.GONE);

            // Criar objeto de usuário simulado
            usuario = new Usuario();
            usuario.setId(usuarioId);
            usuario.setNome("Estudante FECAP");
            usuario.setEmail("estudante@fecap.br");
            usuario.setMatricula("202500001");
            usuario.setSenha("123456"); // Representação da senha armazenada

            // Preencher campos com dados do usuário
            tvNomeUsuario.setText(usuario.getNome());
            tvMatricula.setText("Matrícula: " + usuario.getMatricula());
            etNome.setText(usuario.getNome());
            etEmail.setText(usuario.getEmail());

            Toast.makeText(PerfilActivity.this,
                    "Modo offline: Exibindo perfil simulado",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validarFormulario() {
        String nome = etNome.getText().toString().trim();
        String senhaAtual = etSenhaAtual.getText().toString();
        String novaSenha = etNovaSenha.getText().toString();
        String confirmarSenha = etConfirmarSenha.getText().toString();

        // Validar nome
        if (nome.isEmpty()) {
            etNome.setError("Por favor, insira seu nome");
            return false;
        }

        // Se algum campo de senha estiver preenchido, todos devem estar
        boolean algumCampoSenhaPreenchido = !senhaAtual.isEmpty() || !novaSenha.isEmpty() || !confirmarSenha.isEmpty();
        boolean todosCamposSenhaPreenchidos = !senhaAtual.isEmpty() && !novaSenha.isEmpty() && !confirmarSenha.isEmpty();

        if (algumCampoSenhaPreenchido && !todosCamposSenhaPreenchidos) {
            if (senhaAtual.isEmpty()) {
                etSenhaAtual.setError("Por favor, insira sua senha atual");
            }
            if (novaSenha.isEmpty()) {
                etNovaSenha.setError("Por favor, insira a nova senha");
            }
            if (confirmarSenha.isEmpty()) {
                etConfirmarSenha.setError("Por favor, confirme a nova senha");
            }
            return false;
        }

        // Validar se a senha atual está correta (apenas se estiver alterando a senha)
        if (todosCamposSenhaPreenchidos && !senhaAtual.equals(usuario.getSenha())) {
            etSenhaAtual.setError("Senha atual incorreta");
            return false;
        }

        // Validar se as novas senhas conferem
        if (todosCamposSenhaPreenchidos && !novaSenha.equals(confirmarSenha)) {
            etConfirmarSenha.setError("As senhas não coincidem");
            return false;
        }

        // Validar tamanho da nova senha
        if (todosCamposSenhaPreenchidos && novaSenha.length() < 6) {
            etNovaSenha.setError("A senha deve ter pelo menos 6 caracteres");
            return false;
        }

        return true;
    }

    private void salvarAlteracoes() {
        // Obter valores dos campos
        String nome = etNome.getText().toString().trim();
        String senhaAtual = etSenhaAtual.getText().toString();
        String novaSenha = etNovaSenha.getText().toString();

        // Verificar se houve alterações
        boolean alterouNome = !nome.equals(usuario.getNome());
        boolean alterouSenha = !senhaAtual.isEmpty();

        if (!alterouNome && !alterouSenha) {
            Toast.makeText(this, "Nenhuma alteração realizada", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar progresso
        progressBar.setVisibility(View.VISIBLE);
        btnSalvar.setEnabled(false);

        // No modo offline, simular salvamento
        handler.post(() -> {
            progressBar.setVisibility(View.GONE);
            btnSalvar.setEnabled(true);

            // Atualizar objeto do usuário
            if (alterouNome) {
                usuario.setNome(nome);
            }

            if (alterouSenha) {
                usuario.setSenha(novaSenha);
            }

            // Atualizar interface
            tvNomeUsuario.setText(usuario.getNome());

            // Limpar campos de senha
            etSenhaAtual.setText("");
            etNovaSenha.setText("");
            etConfirmarSenha.setText("");

            Toast.makeText(PerfilActivity.this,
                    "Modo offline: Alterações salvas com sucesso",
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}