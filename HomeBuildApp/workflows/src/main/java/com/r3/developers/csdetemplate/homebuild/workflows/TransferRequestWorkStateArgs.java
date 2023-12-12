package com.r3.developers.csdetemplate.homebuild.workflows;

import net.corda.v5.base.types.MemberX500Name;

import java.util.UUID;

public class TransferRequestWorkStateArgs {
    //Add a new two variables the ID and Updated consultant.
    public UUID Id;
    public String new_consultant;
    public TransferRequestWorkStateArgs(){}

    public TransferRequestWorkStateArgs(UUID id, String new_consultant) {
        Id = id;
        this.new_consultant = new_consultant;
    }

    public UUID getId() {
        return Id;
    }

    public String getNew_consultant() {
        return new_consultant;
    }
}
