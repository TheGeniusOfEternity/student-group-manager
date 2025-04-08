package collection

import annotations.Pos

/**
 * Class, representing  [StudyGroup.coordinates]
 */
class Coordinates (
    @Pos(31) private var x: Int,
    @Pos(32) private var y: Long
) {
    init {
        require(x > -357)
    }

    /**
     * @return Coordinates string representation
     */
    override fun toString(): String {
        return "($x, $y)"
    }
}