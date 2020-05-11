package br.com.alura.estoque.retrofit.service;

import java.util.List;

import br.com.alura.estoque.model.Produto;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ProdutoService {

    @GET("produto") //adiciona a rota no baseUrl para a requisição
    Call<List<Produto>> buscaTodos();
}