package br.com.zupacademy.keymanager.chave

import br.com.zupacademy.keymanager.KeymanagerRemoveServiceGrpc
import br.com.zupacademy.keymanager.RemoveRequest
import br.com.zupacademy.keymanager.RemoveResponse
import br.com.zupacademy.keymanager.clients.ERPClient
import br.com.zupacademy.keymanager.repository.ChaveRepository
import br.com.zupacademy.keymanager.validator.RemoveChaveRequestValidator
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import javax.inject.Singleton

@Singleton
class KeymanagerRemoveGrpcService(private val erpClient: ERPClient, private val chaveRepository: ChaveRepository) :
    KeymanagerRemoveServiceGrpc.KeymanagerRemoveServiceImplBase() {

    override fun removeChave(request: RemoveRequest?, responseObserver: StreamObserver<RemoveResponse>?) {

        if (request!!.allFields.isNotEmpty()) {

            val removeChaveRequestValidator = RemoveChaveRequestValidator(chaveRepository)

            try {
                removeChaveRequestValidator.validaRequest(request)
            } catch (e: StatusRuntimeException) {
                responseObserver?.onError(e)
                responseObserver!!.onCompleted()
                return
            }

            try {
                val chaveEncontrada = chaveRepository.findById(request.pixId)
                chaveRepository.delete(chaveEncontrada.get())

                responseObserver?.onNext(RemoveResponse.newBuilder()
                    .setRemovido(true)
                    .build())
                responseObserver!!.onCompleted()
            }catch (e: Exception){
                responseObserver?.onError(Status.UNKNOWN
                    .withDescription("Um erro inesperado aconteceu, tente novamente mais tarde!")
                    .asRuntimeException())
                return
            }

        }
    }
}