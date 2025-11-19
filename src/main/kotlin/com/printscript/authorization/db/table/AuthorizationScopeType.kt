package com.printscript.authorization.db.table

enum class AuthorizationScopeType(val value: String) {
    OWNER("OWNER"),
    EDITOR("EDITOR"),
    READER("READER"),
    NON_EXISTENT("NON EXISTENT"),
}
