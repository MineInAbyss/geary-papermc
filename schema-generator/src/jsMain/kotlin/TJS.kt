import kotlin.js.Json

@JsModule("ts-json-schema-generator")
@JsNonModule
external object TJS {
    fun createGenerator(config: Json): SchemaGenerator
}

@JsModule("ts-json-schema-generator")
@JsNonModule
external class SchemaGenerator {
    fun createSchema(forClass: String): Json
}
