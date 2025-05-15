package br.edu.fecap.app.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import br.edu.fecap.app.R;
import br.edu.fecap.app.ui.fragments.LivrosFragment;
import br.edu.fecap.app.ui.fragments.EmprestimosFragment;

public class BibliotecaActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private int usuarioId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biblioteca);

        // Obter ID do usuário
        usuarioId = getIntent().getIntExtra("USUARIO_ID", -1);

        // Configurar toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Inicializar componentes
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        // Configurar ViewPager com adapter
        BibliotecaPagerAdapter pagerAdapter = new BibliotecaPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Conectar TabLayout com ViewPager
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.livros_disponiveis);
                    break;
                case 1:
                    tab.setText(R.string.meus_emprestimos);
                    break;
            }
        }).attach();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Adapter para ViewPager
    private class BibliotecaPagerAdapter extends FragmentStateAdapter {

        public BibliotecaPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Criar fragmentos de acordo com a posição
            switch (position) {
                case 0:
                    return LivrosFragment.newInstance(usuarioId);
                case 1:
                    return EmprestimosFragment.newInstance(usuarioId);
                default:
                    return LivrosFragment.newInstance(usuarioId);
            }
        }

        @Override
        public int getItemCount() {
            return 2; // Número de abas
        }
    }
}