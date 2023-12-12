package com.r3.developers.csdetemplate.homebuild.workflows;

import java.util.UUID;

public class UpdateRequestWorkStateArgs {


    //you only need two variables here
    // id, and stauts.
    public UUID Id;
    public String requestStatus;

    public UpdateRequestWorkStateArgs() {
    }

    public UpdateRequestWorkStateArgs(UUID Id, String requestStatus) {
        this.Id = Id;
        this.requestStatus = requestStatus;
    }

    public UUID getId() {
        return Id;
    }

    public String getRequestStatus() {
        return requestStatus;
    }
}
