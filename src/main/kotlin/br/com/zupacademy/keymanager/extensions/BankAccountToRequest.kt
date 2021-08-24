package br.com.zupacademy.keymanager.extensions

import br.com.zupacademy.keymanager.TipoConta
import br.com.zupacademy.keymanager.clients.dto.ITAUResponse
import br.com.zupacademy.keymanager.model.BankAccount
import io.micronaut.http.HttpResponse

fun bankAccountToRequest(tipoConta: TipoConta, ITAUResponse: HttpResponse<ITAUResponse>): BankAccount? {
    return when (tipoConta) {
        TipoConta.CONTA_CORRENTE -> BankAccount(
            ITAUResponse.body()!!.agencia, ITAUResponse.body()!!.numero, "CACC"
        )
        TipoConta.CONTA_POUPANCA -> BankAccount(
            ITAUResponse.body()!!.agencia, ITAUResponse.body()!!.numero, "SVGS"
        )
        else -> null
    }
}