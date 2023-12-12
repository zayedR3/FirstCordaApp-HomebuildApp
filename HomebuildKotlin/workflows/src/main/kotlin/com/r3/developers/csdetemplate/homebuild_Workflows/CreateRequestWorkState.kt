package com.r3.developers.csdetemplate.homebuild_Workflows

import com.r3.developers.csdetemplate.homebuild_Workflows.FinalizeRequestWorkSubFlow.RequestFinal
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
import net.corda.v5.ledger.common.NotaryLookup
import net.corda.v5.ledger.utxo.UtxoLedgerService
import org.slf4j.LoggerFactory
import java.security.PublicKey
import java.time.Duration
import java.time.Instant
import java.util.*

class CreateRequestWorkState : ClientStartableFlow {
    // Injects the JsonMarshallingService to read and populate JSON parameters.
    @CordaInject
    var jsonMarshallingService: JsonMarshallingService? = null

    // Injects the MemberLookup to look up the VNode identities.
    @CordaInject
    var memberLookup: MemberLookup? = null

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    var ledgerService: UtxoLedgerService? = null

    // Injects the NotaryLookup to look up the notary identity.
    @CordaInject
    var notaryLookup: NotaryLookup? = null

    // FlowEngine service is required to run SubFlows.
    @CordaInject
    var flowEngine: FlowEngine? = null

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        log.info("Request_work_createFlow.call() called")
        return try {
            // Obtain the deserialized input arguments to the flow from the requestBody.
            val flowArgs =
                requestBody.getRequestBodyAs(jsonMarshallingService!!, CreateRequestWorkStateArgs::class.java)

            // Get MemberInfos for the Vnode running the flow and the otherMember.
            val myInfo = memberLookup!!.myInfo()
            val consultantInfo = Objects.requireNonNull(
                memberLookup!!.lookup(MemberX500Name.parse(flowArgs.consultant.toString())),
                "MemberLookup can't find otherMember specified in flow arguments."
            )
            val municipality = Objects.requireNonNull(
                memberLookup!!.lookup(MemberX500Name.parse(flowArgs.municipality.toString())),
                "MemberLookup can't find otherMember specified in flow arguments."
            )
            // Create the IOUState from the input arguments and member information.
            val workRequest = Request_WorkState(
                myInfo.name,
                flowArgs.age,
                flowArgs.budget,
                consultantInfo!!.name,
                municipality!!.name,
                "APPLIED",
                flowArgs.address,
                flowArgs.houseHold,
                UUID.randomUUID(),
                Arrays.asList(myInfo.ledgerKeys[0], consultantInfo.ledgerKeys[0], municipality.ledgerKeys[0])
            )
            // Obtain the Notary name and public key.
            val notary = Objects.requireNonNull(
                notaryLookup!!.lookup(MemberX500Name.parse("CN=NotaryService, OU=Test Dept, O=R3, L=London, C=GB")),
                "NotaryLookup can't find notary specified in flow arguments."
            )
            var notaryKey: PublicKey? = null
            for (memberInfo in memberLookup!!.lookup()) {
                if (memberInfo.memberProvidedContext["corda.notary.service.name"] == notary!!.name.toString()) {
                    notaryKey = memberInfo.ledgerKeys[0]
                    break
                }
            }
            // Note, in Java CorDapps only unchecked RuntimeExceptions can be thrown not
            // declared checked exceptions as this changes the method signature and breaks override.
            if (notaryKey == null) {
                throw CordaRuntimeException("No notary PublicKey found")
            }
            // Use UTXOTransactionBuilder to build up the draft transaction.
            val txBuilder = ledgerService!!.createTransactionBuilder()
                .setNotary(notary!!.name)
                .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                .addOutputState(workRequest)
                .addCommand(WorkState_Contract.create())
                .addSignatories(workRequest.getParticipants())

            // Convert the transaction builder to a UTXOSignedTransaction and sign with this Vnode's first Ledger key.
            // Note, toSignedTransaction() is currently a placeholder method, hence being marked as deprecated.
            val signedTransaction = txBuilder.toSignedTransaction()


            // Call FinalizeIOUSubFlow which will finalise the transaction.
            // If successful the flow will return a String of the created transaction id,
            // if not successful it will return an error message.
            flowEngine!!.subFlow(
                RequestFinal(
                    signedTransaction, Arrays.asList(
                        consultantInfo.name, municipality.name
                    )
                )
            )
        } catch (e: Exception) {
            log.warn("Failed to process utxo flow for request body " + requestBody + " because: " + e.message)
            throw CordaRuntimeException(e.message)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(CreateRequestWorkState::class.java)
    }
}
/*
{
  "clientRequestId": "create2",
  "flowClassName": "com.r3.developers.csdetemplate.homebuild_Workflows.CreateRequestWorkState",
  "requestBody": {
    "budget" : "500000",
    "age" : "25",
    "houseHold" : "4",
    "address" : "abuDhabi",
    "consultant" : "CN=consultantOld, OU=Test Dept, O=R3, L=AbuDhabi, C=GB",
    "municipality" : "CN=municipality, OU=Test Dept, O=R3, L=AbuDhabi, C=GB"
    }
}
*/