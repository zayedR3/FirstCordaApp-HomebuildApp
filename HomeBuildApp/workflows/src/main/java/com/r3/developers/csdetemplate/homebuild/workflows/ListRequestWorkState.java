package com.r3.developers.csdetemplate.homebuild.workflows;

import com.r3.developers.csdetemplate.utxoexample.states.Request_WorkState;
import net.corda.v5.application.flows.ClientRequestBody;
import net.corda.v5.application.flows.ClientStartableFlow;
import net.corda.v5.application.flows.CordaInject;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class ListRequestWorkState implements ClientStartableFlow {
    private final static Logger log = LoggerFactory.getLogger(ListRequestWorkState.class);

    @CordaInject
    public JsonMarshallingService jsonMarshallingService;

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    public UtxoLedgerService utxoLedgerService;

    @Suspendable
    @Override
    public String call(ClientRequestBody requestBody) {

        log.info("ListRequestWorkState.call() called");

        // Queries the VNode's vault for unconsumed states and converts the result to a serializable DTO.
        List<StateAndRef<Request_WorkState>> states = utxoLedgerService.findUnconsumedStatesByType(Request_WorkState.class);
        List<ListRequestWorkStateResults> results = states.stream().map( stateAndRef ->
                new ListRequestWorkStateResults(
                        stateAndRef.getState().getContractState().customer,
                        stateAndRef.getState().getContractState().age,
                        stateAndRef.getState().getContractState().budget,
                        stateAndRef.getState().getContractState().consultant,
                        stateAndRef.getState().getContractState().municipality,
                        stateAndRef.getState().getContractState().requestStatus,
                        stateAndRef.getState().getContractState().address,
                        stateAndRef.getState().getContractState().houseHold,
                        stateAndRef.getState().getContractState().id
                )
        ).collect(Collectors.toList());
        return jsonMarshallingService.format(results);
    }
}

/*
RequestBody for triggering the flow via REST:
{
    "clientRequestId": "list-1",
    "flowClassName": "com.r3.developers.csdetemplate.homebuild.workflows.ListRequestWorkState",
    "requestBody": {}
}
*/