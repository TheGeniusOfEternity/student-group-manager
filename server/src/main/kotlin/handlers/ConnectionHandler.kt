package handlers

import collection.StudyGroup
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import dto.ExecuteCommandDto
import invoker.Invoker
import serializers.JsonSerializer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object ConnectionHandler {
    private const val HEALTH_CHECK_REQUESTS = "health-check-requests"
    private const val HEALTH_CHECK_RESPONSES = "health-check-responses"
    private const val DATA_REQUESTS = "data-requests"
    const val DATA_RESPONSES = "data-responses"
    val factory = ConnectionFactory().apply { this.host = "localhost" }
    var currentConnection: Connection? = null
    fun initializeConnection() {

        try {
            currentConnection = factory.newConnection("server")
            val channel = currentConnection?.createChannel()
            val latch = CountDownLatch(1)

            channel?.queueDeclare(HEALTH_CHECK_REQUESTS, false, false, false, null)

            val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                val response = String(delivery.body, charset("UTF-8"))
                if (response == "Are you GOIDA?") {
                    IOHandler printInfoLn "GOIDA requested, sending response..."
                    channel?.basicPublish("", HEALTH_CHECK_RESPONSES, null, "Yes, I am GOIDA!".toByteArray())
                    latch.countDown()
                    State.isRunning = true
                }
            }
            channel?.basicConsume(HEALTH_CHECK_REQUESTS, true, deliverCallback) { _: String? -> }
            latch.await()
        } catch (e: Exception) {
            IOHandler printInfoLn e.printStackTrace().toString()
        }
    }

    fun handleRequests() {
        val channel = currentConnection?.createChannel()
        val latch = CountDownLatch(1)
        var commandRequest: ExecuteCommandDto?
        channel?.queueDeclare(DATA_REQUESTS, false, false, false, null)

        val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
            commandRequest = JsonSerializer.deserialize<ExecuteCommandDto>(delivery.body)
            Invoker.run(commandRequest!!.name,
                if (commandRequest!!.params != null) listOf(commandRequest!!.params) else listOf()
            )
            latch.countDown()
        }
        channel?.basicConsume(DATA_REQUESTS, true, deliverCallback) { _: String? -> }
        latch.await(100, TimeUnit.MILLISECONDS)
        channel?.close()
    }

    inline fun <reified T> handleResponse(commandResponse: ArrayList<T?>?) {
        val channel = currentConnection?.createChannel()
        val bytedResponse = JsonSerializer.serialize<ArrayList<T?>?>(commandResponse)
        channel?.queueDeclare(DATA_RESPONSES, false, false, false, null)
        channel?.basicPublish("", DATA_RESPONSES, null, bytedResponse)
        channel?.close()
        IOHandler.responsesThread.clear()
    }
}