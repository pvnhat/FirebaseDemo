package com.vannhat.firebasedemo_chat

/**
 * Data fetch from Firebase to Object need a Constructor without argument
 * Field names must be like field in Firebase, unless data will be null
 */
class User(
    val Last: String?,
    val First: String?,
    val NestedObject: NestedName?,
    val Born: Int?,
    val vehicle: MutableList<String>?
) {
    constructor() : this(null, null, null, null, null)

    class NestedName(
        val oldName: String?,
        val secondName: String?,
        val thirdName: String?
    ) {
        constructor() : this(null, null, null)
    }
}

