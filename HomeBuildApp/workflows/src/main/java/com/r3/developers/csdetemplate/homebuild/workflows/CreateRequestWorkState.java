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
import net.corda.v5.ledger.common.NotaryLookup;
import net.corda.v5.ledger.utxo.UtxoLedgerService;
import net.corda.v5.ledger.utxo.transaction.UtxoSignedTransaction;
import net.corda.v5.ledger.utxo.transaction.UtxoTransactionBuilder;
import net.corda.v5.membership.MemberInfo;
import net.corda.v5.membership.NotaryInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public class CreateRequestWorkState implements ClientStartableFlow {
    private final static Logger log = LoggerFactory.getLogger(CreateRequestWorkState.class);

    // Injects the JsonMarshallingService to read and populate JSON parameters.
    @CordaInject
    public JsonMarshallingService jsonMarshallingService;

    // Injects the MemberLookup to look up the VNode identities.
    @CordaInject
    public MemberLookup memberLookup;

    // Injects the UtxoLedgerService to enable the flow to make use of the Ledger API.
    @CordaInject
    public UtxoLedgerService ledgerService;

    // Injects the NotaryLookup to look up the notary identity.
    @CordaInject
    public NotaryLookup notaryLookup;

    // FlowEngine service is required to run SubFlows.
    @CordaInject
    public FlowEngine flowEngine;

    @Override
    @Suspendable
    public String call(ClientRequestBody requestBody) {
        log.info("Request_work_createFlow.call() called");

        try {
            // Obtain the deserialized input arguments to the flow from the requestBody.
            CreateRequestWorkStateArgs flowArgs = requestBody.getRequestBodyAs(jsonMarshallingService, CreateRequestWorkStateArgs.class);

            // Get MemberInfos for the Vnode running the flow and the otherMember.
            MemberInfo myInfo = memberLookup.myInfo();


            MemberInfo consultantInfo = requireNonNull(
                    memberLookup.lookup(MemberX500Name.parse(flowArgs.getConsultant())),
                    "MemberLookup can't find otherMember specified in flow arguments."
            );
            MemberInfo municipality = requireNonNull(
                    memberLookup.lookup(MemberX500Name.parse(flowArgs.getMunicipality())),
                    "MemberLookup can't find otherMember specified in flow arguments."
            );
            // Create the IOUState from the input arguments and member information.
            Request_WorkState workRequest = new Request_WorkState(
                    myInfo.getName(),
                    flowArgs.getAge(),
                    flowArgs.getBudget(),
                    consultantInfo.getName(),
                    municipality.getName(),
                    "APPLIED",
                    flowArgs.getAddress(),
                    flowArgs.getHouseHold(),
                    UUID.randomUUID(),
                    Arrays.asList(myInfo.getLedgerKeys().get(0),consultantInfo.getLedgerKeys().get(0),municipality.getLedgerKeys().get(0))

            );
            // Obtain the Notary name and public key.
            NotaryInfo notary = requireNonNull(
                    notaryLookup.lookup(MemberX500Name.parse("CN=NotaryService, OU=Test Dept, O=R3, L=London, C=GB")),
                    "NotaryLookup can't find notary specified in flow arguments."
            );
            PublicKey notaryKey = null;
            for(MemberInfo memberInfo: memberLookup.lookup()){
                if(Objects.equals(
                        memberInfo.getMemberProvidedContext().get("corda.notary.service.name"),
                        notary.getName().toString())) {
                    notaryKey = memberInfo.getLedgerKeys().get(0);
                    break;
                }
            }
            // Note, in Java CorDapps only unchecked RuntimeExceptions can be thrown not
            // declared checked exceptions as this changes the method signature and breaks override.
            if(notaryKey == null) {
                throw new CordaRuntimeException("No notary PublicKey found");
            }
            // Use UTXOTransactionBuilder to build up the draft transaction.
            UtxoTransactionBuilder txBuilder = ledgerService.createTransactionBuilder()
                    .setNotary(notary.getName())
                    .setTimeWindowBetween(Instant.now(), Instant.now().plusMillis(Duration.ofDays(1).toMillis()))
                    .addOutputState(workRequest)
                    .addCommand(new WorkState_Contract.create())
                    .addSignatories(workRequest.getParticipants());

            // Convert the transaction builder to a UTXOSignedTransaction and sign with this Vnode's first Ledger key.
            // Note, toSignedTransaction() is currently a placeholder method, hence being marked as deprecated.
            @SuppressWarnings("DEPRECATION")
            UtxoSignedTransaction signedTransaction = txBuilder.toSignedTransaction();


            // Call FinalizeIOUSubFlow which will finalise the transaction.
            // If successful the flow will return a String of the created transaction id,
            // if not successful it will return an error message.
            return flowEngine.subFlow(new FinalizeRequestWorkSubFlow.RequestFinal(signedTransaction, Arrays.asList(consultantInfo.getName(),municipality.getName())));


        }
        catch (Exception e) {
            log.warn("Failed to process utxo flow for request body " + requestBody + " because: " + e.getMessage());
            throw new CordaRuntimeException(e.getMessage());
        }
    }
}

/*
{
  "clientRequestId": "create2",
  "flowClassName": "com.r3.developers.csdetemplate.homebuild.workflows.CreateRequestWorkState",
  "requestBody": {
    "budget" : "500000",
    "age" : "25",
    "houseHold" : "4",
    "address" : "abuDhabi",
    "consultant" : "CN=consultantOld, OU=Test Dept, O=R3, L=AbuDhabi, C=GB",
    "municipality" : "CN=municipality, OU=Test Dept, O=R3, L=AbuDhabi, C=GB"
    }
}
* */