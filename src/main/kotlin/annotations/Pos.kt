package annotations
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Pos(val order: Int)

