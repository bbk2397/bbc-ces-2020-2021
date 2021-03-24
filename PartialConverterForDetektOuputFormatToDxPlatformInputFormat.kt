import java.lang.ProcessBuilder.Redirect
import java.util.concurrent.TimeUnit
import java.io.File
// import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper


data class DxPlatformProperty(
    val file: String,
    val name: String,
    val category: String,
    val value: Int)

// TODO make it so that acccepts only one path!
fun main(args: Array<String>) {
    if ( args.size == 1 ) {
        println("The given argument is:")
        println("  ${args[0]}")
        println("So, the provided comma separated paths are considered to be these ones.")

        if (arePathsValid(args[0]))
            treatAllPathsAreValid(args[0])
        else
            treatNotAllPathsAreValid(args[0]) 
    }
    else
        treatWrongNumberOfArguments(args)
}

fun String.runCommand(workingDir: File) {
    ProcessBuilder(*split(" ").toTypedArray())
                .directory(workingDir)
                .redirectOutput(Redirect.INHERIT)
                .redirectError(Redirect.INHERIT)
                .start()
                .waitFor(60, TimeUnit.MINUTES)
}

fun generateJSON(commaSeparatedPaths: String) {
    // TODO make it be like: detekt-output-as-dx-platform-input-property-file-<YYYYMMDDhhmmssmsms>.json
    val DIRECTORY = "dx-platform_properties"
    val FILE = "b.json";
    val directory = File(DIRECTORY);
    if( !directory.exists() ) {
        directory.mkdir()
        println("The directory ${DIRECTORY} was created.")
    }

    val OUTPUT_FILE_PATH = DIRECTORY + "/" + FILE
    val jsonOutputFile = File(OUTPUT_FILE_PATH)
    val REPORT_PATH = "./tmp/txt_report"
    

    runCommand(commaSeparatedPaths, REPORT_PATH)

    var numberOfLines: Int = 0
    File(REPORT_PATH).forEachLine {  numberOfLines++ }
    jsonOutputFile.printWriter().use { fileWriter ->
        fileWriter.println("[")
        var currentNumberOfLines: Int = 0
        File(REPORT_PATH).forEachLine { line ->
            currentNumberOfLines++
            val dxPlatformProperty = buildDxPlatformProperty(line, commaSeparatedPaths)

            fileWriter.println("  {")
            fileWriter.println("    \"file\": \"${dxPlatformProperty.file}\",")
            fileWriter.println("    \"name\": \"${dxPlatformProperty.name}\",")
            fileWriter.println("    \"category\": \"${dxPlatformProperty.category}\",")
            fileWriter.println("    \"value\": ${dxPlatformProperty.value}")
            fileWriter.print("  }")
            if( currentNumberOfLines < numberOfLines )
                fileWriter.println(",")
            else 
                fileWriter.println()
        }
        fileWriter.println("]")
    }
}

fun runCommand(commaSeparatedPaths: String, reportPath: String) {
    val reportValue = "txt:${reportPath}"
    val inputValue = "${commaSeparatedPaths}"
    val command = "./detekt-cli-1.16.0/bin/detekt-cli --all-rules --parallel -r ${reportValue} -i ${inputValue} --base-path /home/username/Desktop/upt/2020-2021/sem2/ces/proiect_de_predat/partial_converter_for_detekt_ouput_format_to_dx-platform_input_format"

    command.runCommand(File("./"))

    println("The command below was executed!")
    println("${command}")
    println()
}

fun buildDxPlatformProperty(line: String, path: String): DxPlatformProperty {
    // regexes
    val hyphen = "( - )"
    val at = "( at )"
    val escapedLeftBracket = Regex.escape("[")
    val escapedRightBracket = Regex.escape("]")
    val bracket = "(" + escapedLeftBracket + ".*" + escapedRightBracket + ")"
    val end = "(:[0-9]*:[0-9]* - Signature=.*)"
    val firstSplitString = end + "|" + at + "|" + hyphen
    val firstSplitRegex = firstSplitString.toRegex()
    val fileRegex = "/.*".toRegex()
    val stringArrayAfterFirstSplit = line.split(firstSplitRegex).toTypedArray()
    val currentPathRegex = (System.getProperty("user.dir") + "/" + path + "/").toRegex()
    
    val numberOfNumberRegex = ".*/.*".toRegex()
    val slashRegex = "/".toRegex()
    
    println()
    println()
    println(line)
    val DEFAULT_MINIMUM_FOR_OUTPUT = -1
    var actualNumber: Int = 1
    var minimumForOutput: Int = DEFAULT_MINIMUM_FOR_OUTPUT
    var fileName: String = ""
    var detektNameIssue: String = ""
    for (element in stringArrayAfterFirstSplit) {
        if(!element.equals("") && !element.matches(bracket.toRegex())) {
            println(element)
        	if(element.matches(fileRegex)) {
            	println("file: ${element}")
                fileName = element.split(currentPathRegex).toTypedArray()[1]
            }
            else if(element.matches(numberOfNumberRegex)) {
                val numbers = element.split(slashRegex).toTypedArray()
            	println("actual: ${numbers[0]}")
                println("minimumForOutput: ${numbers[1]}")
                
                actualNumber = numbers[0].toInt()
                minimumForOutput = numbers[1].toInt()
            }
            else {
            	println("detekt name issue: ${element}")
                detektNameIssue = element
            }
        }
    }
 
    if(minimumForOutput > DEFAULT_MINIMUM_FOR_OUTPUT) {
        detektNameIssue += minimumForOutput
    }
    
    val dxPlatformProperty = DxPlatformProperty(fileName, detektNameIssue, "issues-by-detekt-1.16.0", actualNumber)
    println("dxPlatformmProperty:")
    println(dxPlatformProperty)
    println()
    println()
    return dxPlatformProperty
}

fun treatAllPathsAreValid(commaSeparatedPaths: String) {
    println("The comma separated paths ${commaSeparatedPaths} are considered all valid paths.")
    println("A .json file that can be used as Property File for DX-Platform will be generated in:")
    println("./dx-platform_properties")
    
    generateJSON(commaSeparatedPaths)
}

fun treatNotAllPathsAreValid(commaSeparatedPaths: String) {
    println("The comma separated paths are")
    println("    ${commaSeparatedPaths}")
    println("They are considered invalid paths. The program will stop.")
}

fun arePathsValid(paths: String): Boolean {
    return true
}

fun treatWrongNumberOfArguments(args: Array<String>) {
    println()
    println()
    println("Error: undefined behavior!")
    println()
    if (args.size == 0)
        println("The program has 0 arguments!")
    else if ( args.size > 1 )
        println("More than one argument was given!")

    println("Therefore, the program stops. Please read the precondions below.")
    println()
    printPreconditions()
}

fun printPreconditions() {
    println("For this program to work, these are the preconditions:")
    printPrecondition1()
    printPrecondition2()
}

fun printPrecondition1() {
    println("1. There should be provided exactly one argument.")
}

fun printPrecondition2() {
    println("2. The provided argument should be a valid relative path to the directory to be analyzed by the Detekt static analyzer.")
}