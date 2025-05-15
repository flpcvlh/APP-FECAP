package br.edu.fecap.app.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.edu.fecap.app.R;
import br.edu.fecap.app.api.ApiMock;
import br.edu.fecap.app.database.DatabaseHelper;
import br.edu.fecap.app.model.Emprestimo;
import br.edu.fecap.app.model.Livro;

public class LivrosFragment extends Fragment {

    private static final String ARG_USUARIO_ID = "usuarioId";

    private int usuarioId;
    private RecyclerView rvLivros;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private List<Livro> livros = new ArrayList<>();
    private LivrosAdapter adapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public LivrosFragment() {}

    public static LivrosFragment newInstance(int usuarioId) {
        LivrosFragment fragment = new LivrosFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USUARIO_ID, usuarioId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            usuarioId = getArguments().getInt(ARG_USUARIO_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_livros, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvLivros = view.findViewById(R.id.rvLivros);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvLivros.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LivrosAdapter(livros, this::reservarLivro);
        rvLivros.setAdapter(adapter);

        carregarLivros();
    }

    private void carregarLivros() {
        // Mostrar progresso
        progressBar.setVisibility(View.VISIBLE);
        rvLivros.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        // No modo offline, usar dados MockAPI diretamente
        handler.post(() -> {
            progressBar.setVisibility(View.GONE);

            // Usar dados mock para exibição
            livros.clear();
            livros.addAll(ApiMock.getLivrosMock());
            adapter.notifyDataSetChanged();

            if (livros.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvLivros.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvLivros.setVisibility(View.VISIBLE);
            }

            Toast.makeText(getContext(),
                    "Modo offline: Exibindo dados simulados",
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
            String query = "SELECT * FROM livros";
            PreparedStatement statement = connection.prepareStatement(query);

            // Executar consulta
            ResultSet resultSet = statement.executeQuery();

            // Processar resultados
            List<Livro> livrosDB = new ArrayList<>();
            while (resultSet.next()) {
                Livro livro = new Livro();
                livro.setId(resultSet.getInt("id"));
                livro.setTitulo(resultSet.getString("titulo"));
                livro.setAutor(resultSet.getString("autor"));
                livro.setDescricao(resultSet.getString("descricao"));
                livro.setDisponivel(resultSet.getBoolean("disponivel"));
                livrosDB.add(livro);
            }

            // Atualizar UI na thread principal
            handler.post(() -> {
                progressBar.setVisibility(View.GONE);

                if (livrosDB.isEmpty()) {
                    // Usar dados mock se banco estiver vazio
                    livros.clear();
                    livros.addAll(ApiMock.getLivrosMock());
                } else {
                    // Usar dados do banco
                    livros.clear();
                    livros.addAll(livrosDB);
                }

                adapter.notifyDataSetChanged();

                if (livros.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvLivros.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvLivros.setVisibility(View.VISIBLE);
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            // Atualizar UI na thread principal
            handler.post(() -> {
                progressBar.setVisibility(View.GONE);

                // Usar dados mock em caso de erro
                livros.clear();
                livros.addAll(ApiMock.getLivrosMock());
                adapter.notifyDataSetChanged();

                if (livros.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvLivros.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvLivros.setVisibility(View.VISIBLE);
                }

                Toast.makeText(getContext(),
                             "Erro ao carregar livros: " + e.getMessage(),
                             Toast.LENGTH_SHORT).show();
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

    private void reservarLivro(Livro livro) {
        // Verificar se livro está disponível
        if (!livro.isDisponivel()) {
            Toast.makeText(getContext(), "Este livro não está disponível para reserva", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar progresso
        progressBar.setVisibility(View.VISIBLE);

        // No modo offline, simular reserva
        handler.post(() -> {
            progressBar.setVisibility(View.GONE);

            // Atualizar objeto do livro
            livro.setDisponivel(false);
            adapter.notifyDataSetChanged();

            Toast.makeText(getContext(),
                    "Modo offline: Livro reservado com sucesso!",
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private static class LivrosAdapter extends RecyclerView.Adapter<LivrosAdapter.LivroViewHolder> {

        private final List<Livro> livros;
        private final OnReservarClickListener listener;

        public interface OnReservarClickListener {
            void onReservarClick(Livro livro);
        }

        public LivrosAdapter(List<Livro> livros, OnReservarClickListener listener) {
            this.livros = livros;
            this.listener = listener;
        }

        @NonNull
        @Override
        public LivroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_livro, parent, false);
            return new LivroViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LivroViewHolder holder, int position) {
            holder.bind(livros.get(position), listener);
        }

        @Override
        public int getItemCount() {
            return livros.size();
        }

        static class LivroViewHolder extends RecyclerView.ViewHolder {
            private final ImageView ivCapaLivro;
            private final TextView tvTituloLivro;
            private final TextView tvAutorLivro;
            private final TextView tvDescricaoLivro;
            private final TextView tvStatusLivro;
            private final Button btnReservar;

            public LivroViewHolder(@NonNull View itemView) {
                super(itemView);
                ivCapaLivro = itemView.findViewById(R.id.ivCapaLivro);
                tvTituloLivro = itemView.findViewById(R.id.tvTituloLivro);
                tvAutorLivro = itemView.findViewById(R.id.tvAutorLivro);
                tvDescricaoLivro = itemView.findViewById(R.id.tvDescricaoLivro);
                tvStatusLivro = itemView.findViewById(R.id.tvStatusLivro);
                btnReservar = itemView.findViewById(R.id.btnReservar);
            }

            public void bind(Livro livro, OnReservarClickListener listener) {
                tvTituloLivro.setText(livro.getTitulo());
                tvAutorLivro.setText(livro.getAutor());
                tvDescricaoLivro.setText(livro.getDescricao());

                if (livro.getImagemUrl() != null && !livro.getImagemUrl().isEmpty()) {
                    int resourceId = itemView.getContext().getResources().getIdentifier(
                            livro.getImagemUrl(), "drawable", itemView.getContext().getPackageName());

                    if (resourceId != 0) {
                        ivCapaLivro.setImageResource(resourceId);
                    } else {
                        ivCapaLivro.setImageResource(R.drawable.placeholder_book);
                    }
                } else {
                    ivCapaLivro.setImageResource(R.drawable.placeholder_book);
                }

                if (livro.isDisponivel()) {
                    tvStatusLivro.setText(R.string.disponivel);
                    tvStatusLivro.setTextColor(itemView.getContext().getColor(R.color.fecap_green));
                    btnReservar.setEnabled(true);
                } else {
                    tvStatusLivro.setText(R.string.indisponivel);
                    tvStatusLivro.setTextColor(itemView.getContext().getColor(R.color.gray));
                    btnReservar.setEnabled(false);
                }

                btnReservar.setOnClickListener(v -> listener.onReservarClick(livro));
            }
        }
    }
}
