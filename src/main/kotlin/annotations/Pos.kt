package annotations
import collection.StudyGroup
/**
 * Annotation for sorting properties of [StudyGroup]
 * @param order Number of property in list
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Pos(val order: Int)

