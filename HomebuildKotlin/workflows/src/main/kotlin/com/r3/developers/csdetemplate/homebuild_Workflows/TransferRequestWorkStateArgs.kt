package com.r3.developers.csdetemplate.homebuild_Workflows

import java.util.*

class TransferRequestWorkStateArgs {
    //Add a new two variables the ID and Updated consultant.
    var id: UUID? = null
    var new_consultant: String? = null

    constructor()
    constructor(id: UUID?, new_consultant: String?) {
        this.id = id
        this.new_consultant = new_consultant
    }
}