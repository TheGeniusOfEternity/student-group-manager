package collection

class Coordinates (
    private var x: Int,
    private var y: Long
) {
    init {
        require(x > -357)
    }

    override fun toString(): String {
        return "($x, $y)"
    }
}