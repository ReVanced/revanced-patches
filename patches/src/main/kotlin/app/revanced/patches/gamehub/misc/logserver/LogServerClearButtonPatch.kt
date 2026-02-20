package app.revanced.patches.gamehub.misc.logserver

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstructionOrThrow
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference

@Suppress("unused")
val logServerClearButtonPatch = bytecodePatch(
    name = "Log server clear button",
    description = "Adds a Clear Logs button to the WinEmu log HTTP server page.",
) {
    compatibleWith("com.xiaoji.egggame"("5.3.5"))

    execute {
        logHttpServerPageFingerprint.method.apply {
            val htmlIndex = indexOfFirstInstructionOrThrow {
                opcode == Opcode.CONST_STRING &&
                    getReference<StringReference>()?.string?.contains("WinEmu Log Server") == true
            }

            val instruction = getInstruction(htmlIndex)
            val register = (instruction as OneRegisterInstruction).registerA
            val originalHtml = instruction.getReference<StringReference>()!!.string

            val modifiedHtml = originalHtml
                .replace(
                    ".header {\n" +
                        "            color: #ffffff;\n" +
                        "            border-bottom: 1px solid #333;\n" +
                        "            padding-bottom: 10px;\n" +
                        "            margin-bottom: 20px;\n" +
                        "        }",
                    ".header {\n" +
                        "            color: #ffffff;\n" +
                        "            border-bottom: 1px solid #333;\n" +
                        "            padding-bottom: 10px;\n" +
                        "            margin-bottom: 20px;\n" +
                        "            display: flex;\n" +
                        "            align-items: center;\n" +
                        "        }\n" +
                        "        .clear-btn {\n" +
                        "            margin-left: auto;\n" +
                        "            padding: 4px 10px;\n" +
                        "            cursor: pointer;\n" +
                        "            background: #e74c3c;\n" +
                        "            color: #fff;\n" +
                        "            border: none;\n" +
                        "            border-radius: 4px;\n" +
                        "            font-size: 13px;\n" +
                        "        }\n" +
                        "        .clear-btn:hover {\n" +
                        "            background: #c0392b;\n" +
                        "        }",
                )
                .replace(
                    "<h1>WinEmu Log Server</h1>\n    </div>",
                    "<h1>WinEmu Log Server</h1>\n" +
                        "        <button class=\"clear-btn\" " +
                        "onclick=\"document.querySelector('.log-container').innerHTML=''\">Clear Logs</button>\n" +
                        "    </div>",
                )

            check(modifiedHtml != originalHtml) { "HTML replacement patterns did not match" }

            val escapedHtml = modifiedHtml
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")

            replaceInstruction(htmlIndex, "const-string v$register, \"$escapedHtml\"")
        }
    }
}
