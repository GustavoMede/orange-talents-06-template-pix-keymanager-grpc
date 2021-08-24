package br.com.zupacademy.keymanager.clients

import br.com.zupacademy.keymanager.clients.dto.*
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${bcb.url}")
interface BCBClient {

    @Post(produces = ([ MediaType.APPLICATION_XML]), consumes = ([ MediaType.APPLICATION_XML]))
    fun cadastroChaveBCB(@Body BCBRequest: BCBCadastroRequest): HttpResponse<BCBCadastroResponse>

    @Delete("/{key}", produces = ([ MediaType.APPLICATION_XML]), consumes = ([ MediaType.APPLICATION_XML]))
    fun removeChaveBCB(@PathVariable key: String, @Body remocaoRequest: BCBRemocaoRequest)

}