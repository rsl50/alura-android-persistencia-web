package br.com.alura.estoque.repository;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class ProdutoRepository {

    private final ProdutoDAO dao;
    private final ProdutoService service;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
        this.service  = new EstoqueRetrofit().getProdutoService();
    }

    public void buscaProdutos(DadosCarregadosCallback<List<Produto>> callback) {
        buscaProdutosInternos(callback);
    }

    private void buscaProdutosInternos(DadosCarregadosCallback<List<Produto>> callback) {
        //task para leitura de produtos internamente
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                    //carrega produtos internamente
                    callback.quandoSucesso(resultado);
                    buscapProdutosNaApi(callback);
                }).execute();
    }

    private void buscapProdutosNaApi(DadosCarregadosCallback<List<Produto>> callback) {
        Call<List<Produto>> call = service.buscaTodos();

        call.enqueue(new Callback<List<Produto>>() {
            //Sempre que algo é executado no onResponse ou OnFailure, a execução é na UI Thread, por isso se usa o AsyncTask para não travar ela
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<List<Produto>> call, Response<List<Produto>> response) {
                if (response.isSuccessful()){
                    List<Produto> produtosNovos = response.body();
                    if (produtosNovos != null) {
                        atualizaInterno(produtosNovos, callback);
                    }
                } else {
                    callback.quandoFalha("Resposta não sucedida");
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<List<Produto>> call, Throwable t) {
                callback.quandoFalha("Falha de comunicação: " + t.getMessage());
            }
        });
    }

    private void atualizaInterno(List<Produto> produtos, DadosCarregadosCallback<List<Produto>> callback) {
        new BaseAsyncTask<>(() -> {
            dao.salva(produtos);
            return dao.buscaTodos();
        }, callback::quandoSucesso)
                .execute();
    }

    public void salva(Produto produto, DadosCarregadosCallback<Produto> callback) {
        salvaNaApi(produto, callback);
    }

    private void salvaNaApi(Produto produto, DadosCarregadosCallback<Produto> callback) {
        Call<Produto> call = service.salva(produto);
        //execução assíncrona da call, as threads são executadas em paralelo
        call.enqueue(new Callback<Produto>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                //verifica se resposta está Ok e produto não nulo antes de salvar
                if (response.isSuccessful()){
                    Produto produtoSalvo = response.body();
                    if (produtoSalvo != null) {
                        salvaInterno(produtoSalvo, callback);
                    }
                } else {
                    callback.quandoFalha("Resposta não sucedida");
                }
            }
            //Para saber mais - Mais cuidados com a resposta do Retrofit
            //https://medium.com/@tsaha.cse/advanced-retrofit2-part-1-network-error-handling-response-caching-77483cf68620
            @Override
            @EverythingIsNonNull
            public void onFailure(Call<Produto> call, Throwable t) {
                callback.quandoFalha("Falha de comunicação: " + t.getMessage());
            }
        });
    }

    private void salvaInterno(Produto produto, DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produto);
            return dao.buscaProduto(id);
        }, callback::quandoSucesso)
                .execute();
    }


    public interface DadosCarregadosCallback <T>{
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }
}
