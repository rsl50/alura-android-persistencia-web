package br.com.alura.estoque.retrofit.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class CallbackComRetorno<T> implements Callback<T> {

    private final RespostaCallback<T> callback;

    public CallbackComRetorno(RespostaCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    @EverythingIsNonNull
    public void onResponse(Call<T> call, Response<T> response) {
        //verifica se resposta está Ok e produto não nulo antes de salvar
        if (response.isSuccessful()) {
            T resultado = response.body();
            if (resultado != null) {
                callback.quandoSucesso(resultado);
            }
        } else {
            callback.quandoFalha("Resposta não sucedida");
        }
    }

    //Para saber mais - Mais cuidados com a resposta do Retrofit
    //https://medium.com/@tsaha.cse/advanced-retrofit2-part-1-network-error-handling-response-caching-77483cf68620
    @Override
    @EverythingIsNonNull
    public void onFailure(Call call, Throwable t) {
        callback.quandoFalha("Falha de comunicação: " + t.getMessage());
    }

    public interface RespostaCallback <T> {
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }
}
