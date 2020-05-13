package br.com.alura.estoque.retrofit.service;

import java.util.List;

import br.com.alura.estoque.model.Produto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ProdutoService {

    @GET("produto") //adiciona a rota no baseUrl para a requisição
    Call<List<Produto>> buscaTodos();

    @POST("produto")
    Call<Produto> salva(@Body Produto produto);

    @PUT("produto/{id}") //copia o @Path em {id} para o endereço incluir o id do produto
    Call<Produto> edita(@Path("id")long id, @Body Produto produto);
}
