package br.edu.fecap.app.api;

import java.util.List;

import br.edu.fecap.app.model.Boleto;
import br.edu.fecap.app.model.Emprestimo;
import br.edu.fecap.app.model.Livro;
import br.edu.fecap.app.model.Usuario;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Endpoints para Usuários
    @GET("usuarios/{id}")
    Call<Usuario> getUsuario(@Path("id") int id);

    @PUT("usuarios/{id}")
    Call<Usuario> atualizarUsuario(@Path("id") int id, @Body Usuario usuario);

    // Endpoints para Livros
    @GET("livros")
    Call<List<Livro>> getLivros();

    @GET("livros/{id}")
    Call<Livro> getLivro(@Path("id") int id);

    // Endpoints para Empréstimos
    @GET("emprestimos")
    Call<List<Emprestimo>> getEmprestimosUsuario(@Query("idUsuario") int idUsuario);

    @POST("emprestimos")
    Call<Emprestimo> realizarEmprestimo(@Body Emprestimo emprestimo);

    @PUT("emprestimos/{id}")
    Call<Emprestimo> devolverLivro(@Path("id") int id, @Body Emprestimo emprestimo);

    // Endpoints para Boletos
    @GET("boletos")
    Call<List<Boleto>> getBoletosUsuario(@Query("idUsuario") int idUsuario);

    @GET("boletos/{id}")
    Call<Boleto> getBoleto(@Path("id") int id);

    @PUT("boletos/{id}/pagar")
    Call<Boleto> pagarBoleto(@Path("id") int id);
}