package app.revanced.util

import java.io.File

internal fun main(args: Array<String>) {
    if (args.size != 2) {
        throw RuntimeException("Exactly two arguments are required: <input_file> <output_file>")
    }

    val inputFilePath = args[0]
    val inputFile = File(inputFilePath)
    if (!inputFile.exists()) {
        throw RuntimeException("File not found at path: $inputFilePath")
    }

    // Comment out the non standard <app> and <patch> tags.
    val content = inputFile.readText()
    val tagRegex = """((<app\s+.*>)|(</app>)|(<patch\s+.*>)|(</patch>))""".toRegex()
    val modifiedContent = content.replace(tagRegex, """<!-- $1 -->""")

    // Write modified content to the output file (creates file if it doesn't exist).
    val outputFilePath = args[1]
    val outputFile = File(outputFilePath)
    outputFile.parentFile?.mkdirs()
    outputFile.writeText(modifiedContent)

    println("Preprocessed strings.xml to: $outputFilePath")
}

