package collection

import annotations.Pos
import kotlinx.serialization.Serializable

/**
 * Class, representing  [StudyGroup.coordinates]
 */
@Serializable
class Coordinates (
    @Pos(31) private val x: Int,
    @Pos(32) private val y: Long
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