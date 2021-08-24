package br.com.zupacademy.keymanager.clients

import br.com.zupacademy.keymanager.clients.dto.ITAUResponse
import br.com.zupacademy.keymanager.model.Titular
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itau.url}")
interface ERPClient {

    @Get("/clientes/{clienteId}/contas")
    fun consultaConta(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<ITAUResponse>
}

data class ConsultaResponse(
    val tipo: String,
    val agencia: String,
    val numero: String,
    val titular: Titular,
)