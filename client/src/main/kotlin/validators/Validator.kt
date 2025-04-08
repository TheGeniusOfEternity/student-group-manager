package validators

import annotations.Pos
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

/**
 * Interface for validation, has default methods, [getProperties] & [getPropertyNameForValidation], and [validateData] method
 */
interface Validator<T> {
    /**
     * Necessary method for all validators, has different implementation depending on validator
     */
    fun validateData(data: T): Any?

    /**
     * Get all properties with all info from the [className]
     * @param className Name of class, whose properties will be accessed
     * @return Properties List
     */
    fun getProperties(className: String): List<Pair<KProperty1<out Any, *>, Int?>> {
        val properties = Class.forName(className).kotlin.declaredMemberProperties.map { property ->
            val order = property.annotations.find { it is Pos } as? Pos
            property to order?.order
        }.sortedBy { it.second }
        return properties
    }

    /**
     * Get property name by its position in class (depending on [Pos]
     * @param index [Pos.order] value
     * @param className Name of class, whose properties will be accessed
     * @return Property name as string, if found, null if not
     */
    fun getPropertyNameForValidation(index: Int, className: String): KProperty1<out Any, *>? {
        val properties = getProperties(className)
        properties.forEach {
            if (it.second == index) { return it.first }
        }
        return null
    }
}