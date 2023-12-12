package com.r3.developers.csdetemplate.homebuild_Workflows

import com.r3.developers.csdetemplate.utxoexample.states.Request_WorkState
import net.corda.v5.application.flows.*
import net.corda.v5.application.messaging.FlowMessaging
import net.corda.v5.application.messaging.FlowSession
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.base.exceptions.CordaRuntimeException
import net.corda.v5.base.types.MemberX500Name
import net.corda.v5.ledger.utxo.UtxoLedgerService
import net.corda.v5.ledger.utxo.transaction.UtxoLedgerTransaction
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction
import net.corda.v5.ledger.utxo.transaction.UtxoTransactionValidator
import org.slf4j.LoggerFactory

class FinalizeRequestWorkSubFlow {
    private companion object{
        val log = LoggerFactory.getLogger(this::class.java.enclosingClass)
    }

    @InitiatingFlow(protocol = "finalize-iou-protocol")
    class RequestFinal(
        private val signedTransaction: UtxoSignedTransaction,
        private val otherMembers: List<MemberX500Name>
    ) :
        SubFlow<String> {
        @CordaInject
        var ledgerService: UtxoLedgerService? = null

        @CordaInject
        var flowMessaging: FlowMessaging? = null

        @Suspendable
        override fun call(): String {
            log.info("FinalizeIOU.call() called")

            // Initiates a session with the other Member.
            //FlowSession session = flowMessaging.initiateFlow(otherMember);
            val sessionsList: MutableList<FlowSession> = ArrayList()
            for (member in otherMembers) {
                sessionsList.add(flowMessaging!!.initiateFlow(member))
            }
            // Calls the Corda provided finalise() function which gather signatures from the counterparty,
            // notarises the transaction and persists the transaction to each party's vault.
            // On success returns the id of the transaction created.
            var result: String
            try {
                val finalizedSignedTransaction = ledgerService!!.finalize(
                    signedTransaction,
                    sessionsList
                ).transaction

                //\Send to the third person.
                result = finalizedSignedTransaction.id.toString()
                log.info("Success! Response: $result")
            } // Soft fails the flow and returns the error message without throwing a flow exception.
            catch (e: Exception) {
                log.warn("Finality failed", e)
                result = "Finality failed, " + e.message
            }
            // Returns the transaction id converted as a string
            return result
        }
    }

    @InitiatedBy(protocol = "finalize-iou-protocol")
    class FinalizeIOUResponderFlow : ResponderFlow {
        // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
        @CordaInject
        var utxoLedgerService: UtxoLedgerService? = null

        @Suspendable
        override fun call(session: FlowSession) {
            log.info("FinalizeIOUResponderFlow.call() called")
            try {
                // Defines the lambda validator used in receiveFinality below.
                val txValidator = UtxoTransactionValidator { ledgerTransaction: UtxoLedgerTransaction ->

                    // Note, this exception will only be shown in the logs if Corda Logging is set to debug.
                    if (ledgerTransaction.outputContractStates[0].javaClass != Request_WorkState::class.java
                    ) throw CordaRuntimeException("Failed verification - transaction did not have exactly one output IOUState.")
                    log.info("Verified the transaction - " + ledgerTransaction.id)
                }

                // Calls receiveFinality() function which provides the responder to the finalise() function
                // in the Initiating Flow. Accepts a lambda validator containing the business logic to decide whether
                // responder should sign the Transaction.
                val finalizedSignedTransaction = utxoLedgerService!!.receiveFinality(session, txValidator).transaction
                log.info("Finished responder flow - " + finalizedSignedTransaction.id)
            } // Soft fails the flow and log the exception.
            catch (e: Exception) {
                log.warn("Exceptionally finished responder flow", e)
            }
        }
    }
}