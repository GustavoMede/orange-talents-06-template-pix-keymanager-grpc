package br.com.zupacademy.keymanager.chave

import br.com.zupacademy.keymanager.CadastroChaveRequest
import br.com.zupacademy.keymanager.CadastroChaveResponse
import br.com.zupacademy.keymanager.KeymanagerServiceGrpc
import br.com.zupacademy.keymanager.TipoChave
import br.com.zupacademy.keymanager.clients.ERPClient
import br.com.zupacademy.keymanager.model.Chave
import br.com.zupacademy.keymanager.repository.ChaveRepository
import br.com.zupacademy.keymanager.validator.CadastraChaveRequestValidator
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import io.micronaut.http.exceptions.HttpStatusException
import java.util.*
import javax.inject.Singleton

@Singleton
class KeymanagerGrpcService(private val erpClient: ERPClient, private val chaveRepository: ChaveRepository) :
    KeymanagerServiceGrpc.KeymanagerServiceImplBase() {

    override fun cadastraChave(
        request: CadastroChaveRequest?,
        responseObserver: StreamObserver<CadastroChaveResponse>?
    ) {

        if (request != null) {

            val cadastraChaveRequestValidator = CadastraChaveRequestValidator(chaveRepository)

            try {
                cadastraChaveRequestValidator.validaRequest(request)
            } catch (e: StatusRuntimeException) {
                responseObserver?.onError(e)
                responseObserver!!.onCompleted()

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


            if (request.tipoChave == TipoChave.CPF) {
                val chave = request.toModel()
                chaveRepository.save(chave)

                responseObserver!!.onNext(
                    CadastroChaveResponse.newBuilder()
                        .setPixId(chave.chavePix)
                        .build()
                )
                responseObserver.onCompleted()
            }

            if (request.tipoChave == TipoChave.EMAIL) {
                val chave = request.toModel()
                chaveRepository.save(chave)

                responseObserver!!.onNext(
                    CadastroChaveResponse.newBuilder()
                        .setPixId(chave.chavePix)
                        .build()
                )
                responseObserver.onCompleted()
            }

            if (request.tipoChave == TipoChave.TELEFONE) {
                val chave = request.toModel();
                chaveRepository.save(chave)

                responseObserver!!.onNext(
                    CadastroChaveResponse.newBuilder()
                        .setPixId(chave.chavePix)
                        .build()
                )
                responseObserver.onCompleted()
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
            }

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