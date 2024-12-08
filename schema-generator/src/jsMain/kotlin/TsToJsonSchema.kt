import kotlinx.io.Buffer
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import kotlin.js.json

fun main() {
    println("Running!")
    val buildDir = Path("../../../")
    // By default, runs in build/js/packages/<project name>
    val jsonString =
        JSON.stringify(TJS.createGenerator(config(Path(buildDir, "schemas/spawns.ts"))).createSchema("SpawnEntryFile"))
    val out = SystemFileSystem.sink(Path(buildDir, "schemas/test.json"))
    val buffer = Buffer().apply {
        writeString(jsonString)
    }

    out.write(buffer, buffer.size)
}

fun config(path: Path) = json(
    "path" to path.toString(),
    "type" to "*"
)
