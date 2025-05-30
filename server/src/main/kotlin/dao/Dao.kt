package dao

/**
 * Data Access Object (DAO) - interface with all required methods
 */
interface Dao<T>{
    /**
     * Add new entity to related table
     * @return id of added entity
     */
    fun insert(entity: T, userId: Int? = null): Int?

    /**
     * Update entity in related table
     */
    fun update(entity: T, userId: Int? = null): Int?

    /**
     * Remove entity in related table
     */
    fun delete(id: Int)

    /**
     * Get all entities from related table
     * @return [List] of entities
     */
    fun getAll(): List<T>

    /**
     * Get entity by its id
     */
    fun getById(id: Long): T?
}