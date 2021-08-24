package br.com.zupacademy.keymanager.keymanagerGrpcService

import br.com.zupacademy.keymanager.*
import br.com.zupacademy.keymanager.clients.dto.ITAUResponse
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
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CadastroChaveTest(
    private val gRpcCadastraClient: KeymanagerCadastraServiceGrpc.KeymanagerCadastraServiceBlockingStub
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
            .thenReturn(HttpResponse.ok(ITAUResponse("CONTA_CORRENTE", "0001", "291900")))

        val response = client.consultaConta("c56dfef4-7901-44fb-84e2-a2cefb157890", "CONTA_CORRENTE")

        assertEquals(HttpStatus.OK, response.status)
        assertEquals("CONTA_CORRENTE", response.body().tipo)
        assertEquals("0001", response.body().agencia)
        assertEquals("291900", response.body().numero)
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
        val response = gRpcCadastraClient.cadastraChave(
            CadastroChaveRequest.newBuilder()
                .setId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave(TipoChave.CPF)
                .setValorTipo("12547896587")
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        val chaveEncontrada = chaveRepository.findAll()

        //validação
        assertNotNull(response.pixId)
        assertEquals("12547896587", response.pixId)
        assertTrue(chaveRepository.existsByChavePix("12547896587"))
        assertEquals(TipoConta.CONTA_CORRENTE, chaveEncontrada.get(0).tipoConta)
    }

    @Test
    internal fun `deve retornar ALREADY_EXISTS para chave CPF duplicada`() {
        //cenário

        chaveRepository.save(Chave("12547896587", "c56dfef4-7901-44fb-84e2-a2cefb157890", TipoConta.CONTA_CORRENTE))

        //ação
        val error = assertThrows<StatusRuntimeException> {
            gRpcCadastraClient.cadastraChave(
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
        val error = assertThrows<StatusRuntimeException> {
            gRpcCadastraClient.cadastraChave(
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
        gRpcCadastraClient.cadastraChave(
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
        val error = assertThrows<StatusRuntimeException> {
            gRpcCadastraClient.cadastraChave(
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

        gRpcCadastraClient.cadastraChave(
            CadastroChaveRequest.newBuilder()
                .setId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave(TipoChave.EMAIL)
                .setValorTipo("gustavo@gmail.co" + "m".repeat(61))
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        assertTrue(chaveRepository.existsByChavePix("gustavo@gmail.co" + "m".repeat(61)))
    }

    @Test
    internal fun `deve cadastrar telefone como chave valida`() {

        gRpcCadastraClient.cadastraChave(
            CadastroChaveRequest.newBuilder()
                .setId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                .setTipoChave(TipoChave.EMAIL)
                .setValorTipo("gustavo@gmail.co" + "m".repeat(61))
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        )

        assertTrue(chaveRepository.existsByChavePix("gustavo@gmail.co" + "m".repeat(61)))
    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT ao enviar uma requisicao sem tipo da chave`() {

        val error = assertThrows<StatusRuntimeException> {
            gRpcCadastraClient.cadastraChave(
                CadastroChaveRequest.newBuilder()
                    .setId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setValorTipo("gustavo@gmail.com")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("O tipo da chave deve ser informado.", status.description)
        }

    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT ao enviar uma requisicao sem tipo da conta`() {

        val error = assertThrows<StatusRuntimeException> {
            gRpcCadastraClient.cadastraChave(
                CadastroChaveRequest.newBuilder()
                    .setId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.EMAIL)
                    .setValorTipo("gustavo@gmail.com")
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("O tipo da conta deve ser informado.", status.description)
        }

    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT ao enviar uma requisicao nula`() {

        val error = assertThrows<StatusRuntimeException> {
            gRpcCadastraClient.cadastraChave(
                null
            )
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("A requisição deve ser preenchida.", status.description)
        }

    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT ao enviar uma requisicao com cpf invalido`() {

        val error = assertThrows<StatusRuntimeException> {
            gRpcCadastraClient.cadastraChave(
                CadastroChaveRequest.newBuilder()
                    .setId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.CPF)
                    .setValorTipo("154268574587")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("CPF em branco ou inválido.\n" +
                    "Formato esperado 111.222.333-44", status.description)
        }

    }

    @Test
    internal fun `deve retornar INVALID_ARGUMENT ao enviar uma requisicao com telefone invalido`() {

        val error = assertThrows<StatusRuntimeException> {
            gRpcCadastraClient.cadastraChave(
                CadastroChaveRequest.newBuilder()
                    .setId("c56dfef4-7901-44fb-84e2-a2cefb157890")
                    .setTipoChave(TipoChave.TELEFONE)
                    .setValorTipo("55665874585")
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(error) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Telefone em branco ou inválido.\n" +
                    "Formato esperado +5585988714077", status.description)
        }

    }

    @Factory
    class CadastroClients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeymanagerCadastraServiceGrpc.KeymanagerCadastraServiceBlockingStub {
            return KeymanagerCadastraServiceGrpc.newBlockingStub(channel)
        }
    }
}