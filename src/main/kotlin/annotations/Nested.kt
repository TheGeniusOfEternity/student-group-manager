package annotations
import collection.StudyGroup
/**
 * Annotation for nested properties like [StudyGroup.coordinates] & [StudyGroup.groupAdmin]
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Nested()
