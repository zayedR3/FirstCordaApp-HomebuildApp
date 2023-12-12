package com.r3.developers.csdetemplate.homebuild_Workflows

import com.r3.developers.csdetemplate.utxoexample.contracts.WorkState_Contract
import com.r3.developers.csdetemplate.utxoexample.states.Request_WorkState
import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.flows.FlowEngine
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.application.membership.MemberLookup
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.stream.Collectors

class UpdateRequestWorkState : ClientStartableFlow {
    @CordaInject
    var memberLookup: MemberLookup? = null

    @CordaInject
    var jsonMarshallingService: JsonMarshallingService? = null

    @CordaInject
    var ledgerService: UtxoLedgerService? = null

    @CordaInject
    var flowEngine: FlowEngine? = null

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        log.info("UpdateRequestWorkState.call() called")


        //you need get the id from UpdateRequestWorkStateArgs
        return try {
            //get the object from flowargs
            val flowArgs = requestBody.getRequestBodyAs(
                jsonMarshallingService!!,
                UpdateRequestWorkStateArgs::class.java
            )

            //you are getting all of the the Request_WorkState from the database <- this is the query
            val listOfAllRequestWorkState = ledgerService!!.findUnconsumedStatesByType(
                Request_WorkState::class.java
            )

            //now you are pick the exact request_workstate from the list, by its ID |<- this is the filtering
            val filtedRequestStateList = listOfAllRequestWorkState.stream()
                .filter { it: StateAndRef<Request_WorkState> ->
                    it.state.contractState.id == flowArgs.id
                }.collect(Collectors.toList())

            //check if there is some error <- meaning more request state that has the same id
            if (filtedRequestStateList.size != 1) throw CordaRuntimeException("Multiple or zero requestWork states with id " + flowArgs.id + " found")

            //extract the actual content from the list <- this is now your input
            val inputStateAndRef = filtedRequestStateList[0]

            //Get the member information to run the flow for the other members.
            val inputState = inputStateAndRef.state.contractState

            //customer and consultant info.
            val customerInfo = inputState.customer
            val consultantInfo = inputState.consultant


            //Create new RequestState using update message helper function.
            val newStatus = inputState.updateStatus(flowArgs.requestStatus)

            //Build up draft transaction.
            val txBuilder = ledgerService!!.createTransactionBuilder()
                .setNotary(inputStateAndRef.state.notaryName)
                .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                .addOutputState(newStatus)
                .addInputState(inputStateAndRef.ref)
                .addCommand(WorkState_Contract.Update())
                .addSignatories(newStatus.getParticipants())
            val signedTransaction = txBuilder.toSignedTransaction()
            flowEngine!!.subFlow(
                FinalizeRequestWorkSubFlow.RequestFinal(
                    signedTransaction,
                    Arrays.asList(customerInfo, consultantInfo)
                )
            )


            //you need to do a query into the database to get the request work state, <- it is your input.

            //you update the input's requestStaus to a different status. It can be "INPROGRESS", or "REJEXTED", or "APPROVED"
            //everything else remain the same. <- this is your output


            // now build the transaction

            //self sign the transaction

            //finalize the flow (collect signature, send to notary) hint: you can re-use the FinalizeRequestWorkSubFlow.
        } // Catch any exceptions, log them and rethrow the exception.
        catch (e: Exception) {
            log.warn("Failed to process utxo flow for request body " + requestBody + " because: " + e.message)
            throw e
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(UpdateRequestWorkState::class.java)
    }
}
//"4428d93b-a06f-40ae-87a6-f26bc3ea4c36
/*
{
  "clientRequestId": "update-1",
  "flowClassName": "com.r3.developers.csdetemplate.homebuild_Workflows.UpdateRequestWorkState",
  "requestBody": {
    "Id" : "4428d93b-a06f-40ae-87a6-f26bc3ea4c36",
    "requestStatus" : "APPROVED"
    }
}
* */