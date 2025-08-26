package collection

import annotations.Pos
import kotlinx.serialization.Serializable

/**
 * Class, representing  [StudyGroup.coordinates]
 */
@Serializable
class Coordinates (
    @Pos(31) private var x: Int,
    @Pos(32) private var y: Long
) {
    init {
        require(x > -357)
    }


    fun getX() = x
    fun getY() = y

    /**
     * @return Coordinates string representation
     */
    override fun toString(): String {
        return "($x, $y)"
    }
}