package br.edu.fecap.app.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.edu.fecap.app.R;
import br.edu.fecap.app.api.ApiMock;
import br.edu.fecap.app.database.DatabaseHelper;
import br.edu.fecap.app.model.Boleto;

public class BoletosActivity extends AppCompatActivity {

    private RecyclerView rvBoletos;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private int usuarioId;
    private List<Boleto> boletos = new ArrayList<>();
    private BoletosAdapter adapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boletos);

        // Obter ID do usuário
        usuarioId = getIntent().getIntExtra("USUARIO_ID", -1);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Inicializar componentes
        rvBoletos = findViewById(R.id.rvBoletos);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Configurar RecyclerView
        rvBoletos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BoletosAdapter(boletos, this::pagarBoleto);
        rvBoletos.setAdapter(adapter);

        // Carregar boletos
        carregarBoletos();
    }

    private void carregarBoletos() {
        // Mostrar progresso
        progressBar.setVisibility(View.VISIBLE);
        rvBoletos.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        // No modo offline, usar dados MockAPI diretamente
        handler.post(() -> {
            progressBar.setVisibility(View.GONE);

            // Usar dados mock para exibição
            boletos.clear();
            boletos.addAll(ApiMock.getBoletosMock(usuarioId));
            adapter.notifyDataSetChanged();

            if (boletos.isEmpty()) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvBoletos.setVisibility(View.GONE);
            } else {
                tvEmpty.setVisibility(View.GONE);
                rvBoletos.setVisibility(View.VISIBLE);
            }
        });
    }

    private void pagarBoleto(Boleto boleto) {
        // Verificar se boleto está pendente
        if (!"PENDENTE".equals(boleto.getStatus())) {
            Toast.makeText(this, "Este boleto já foi pago", Toast.LENGTH_SHORT).show();
            return;
        }

        // Confirmar pagamento
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Pagamento");
        builder.setMessage("Deseja efetuar o pagamento da mensalidade de " +
                getNomeMes(boleto.getMes()) + "/" + boleto.getAno() +
                " no valor de " + NumberFormat.getCurrencyInstance().format(boleto.getValor()) + "?");

        builder.setPositiveButton("Pagar", (dialog, which) -> {
            // Mostrar progresso
            progressBar.setVisibility(View.VISIBLE);

            // No modo offline, simular pagamento
            handler.post(() -> {
                progressBar.setVisibility(View.GONE);

                // Atualizar objeto boleto
                boleto.setStatus("PAGO");
                boleto.setDataPagamento(new Date());

                adapter.notifyDataSetChanged();

                // Mostrar confirmação de pagamento
                AlertDialog.Builder successBuilder = new AlertDialog.Builder(this);
                successBuilder.setTitle("Pagamento Realizado");
                successBuilder.setMessage("Modo offline: Seu pagamento foi processado com sucesso!");
                successBuilder.setPositiveButton("OK", null);
                successBuilder.show();
            });
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private String getNomeMes(int mes) {
        String[] meses = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        return meses[mes - 1];
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

    // Adapter para RecyclerView de boletos
    private static class BoletosAdapter extends RecyclerView.Adapter<BoletosAdapter.BoletoViewHolder> {

        private final List<Boleto> boletos;
        private final OnPagarClickListener listener;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        public interface OnPagarClickListener {
            void onPagarClick(Boleto boleto);
        }

        public BoletosAdapter(List<Boleto> boletos, OnPagarClickListener listener) {
            this.boletos = boletos;
            this.listener = listener;
        }

        @NonNull
        @Override
        public BoletoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.item_boleto, null);
            // Configurar largura total
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            view.setLayoutParams(params);
            return new BoletoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BoletoViewHolder holder, int position) {
            Boleto boleto = boletos.get(position);
            holder.bind(boleto, listener, dateFormat, currencyFormat);
        }

        @Override
        public int getItemCount() {return boletos.size();
        }

        static class BoletoViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvMesAno;
            private final TextView tvValor;
            private final TextView tvVencimento;
            private final TextView tvStatus;
            private final Button btnPagar;

            public BoletoViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMesAno = itemView.findViewById(R.id.tvMesAno);
                tvValor = itemView.findViewById(R.id.tvValor);
                tvVencimento = itemView.findViewById(R.id.tvVencimento);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btnPagar = itemView.findViewById(R.id.btnPagar);
            }

            public void bind(Boleto boleto, OnPagarClickListener listener, SimpleDateFormat dateFormat, NumberFormat currencyFormat) {
                // Mes/Ano
                String[] meses = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
                tvMesAno.setText(meses[boleto.getMes() - 1] + "/" + boleto.getAno());

                // Valor
                tvValor.setText(currencyFormat.format(boleto.getValor()));

                // Data de vencimento
                tvVencimento.setText("Vencimento: " + dateFormat.format(boleto.getDataVencimento()));

                // Status
                String status = boleto.getStatus();
                tvStatus.setText(status);

                if ("PENDENTE".equals(status)) {
                    // Verificar se está vencido
                    Calendar hoje = Calendar.getInstance();
                    Calendar vencimento = Calendar.getInstance();
                    vencimento.setTime(boleto.getDataVencimento());

                    if (hoje.after(vencimento)) {
                        tvStatus.setText("VENCIDO");
                        tvStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                    } else {
                        tvStatus.setTextColor(itemView.getContext().getColor(R.color.fecap_green));
                    }

                    btnPagar.setVisibility(View.VISIBLE);
                    btnPagar.setEnabled(true);
                } else {
                    tvStatus.setTextColor(itemView.getContext().getColor(R.color.gray));
                    btnPagar.setVisibility(View.GONE);
                }

                btnPagar.setOnClickListener(v -> listener.onPagarClick(boleto));
            }
        }
    }
}