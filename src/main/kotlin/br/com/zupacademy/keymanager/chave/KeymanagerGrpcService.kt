package br.com.zupacademy.keymanager.chave

import br.com.zupacademy.keymanager.CadastroChaveRequest
import br.com.zupacademy.keymanager.CadastroChaveResponse
import br.com.zupacademy.keymanager.KeymanagerCadastraServiceGrpc
import br.com.zupacademy.keymanager.TipoChave
import br.com.zupacademy.keymanager.clients.ERPClient
import br.com.zupacademy.keymanager.model.Chave
import br.com.zupacademy.keymanager.repository.ChaveRepository
import br.com.zupacademy.keymanager.validator.CadastraChaveRequestValidator
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import java.util.*
import javax.inject.Singleton

@Singleton
class KeymanagerGrpcService(private val erpClient: ERPClient, private val chaveRepository: ChaveRepository) :
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

            val response = erpClient.consultaConta(request.id, request.tipoConta.name)
            if (response.status().code == 404) {
                responseObserver?.onError(
                    Status.NOT_FOUND
                        .withDescription("Usuário inexistente.")
                        .asRuntimeException()
                )
                return
            }

            if (request.tipoChave == TipoChave.ALEATORIA) {
                val chave = Chave(UUID.randomUUID().toString(), request.id, request.tipoConta)
                chaveRepository.save(chave)

                responseObserver!!.onNext(
                    CadastroChaveResponse.newBuilder()
                        .setPixId(chave.chavePix)
                        .build()
                )
                responseObserver.onCompleted()
                return
            }

            val chave = request.toModel()
            chaveRepository.save(chave)

            responseObserver!!.onNext(
                CadastroChaveResponse.newBuilder()
                    .setPixId(chave.chavePix)
                    .build()
            )
            responseObserver.onCompleted()
            return

        } else {
            responseObserver?.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("A requisição deve ser preenchida.")
                    .asRuntimeException()
            )
            responseObserver!!.onCompleted()
        }
    }
}

fun CadastroChaveRequest.toModel(): Chave {

    return Chave(this.valorTipo, this.id, this.tipoConta)
}