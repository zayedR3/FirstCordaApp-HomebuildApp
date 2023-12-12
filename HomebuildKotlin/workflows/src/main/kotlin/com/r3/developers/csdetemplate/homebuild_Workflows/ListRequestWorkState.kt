package com.r3.developers.csdetemplate.homebuild_Workflows

import com.r3.developers.csdetemplate.utxoexample.states.Request_WorkState
import net.corda.v5.application.flows.ClientRequestBody
import net.corda.v5.application.flows.ClientStartableFlow
import net.corda.v5.application.flows.CordaInject
import net.corda.v5.application.marshalling.JsonMarshallingService
import net.corda.v5.base.annotations.Suspendable
import net.corda.v5.ledger.utxo.StateAndRef
import net.corda.v5.ledger.utxo.UtxoLedgerService
import org.slf4j.LoggerFactory
import java.util.stream.Collectors


class ListRequestWorkState : ClientStartableFlow {
    @CordaInject
    var jsonMarshallingService: JsonMarshallingService? = null

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    var utxoLedgerService: UtxoLedgerService? = null

    @Suspendable
    override fun call(requestBody: ClientRequestBody): String {
        log.info("ListRequestWorkState.call() called")

        // Queries the VNode's vault for unconsumed states and converts the result to a serializable DTO.
        val states = utxoLedgerService!!.findUnconsumedStatesByType(
            Request_WorkState::class.java
        )
        val results = states.stream().map { stateAndRef: StateAndRef<Request_WorkState> ->
            ListRequestWorkStateResults(
                stateAndRef.state.contractState.customer,
                stateAndRef.state.contractState.age,
                stateAndRef.state.contractState.budget,
                stateAndRef.state.contractState.consultant,
                stateAndRef.state.contractState.municipality,
                stateAndRef.state.contractState.requestStatus,
                stateAndRef.state.contractState.address,
                stateAndRef.state.contractState.houseHold,
                stateAndRef.state.contractState.id
            )
        }.collect(Collectors.toList())
        return jsonMarshallingService!!.format(results)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ListRequestWorkState::class.java)
    }
}
