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
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.stream.Collectors

class TransferRequestWorkState : ClientStartableFlow {
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
        log.info("TransferRequestWorkState.call() called")


        //you need get the id from UpdateRequestWorkStateArgs
        return try {

            //get the object from flowargs
            val flowargs = requestBody.getRequestBodyAs(
                jsonMarshallingService!!,
                TransferRequestWorkStateArgs::class.java
            )

            //you are getting all of the the Request_WorkState from the database <- this is the query
            val listOfAllRequestWorkState = ledgerService!!.findUnconsumedStatesByType(
                Request_WorkState::class.java
            )

            //now you are pick the exact request_workstate from the list, by its ID |<- this is the filtering
            val filtedRequestStateList = listOfAllRequestWorkState.stream()
                .filter { it: StateAndRef<Request_WorkState> ->
                    it.state.contractState.id == flowargs.id
                }.collect(Collectors.toList())

            //check if there is some error <- meaning more request state that has the same id
            if (filtedRequestStateList.size != 1) throw CordaRuntimeException("Multiple or zero requestWork states with id " + flowargs.id + " found")

            //extract the actual content from the list <- this is now your input
            val inputStateAndRef = filtedRequestStateList[0]

            //Get the member information to run the flow for the other members.
            val inputState = inputStateAndRef.state.contractState

            //customer and consultant info.
            val customerName = inputState.customer
            val newConsultantName = MemberX500Name.parse(flowargs.new_consultant.toString())
            val municipalityName = inputState.municipality
            val myInfo = memberLookup!!.myInfo()
            val newConsultantInfo = Objects.requireNonNull(
                memberLookup!!.lookup(newConsultantName),
                "MemberLookup can't find otherMember specified in flow arguments."
            )
            val customerInfo = Objects.requireNonNull(
                memberLookup!!.lookup(customerName),
                "MemberLookup can't find otherMember specified in flow arguments."
            )
            val municipalityInfo = Objects.requireNonNull(
                memberLookup!!.lookup(municipalityName),
                "MemberLookup can't find otherMember specified in flow arguments."
            )

            //Create new RequestState using update state helper function.
            //Request_WorkState withNewConsultant = inputState.updateNewConsultant(newConsultantInfo.getName(), Arrays.asList(customerInfo.getLedgerKeys().get(0),newConsultantInfo.getLedgerKeys().get(0),municipalityInfo.getLedgerKeys().get(0)));
            //Request_WorkState withNewConsultant = inputState.updateNewConsultant(newConsultantInfo.getName(), Arrays.asList(customerInfo.getLedgerKeys().get(0),newConsultantInfo.getLedgerKeys().get(0),municipalityInfo.getLedgerKeys().get(0)));
            //Request_WorkState withNewConsultant = inputState.updateNewConsultant(newConsultantInfo.getName(), Arrays.asList(customerInfo.getLedgerKeys().get(0),newConsultantInfo.getLedgerKeys().get(0),municipalityInfo.getLedgerKeys().get(0)));
            val withNewConsultant = inputState.updateNewConsultant(
                newConsultantInfo!!.name, Arrays.asList(
                    customerInfo!!.ledgerKeys[0], newConsultantInfo.ledgerKeys[0], municipalityInfo!!.ledgerKeys[0]
                )
            )


            //Build up draft transaction.
            val txBuilder = ledgerService!!.createTransactionBuilder()
                .setNotary(inputStateAndRef.state.notaryName)
                .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                .addOutputState(withNewConsultant)
                .addInputState(inputStateAndRef.ref)
                .addCommand(WorkState_Contract.Transfer())
                .addSignatories(
                    Arrays.asList(
                        customerInfo.ledgerKeys[0],
                        newConsultantInfo.ledgerKeys[0], municipalityInfo.ledgerKeys[0], myInfo.ledgerKeys[0]
                    )
                )
            val signedTransaction = txBuilder.toSignedTransaction()
            flowEngine!!.subFlow(
                FinalizeRequestWorkSubFlow.RequestFinal(
                    signedTransaction,
                    Arrays.asList(customerName, newConsultantName, municipalityName)
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
        private val log = LoggerFactory.getLogger(TransferRequestWorkState::class.java)
    }
}
