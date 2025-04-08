package parsers

/**
 * Main interface for all parsers.
 */
interface Parser<T> {
    fun parse(data: T): Any?
}