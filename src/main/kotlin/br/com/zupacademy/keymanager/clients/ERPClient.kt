package br.com.zupacademy.keymanager.clients

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${contas.url}")
interface ERPClient {

    @Get("/clientes/{clienteId}/contas")
    fun consultaConta(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<ConsultaResponse>
}