package handlers

import GroupData
import annotations.Nested
import collection.StudyGroup
import validators.GroupDataValidator
import validators.PropertyValidator
import kotlin.reflect.KClass

class InsertInputHandler: Handler<GroupData, String> {
    /**
     * Handle user's input on Insert command and creates GroupData
     *
     * @param data A [GroupData] object, contains only ("id", id)
     * @param option class's name to get its properties
     */
    override fun handle(data: GroupData, option: String): StudyGroup? {
        val propertyValidator = PropertyValidator()
        val groupDataValidator = GroupDataValidator()
        val properties = propertyValidator.getProperties(className = option)
        for ((property) in properties) {
            if (property.annotations.contains(Nested())) {
                if (property.name == "groupAdmin") {
                    var input: String
                    do {
                        print("${property.name.replaceFirstChar { it.uppercase() }} (Y/n): ")
                        input = readln()
                    } while (input != "Y" && input != "n")
                    if (input == "Y") handle(data, property.returnType.toString().split("?")[0])
                } else {
                    handle(data, property.returnType.toString().split("?")[0])
                }
            }
            else {
                if (property.name == "id" || property.name == "creationDate") continue
                var input: String
                do {
                    print("${property.name.replaceFirstChar { it.uppercase() }}: ")
                    if ((property.returnType.classifier as? KClass<*>)?.java?.isEnum == true) {
                        val enums = (property.returnType.classifier as? KClass<*>)?.java?.enumConstants?.joinToString(separator = ", ")
                        print("($enums) ")
                    }
                    input = readln()

                } while (!propertyValidator.validateData(Pair(property.name, input)))
                data.add(Pair(property.name, input))
            }
        }
        if (option == "collection.StudyGroup") {
            return groupDataValidator.validateData(data)
        }
        return null
    }
}