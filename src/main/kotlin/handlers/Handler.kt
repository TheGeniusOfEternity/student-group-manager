package handlers

interface Handler<T> {
    /**
     * This function handle some data and returns result or null on fail.
     *
     * @param data subject of handler, could be some info itself or path to it (ex. fileName)
     * @param option extra param for some handlers (ex. className)
     * @return handle result (could be new StudyGroup)
     */
    fun handle(data: T, option: String): Any?
}