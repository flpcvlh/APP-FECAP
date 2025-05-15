package br.edu.fecap.app.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.edu.fecap.app.R;
import br.edu.fecap.app.api.ApiMock;
import br.edu.fecap.app.database.DatabaseHelper;
import br.edu.fecap.app.model.Emprestimo;
import br.edu.fecap.app.model.Livro;

public class EmprestimosFragment extends Fragment {

    private static final String ARG_USUARIO_ID = "usuarioId";

    private int usuarioId;
    private RecyclerView rvEmprestimos;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private List<Emprestimo> emprestimos = new ArrayList<>();
    private Map<Integer, Livro> livrosMap = new HashMap<>();
    private EmprestimosAdapter adapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public EmprestimosFragment() {}

    public static EmprestimosFragment newInstance(int usuarioId) {
        EmprestimosFragment fragment = new EmprestimosFragment();
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
        return inflater.inflate(R.layout.fragment_emprestimos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvEmprestimos = view.findViewById(R.id.rvEmprestimos);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        rvEmprestimos.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EmprestimosAdapter(emprestimos, livrosMap, this::devolverLivro);
        rvEmprestimos.setAdapter(adapter);

        carregarEmprestimos();
    }

    private void carregarEmprestimos() {
        // Mostrar progresso
        progressBar.setVisibility(View.VISIBLE);
        rvEmprestimos.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        // No modo offline, usar dados MockAPI diretamente
        handler.post(() -> {
            progressBar.setVisibility(View.GONE);

            // Usar dados mock para exibição
            emprestimos.clear();
            emprestimos.addAll(ApiMock.getEmprestimosMock(usuarioId));

            // Preencher o mapa de livros com dados mock
            for (Livro livro : ApiMock.getLivrosMock()) {
                livrosMap.put(livro.getId(), livro);
            }

            adapter.notifyDataSetChanged();

            if (emprestimos.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvEmprestimos.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvEmprestimos.setVisibility(View.VISIBLE);
            }
        });
    }

    private void devolverLivro(Emprestimo emprestimo) {
        // Verificar se empréstimo está ativo
        if (!"ATIVO".equals(emprestimo.getStatus())) {
            Toast.makeText(getContext(), "Este livro já foi devolvido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar progresso
        progressBar.setVisibility(View.VISIBLE);

        // No modo offline, simular devolução
        handler.post(() -> {
            progressBar.setVisibility(View.GONE);

            // Atualizar objetos
            emprestimo.setStatus("DEVOLVIDO");
            emprestimo.setDataDevolucaoEfetiva(new Date());

            Livro livro = livrosMap.get(emprestimo.getIdLivro());
            if (livro != null) {
                livro.setDisponivel(true);
            }

            adapter.notifyDataSetChanged();

            Toast.makeText(getContext(),
                    "Modo offline: Livro devolvido com sucesso!",
                    Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private static class EmprestimosAdapter extends RecyclerView.Adapter<EmprestimosAdapter.EmprestimoViewHolder> {

        private final List<Emprestimo> emprestimos;
        private final Map<Integer, Livro> livrosMap;
        private final OnDevolverClickListener listener;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        public interface OnDevolverClickListener {
            void onDevolverClick(Emprestimo emprestimo);
        }

        public EmprestimosAdapter(List<Emprestimo> emprestimos, Map<Integer, Livro> livrosMap, OnDevolverClickListener listener) {
            this.emprestimos = emprestimos;
            this.livrosMap = livrosMap;
            this.listener = listener;
        }

        @NonNull
        @Override
        public EmprestimoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_emprestimo, parent, false);
            return new EmprestimoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EmprestimoViewHolder holder, int position) {
            Emprestimo emprestimo = emprestimos.get(position);
            Livro livro = livrosMap.get(emprestimo.getIdLivro());
            holder.bind(emprestimo, livro, listener, dateFormat);
        }

        @Override
        public int getItemCount() {
            return emprestimos.size();
        }

        static class EmprestimoViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvTituloLivro;
            private final TextView tvAutorLivro;
            private final TextView tvDataEmprestimo;
            private final TextView tvDataDevolucaoPrevista;
            private final TextView tvStatus;
            private final Button btnDevolver;

            public EmprestimoViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTituloLivro = itemView.findViewById(R.id.tvTituloLivro);
                tvAutorLivro = itemView.findViewById(R.id.tvAutorLivro);
                tvDataEmprestimo = itemView.findViewById(R.id.tvDataEmprestimo);
                tvDataDevolucaoPrevista = itemView.findViewById(R.id.tvDataDevolucaoPrevista);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btnDevolver = itemView.findViewById(R.id.btnDevolver);
            }

            public void bind(Emprestimo emprestimo, Livro livro, OnDevolverClickListener listener, SimpleDateFormat dateFormat) {
                if (livro != null) {
                    tvTituloLivro.setText(livro.getTitulo());
                    tvAutorLivro.setText(livro.getAutor());
                } else {
                    tvTituloLivro.setText("Livro ID: " + emprestimo.getIdLivro());
                    tvAutorLivro.setText("Autor desconhecido");
                }

                tvDataEmprestimo.setText("Empréstimo: " + dateFormat.format(emprestimo.getDataEmprestimo()));
                tvDataDevolucaoPrevista.setText("Devolução: " + dateFormat.format(emprestimo.getDataDevolucaoPrevista()));

                String status = emprestimo.getStatus();
                tvStatus.setText(status);

                if ("ATIVO".equals(status)) {
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.fecap_green));
                    btnDevolver.setVisibility(View.VISIBLE);
                    btnDevolver.setEnabled(true);
                } else if ("ATRASADO".equals(status)) {
                    tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                    btnDevolver.setVisibility(View.VISIBLE);
                    btnDevolver.setEnabled(true);
                } else {
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.gray));
                    btnDevolver.setVisibility(View.GONE);
                }

                btnDevolver.setOnClickListener(v -> listener.onDevolverClick(emprestimo));
            }
        }
    }
}
