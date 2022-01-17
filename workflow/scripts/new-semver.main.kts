#!/usr/bin/env kotlin

import java.io.File
import java.nio.file.Files

/***********************************************
 * DESCRIPTION:
 * -----------
 * Will find next patched version when given current snapshot-version and current major/minor-version (using semantic versioning)
 * -
 * Major and minor version will be determined by previous snapshot tag and the patch-version is bumped.
 * -
 * The new patched version (full semantic version, <major>.<minor>.<patch>) will be written to a new file named newSemVer
 * -
 * Note! If the major/minor version is less than current semantic version or larger than bumped semantic version, the script will fail...
 */
object Argument {
    const val MAJOR_MINOR = "majorMinor"
    const val SEMANTIC = "semantic"
}

object Constants {
    const val ENVIRONMENT_IS_DEBUG = "IS_DEBUG"
    const val FILE_NAME_NEW_SEM_VER = "newSemVer"

    private const val SCRIPT_FILE_NAME = "new-semver.main.kts"

    val ERROR_MESSAGE = """
            >>> ERROR <<<
            {}
              Usage: $SCRIPT_FILE_NAME <mapped args, ie: ${Argument.MAJOR_MINOR}=? ${Argument.SEMANTIC}=?>
              - ex: ./$SCRIPT_FILE_NAME ${Argument.MAJOR_MINOR}=2.0 ${Argument.SEMANTIC}=2.0.15
        """.trimIndent()
}

val isDebug = System.getenv(Constants.ENVIRONMENT_IS_DEBUG)?.toBoolean() ?: false
val allArgs = args.joinToString(" ")

debugMessage("all args: $allArgs")

if (args.size < 2) {
    throw IllegalArgumentException(errMsg("Two arguments are required!"))
}

val inputs = allArgs.split(Regex(" "))
val commands: MutableMap<String, String> = HashMap()

inputs.filter { it.contains('=') }.filter { !it.endsWith('=') }.forEach {
    val key = it.split("=")[0]
    val value = it.split("=")[1]
    commands[key] = value
}

debugMessage("map args: $commands")

// read arguments
val majorMinorVersion = commands[Argument.MAJOR_MINOR] ?: throw IllegalArgumentException(errMsg("${Argument.MAJOR_MINOR} argument is not supplied!"))
val semantictVersion = commands[Argument.SEMANTIC] ?: throw IllegalArgumentException(errMsg("${Argument.SEMANTIC} argument is not supplied!"))

val newSemanticVersion = createNewSemanticVersion()
val semVerFile = File(Constants.FILE_NAME_NEW_SEM_VER)

Files.write(semVerFile.toPath(), newSemanticVersion.toByteArray())

fun errMsg(message: String): String = Constants.ERROR_MESSAGE.replace("{}", message)
fun createNewSemanticVersion(): String {
    debugMessage("creating new semantic version from $majorMinorVersion and current semantic version ($semantictVersion)")

    if (!semantictVersion.startsWith(majorMinorVersion)) {
        return createNewSemanticVersion(
            majorMinorVersion.split(".")[0].toInt(),
            majorMinorVersion.split(".")[1].toInt(),
            semantictVersion.split(".")[0].toInt(),
            semantictVersion.split(".")[1].toInt()
        )
    }

    val currentPatch = semantictVersion.substring(semantictVersion.lastIndexOf('.') + 1).toInt()

    return "$majorMinorVersion.${currentPatch + 1}"
}

fun createNewSemanticVersion(majorVersion: Int, minorVersion: Int, currentMajorVersion: Int, currentMinorVersion: Int): String {
    debugMessage(
        """
        supplied major version: $majorVersion
        supplied minor version: $minorVersion
        snapshot major version: $currentMajorVersion
        snapshot minor version: $currentMinorVersion
    """.trimIndent()
    )

    if (majorVersion < currentMajorVersion) {
        throw IllegalStateException("Supplied major/minor version ($majorMinorVersion) is less than current snapshot major version!")
    }

    if (majorVersion == currentMajorVersion && minorVersion < currentMinorVersion) {
        throw IllegalStateException("Supplied major/minor version ($majorMinorVersion) is less than current snapshot major/minor version")
    }

    if (majorVersion > (currentMajorVersion + 1)) {
        throw IllegalStateException("New major version should only be bumped! (new: $majorVersion, current: $currentMajorVersion)")
    }

    if (majorVersion == currentMajorVersion && minorVersion > (currentMinorVersion + 1)) {
        throw IllegalStateException("New minor version should only be bumped! (new: $minorVersion, current: $currentMinorVersion)")
    }

    return "$majorMinorVersion.0"
}

fun debugMessage(message: String) {
    if (isDebug) println(message)
}
