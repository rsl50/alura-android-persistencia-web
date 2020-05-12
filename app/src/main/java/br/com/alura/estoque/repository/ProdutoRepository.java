package br.com.alura.estoque.repository;

import android.os.AsyncTask;

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

public class ProdutoRepository {

    private final ProdutoDAO dao;
    private final ProdutoService service;

    public ProdutoRepository(ProdutoDAO dao) {
        this.dao = dao;
        this.service  = new EstoqueRetrofit().getProdutoService();
    }

    public void buscaProdutos(DadosCarregadosListener<List<Produto>> listener) {
        buscaProdutosInternos(listener);
    }

    private void buscaProdutosInternos(DadosCarregadosListener<List<Produto>> listener) {
        //task para leitura de produtos internamente
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
                    //carrega produtos internamente
                    listener.quandoCarregados(resultado);
                    buscapProdutosNaApi(listener);
                }).execute();
    }

    private void buscapProdutosNaApi(DadosCarregadosListener<List<Produto>> listener) {
        Call<List<Produto>> call = service.buscaTodos();

        //inicia task para carregar produtos online
        new BaseAsyncTask<>(() -> {
            try {
                Response<List<Produto>> resposta = call.execute();
                List<Produto> produtosNovos = resposta.body();
                dao.salva(produtosNovos);//faz com que produtos recebidos online sejam gravados no database interno para exibição offline
            } catch (IOException e){
                e.printStackTrace();
            }
            return dao.buscaTodos();//exibe os produtos usando o database interno
        }, listener::quandoCarregados)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);//evita que thread entre na fila de execução, criando uma nova thread
    }

    public void salva(Produto produto, DadosCarregadosListener<Produto> listener) {
        Call<Produto> call = service.salva(produto);
        //execução assíncrona da call, as threads são executadas em paralelo
        call.enqueue(new Callback<Produto>() {
            @Override
            public void onResponse(Call<Produto> call, Response<Produto> response) {
                Produto produtoSalvo = response.body();

                new BaseAsyncTask<>(() -> {
                    long id = dao.salva(produtoSalvo);
                    return dao.buscaProduto(id);
                }, listener::quandoCarregados)
                    .execute();
            }

            @Override
            public void onFailure(Call<Produto> call, Throwable t) {

            }
        });


    }

    public interface DadosCarregadosListener <T>{
        void quandoCarregados(T produtos);
    }
}