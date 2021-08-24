package br.com.zupacademy.keymanager.chave

import br.com.zupacademy.keymanager.CadastroChaveRequest
import br.com.zupacademy.keymanager.CadastroChaveResponse
import br.com.zupacademy.keymanager.KeymanagerCadastraServiceGrpc
import br.com.zupacademy.keymanager.clients.BCBClient
import br.com.zupacademy.keymanager.clients.ERPClient
import br.com.zupacademy.keymanager.extensions.bankAccountToRequest
import br.com.zupacademy.keymanager.extensions.novaChaveBCBRequest
import br.com.zupacademy.keymanager.extensions.ownerToRequest
import br.com.zupacademy.keymanager.model.Chave
import br.com.zupacademy.keymanager.repository.ChaveRepository
import br.com.zupacademy.keymanager.validator.CadastraChaveRequestValidator
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton

@Singleton
class KeymanagerGrpcService(
    private val erpClient: ERPClient,
    private val chaveRepository: ChaveRepository,
    private val bcbClient: BCBClient
) :
    KeymanagerCadastraServiceGrpc.KeymanagerCadastraServiceImplBase() {

    override fun cadastraChave(
        request: CadastroChaveRequest?,
        responseObserver: StreamObserver<CadastroChaveResponse>?
    ) {

        if (request!!.allFields.isNotEmpty()) {

            val cadastraChaveRequestValidator = CadastraChaveRequestValidator(chaveRepository)

            try {
                cadastraChaveRequestValidator.validaRequest(request)
            } catch (e: StatusRuntimeException) {
                responseObserver?.onError(e)
                responseObserver!!.onCompleted()
                return
            }

            val consultaResponse = erpClient.consultaConta(request.id, request.tipoConta.name)
            if (consultaResponse.status().code == 404 || consultaResponse.body() == null) {
                responseObserver?.onError(
                    Status.NOT_FOUND
                        .withDescription("Usuário inexistente.")
                        .asRuntimeException()
                )
                return
            }

            val bankAccount = bankAccountToRequest(request.tipoConta, consultaResponse)
            val owner = ownerToRequest(request.tipoCliente, consultaResponse)

            if (owner != null && bankAccount != null) {

                try {
                    bcbClient.cadastroChaveBCB(
                        novaChaveBCBRequest(bankAccount, owner, request)
                    ).body()?.let {
                        val chave = chaveRepository.save(Chave(it.key, request.id, request.tipoConta))
                        responseObserver?.onNext(
                            CadastroChaveResponse.newBuilder()
                                .setPixId(chave.pixId)
                                .setClienteId(chave.clienteId)
                                .build()
                        )
                        responseObserver?.onCompleted()
                        return
                    }
                } catch (e: HttpClientResponseException) {
                    when (e.status.code) {
                        422 -> {
                            responseObserver?.onError(
                                Status.ALREADY_EXISTS
                                    .withDescription("Chave já existente!")
                                    .asRuntimeException()
                            )
                        }
                    }
                    responseObserver?.onCompleted()
                    return
                }
            }


        } else {
            responseObserver?.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("A requisição deve ser preenchida.")
                    .asRuntimeException()
            )
            responseObserver!!.onCompleted()
            return
        }
    }

}

fun CadastroChaveRequest.toModel(): Chave {

    return Chave(this.valorTipo, this.id, this.tipoConta)
}


