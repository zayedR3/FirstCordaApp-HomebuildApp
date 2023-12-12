package com.r3.developers.csdetemplate.homebuild.workflows;

import com.r3.developers.csdetemplate.utxoexample.contracts.WorkState_Contract;
import com.r3.developers.csdetemplate.utxoexample.states.Request_WorkState;
import net.corda.v5.application.flows.ClientRequestBody;
import net.corda.v5.application.flows.ClientStartableFlow;
import net.corda.v5.application.flows.CordaInject;
import net.corda.v5.application.flows.FlowEngine;
import net.corda.v5.application.marshalling.JsonMarshallingService;
import net.corda.v5.application.membership.MemberLookup;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.base.exceptions.CordaRuntimeException;
import net.corda.v5.base.types.MemberX500Name;
import net.corda.v5.ledger.utxo.StateAndRef;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import net.corda.v5.ledger.utxo.transaction.UtxoTransactionBuilder;
import net.corda.v5.membership.MemberInfo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class UpdateRequestWorkState implements ClientStartableFlow {
    private final static Logger log = LoggerFactory.getLogger(UpdateRequestWorkState.class);

    @CordaInject
    public MemberLookup memberLookup;
    @CordaInject
    public JsonMarshallingService jsonMarshallingService;
    @CordaInject
    public UtxoLedgerService ledgerService;
    @CordaInject
    public FlowEngine flowEngine;


    @Override
    @Suspendable
    public String call(ClientRequestBody requestBody) {
        log.info("UpdateRequestWorkState.call() called");


        //you need get the id from UpdateRequestWorkStateArgs
        try {
            //get the object from flowargs
            UpdateRequestWorkStateArgs flowArgs = requestBody.getRequestBodyAs(jsonMarshallingService, UpdateRequestWorkStateArgs.class);

            //you are getting all of the the Request_WorkState from the database <- this is the query
            List<StateAndRef<Request_WorkState>> listOfAllRequestWorkState = ledgerService.findUnconsumedStatesByType(Request_WorkState.class);

            //now you are pick the exact request_workstate from the list, by its ID |<- this is the filtering
            List<StateAndRef<Request_WorkState>> filtedRequestStateList = listOfAllRequestWorkState.stream()
                    .filter(it -> it.getState().getContractState().id.equals(flowArgs.getId())).collect(toList());

            //check if there is some error <- meaning more request state that has the same id
            if (filtedRequestStateList.size() != 1)
                throw new CordaRuntimeException("Multiple or zero requestWork states with id " + flowArgs.getId() + " found");

            //extract the actual content from the list <- this is now your input
            StateAndRef<Request_WorkState> inputStateAndRef = filtedRequestStateList.get(0);

            //Get the member information to run the flow for the other members.

            Request_WorkState inputState = inputStateAndRef.getState().getContractState();

            //customer and consultant info.
            MemberX500Name customerInfo = inputState.customer;
            MemberX500Name consultantInfo = inputState.consultant;


            //Create new RequestState using update message helper function.
            Request_WorkState newStatus = inputState.updateStatus(flowArgs.requestStatus);

            //Build up draft transaction.
            UtxoTransactionBuilder txBuilder = ledgerService.createTransactionBuilder()
                    .setNotary(inputStateAndRef.getState().getNotaryName())
                    .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                    .addOutputState(newStatus)
                    .addInputState(inputStateAndRef.getRef())
                    .addCommand(new WorkState_Contract.Update())
                    .addSignatories(newStatus.getParticipants());


            UtxoSignedTransaction signedTransaction = txBuilder.toSignedTransaction();

            return flowEngine.subFlow(new FinalizeRequestWorkSubFlow.RequestFinal(signedTransaction, Arrays.asList(customerInfo,consultantInfo)));



            //you need to do a query into the database to get the request work state, <- it is your input.

            //you update the input's requestStaus to a different status. It can be "INPROGRESS", or "REJEXTED", or "APPROVED"
            //everything else remain the same. <- this is your output


            // now build the transaction

            //self sign the transaction

            //finalize the flow (collect signature, send to notary) hint: you can re-use the FinalizeRequestWorkSubFlow.


        }
        // Catch any exceptions, log them and rethrow the exception.
        catch (Exception e) {
            log.warn("Failed to process utxo flow for request body " + requestBody + " because: " + e.getMessage());
            throw e;
        }
    }
}

//"4428d93b-a06f-40ae-87a6-f26bc3ea4c36
/*
{
  "clientRequestId": "update-1",
  "flowClassName": "com.r3.developers.csdetemplate.homebuild.workflows.UpdateRequestWorkState",
  "requestBody": {
    "Id" : "4428d93b-a06f-40ae-87a6-f26bc3ea4c36",
    "requestStatus" : "APPROVED"
    }
}
* */