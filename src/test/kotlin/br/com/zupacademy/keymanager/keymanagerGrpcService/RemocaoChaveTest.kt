package br.com.zupacademy.keymanager.keymanagerGrpcService

import br.com.zupacademy.keymanager.KeymanagerRemoveServiceGrpc
import br.com.zupacademy.keymanager.RemoveRequest
import br.com.zupacademy.keymanager.TipoConta
import br.com.zupacademy.keymanager.model.Chave
import br.com.zupacademy.keymanager.repository.ChaveRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class RemocaoChaveTest(private val gRpcRemoveClient: KeymanagerRemoveServiceGrpc.KeymanagerRemoveServiceBlockingStub) {

    @Inject
    lateinit var chaveRepository: ChaveRepository

    lateinit var chave: Chave

    @AfterEach
    internal fun tearDown() {
        chaveRepository.deleteAll()
    }

    @BeforeEach
    internal fun setUp() {
        chave = chaveRepository.save(
            Chave(
                "gustavo@gmail.com",
                "c56dfef4-7901-44fb-84e2-a2cefb157890",
                TipoConta.CONTA_CORRENTE
            )
        )
    }

    /*
    1 - deve remover chave com sucesso.
    2 - nao deve remover chave com dono diferente.
    3 - nao deve remover chave com pixId vazio.
    4 - nao deve remover chave com clienteId vazio.
    5 - nao deve remover chave inexistente.
     */

    @Test
    internal fun `deve remover chave com sucesso`() {
        val response = gRpcRemoveClient.removeChave(
            RemoveRequest.newBuilder()
                .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setPixId(chave.pixId)
                .build()
        )

        assertTrue(!chaveRepository.existsByChavePix("gustavo@gmail.com"))
        assertEquals(true, response.removido)
    }

    @Test
    internal fun `deve retornar NOT_FOUND ao tentar remover uma chave com clienteId invalido`() {
        val error = assertThrows<StatusRuntimeException> {
            gRpcRemoveClient.removeChave(
                RemoveRequest.newBuilder()
                    .setPixId(chave.pixId)
                    .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave PIX já removida ou inexistente.", status.description)
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT ao tentar remover chave sem clienteId`() {

        val error = assertThrows<StatusRuntimeException> {
            gRpcRemoveClient.removeChave(
                RemoveRequest.newBuilder()
                    .setPixId(chave.pixId)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("O identificador do cliente deve ser informado.", status.description)
        }
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT ao tentar remover chave sem pixId`() {

        val error = assertThrows<StatusRuntimeException> {
            gRpcRemoveClient.removeChave(
                RemoveRequest.newBuilder()
                    .setClienteId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("O ID da chave PIX deve ser informado.", status.description)
        }
    }

    @Factory
    class RemocaoClients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerRemoveServiceGrpc.KeymanagerRemoveServiceBlockingStub {
            return KeymanagerRemoveServiceGrpc.newBlockingStub(channel)
        }
    }

}