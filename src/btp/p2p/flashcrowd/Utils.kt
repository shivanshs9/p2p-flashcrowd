package btp.p2p.flashcrowd

object MsgTypes {
    const val FIND_VAL = 1
    const val STORE_VAL = 2
    const val FIND_VAL_STERILE = 3
    const val STORE_VAL_STERILE = 4
    const val CONN_REQ = 5
    const val CONN_SUCCESS = 6
    const val CONN_FAILURE = 7
    const val CONN_REQ_STERILE = 8
    const val CONN_SUCCESS_STERILE = 9
}

object Constants {
    const val K = 4
    const val globalTimeout: Long = 3000
    const val globalDelay: Long = 500
    const val SterileDelay: Long = 7500
}