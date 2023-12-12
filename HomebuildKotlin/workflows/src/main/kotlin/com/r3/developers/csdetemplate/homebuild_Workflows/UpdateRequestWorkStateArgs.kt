package com.r3.developers.csdetemplate.homebuild_Workflows

import java.util.*

class UpdateRequestWorkStateArgs {
    //you only need two variables here
    // id, and stauts.
    var id: UUID? = null
    var requestStatus: String? = null

    constructor()
    constructor(Id: UUID?, requestStatus: String?) {
        id = Id
        this.requestStatus = requestStatus
    }
}