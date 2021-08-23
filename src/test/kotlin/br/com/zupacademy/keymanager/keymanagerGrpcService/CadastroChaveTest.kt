package br.com.zupacademy.keymanager.keymanagerGrpcService

import br.com.zupacademy.keymanager.CadastroChaveRequest
import br.com.zupacademy.keymanager.KeymanagerServiceGrpc
import br.com.zupacademy.keymanager.TipoChave
import br.com.zupacademy.keymanager.TipoConta
import br.com.zupacademy.keymanager.clients.ERPClient
import br.com.zupacademy.keymanager.model.Chave
import br.com.zupacademy.keymanager.repository.ChaveRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CadastroChaveTest(
    val gRpcClient: KeymanagerServiceGrpc.KeymanagerServiceBlockingStub
) {

    @Inject
    lateinit var chaveRepository: ChaveRepository

    @BeforeEach
    internal fun tearDown() {
        chaveRepository.deleteAll()
    }

    @Test
    internal fun `deve encontrar conta com id do cliente`() {
        val client: ERPClient = Mockito.mock(ERPClient::class.java)

        Mockito.`when`(client.consultaConta("c56dfef4-7901-44fb-84e2-a2cefb157890", "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok())

        val response = client.consultaConta("c56dfef4-7901-44fb-84e2-a2cefb157890", "CONTA_CORRENTE")

        assertEquals(HttpStatus.OK, response.status)
    }

    @Test
    internal fun `deve retornar erro not_found ao procurar conta`() {
        val client: ERPClient = Mockito.mock(ERPClient::class.java)

        Mockito.`when`(client.consultaConta("2f6bdb46-ebfc-46c1-aede-0dd74d730182", "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        val response = client.consultaConta("2f6bdb46-ebfc-46c1-aede-0dd74d730182", "CONTA_CORRENTE")

        assertEquals(HttpStatus.NOT_FOUND, response.status)
    }

    @Test
    internal fun `deve cadastrar chave com cpf`() {
        //cenário

        //ação
        val response = gRpcClient.cadastraChave(
            CadastroChaveRequest.newBuilder()
                .setId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave(TipoChave.CPF)
                .setValorTipo("12547896587")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        //validação
        assertNotNull(response.pixId)
        assertEquals("12547896587", response.pixId)
        assertTrue(chaveRepository.existsByChavePix("12547896587"))
    }

    @Test
    internal fun `deve retornar ALREADY_EXISTS para chave CPF duplicada`() {
        //cenário

        chaveRepository.save(Chave("12547896587", "c56dfef4-7901-44fb-84e2-a2cefb157890", TipoConta.CONTA_CORRENTE))

        //ação
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            gRpcClient.cadastraChave(
                CadastroChaveRequest.newBuilder()
                    .setId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.CPF)
                    .setValorTipo("12547896587")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        //validação
        with(error) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave já cadastrada.", status.description)
        }

    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT ao tentar cadastrar uma chave de formato invalido`() {
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            gRpcClient.cadastraChave(
                CadastroChaveRequest.newBuilder()
                    .setId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.EMAIL)
                    .setValorTipo("gustavogmail.com")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Email em branco ou inválido.\nFormato esperado example@example.com", status.description)

        }
    }

    @Test
    internal fun `deve cadastrar chave aleatoria`() {
        gRpcClient.cadastraChave(
            CadastroChaveRequest.newBuilder()
                .setId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave(TipoChave.ALEATORIA)
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        val chaveCriada = chaveRepository.findAll().get(0)

        assertTrue(chaveRepository.findAll().size == 1)
        assertEquals("c56dfef4-7901-44fb-84e2-a2cefb157890", chaveCriada.clienteId)
        assertTrue(chaveCriada.chavePix.isNotBlank())
        assertTrue(chaveCriada.chavePix.matches("([a-f0-9]{8}(-[a-f0-9]{4}){4}[a-f0-9]{8})".toRegex()))
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT quando tentar cadastrar chave com mais de 77 caracteres`() {
        val error = org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            gRpcClient.cadastraChave(
                CadastroChaveRequest.newBuilder()
                    .setId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.EMAIL)
                    .setValorTipo("gustavo@gmail.co" + "m".repeat(62))
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Por favor, insira uma chave com até 77 caracteres.", status.description)

        }
    }

    @Test
    internal fun `deve cadastrar chave com exatamente 77 caracteres`() {

        gRpcClient.cadastraChave(
            CadastroChaveRequest.newBuilder()
                .setId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave(TipoChave.EMAIL)
                .setValorTipo("gustavo@gmail.co" + "m".repeat(61))
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        assertTrue(chaveRepository.existsByChavePix("gustavo@gmail.co" + "m".repeat(61)))
    }

    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerServiceGrpc.KeymanagerServiceBlockingStub {
            return KeymanagerServiceGrpc.newBlockingStub(channel)
        }
    }
}