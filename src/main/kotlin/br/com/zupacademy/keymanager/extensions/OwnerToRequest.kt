package br.com.zupacademy.keymanager.extensions

import br.com.zupacademy.keymanager.TipoCliente
import br.com.zupacademy.keymanager.clients.dto.ITAUResponse
import br.com.zupacademy.keymanager.model.Owner
import io.micronaut.http.HttpResponse

fun ownerToRequest(tipoCliente: TipoCliente, ITAUResponse: HttpResponse<ITAUResponse>): Owner? {
    return when (tipoCliente) {
        TipoCliente.PESSOA_FISICA -> Owner(
            "NATURAL_PERSON", ITAUResponse.body()!!.titular.nome,
            ITAUResponse.body()!!.titular.cpf
        )
        TipoCliente.PESSOA_JURIDICA -> Owner(
            "LEGAL_PERSON", ITAUResponse.body()!!.titular.nome,
            ITAUResponse.body()!!.titular.cpf
        )
        else -> null
    }
}