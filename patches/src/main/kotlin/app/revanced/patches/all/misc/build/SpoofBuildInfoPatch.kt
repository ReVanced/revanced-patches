package app.revanced.patches.all.misc.build

import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.longOption
import app.revanced.patcher.patch.stringOption

@Suppress("unused")
val spoofBuildInfoPatch = bytecodePatch(
    name = "Spoof build info",
    description = "Spoofs the information about the current build.",
    use = false,
) {
    val board by stringOption(
        default = null,
        name = "Board",
        description = "The name of the underlying board, like \"goldfish\".",
    )

    val bootloader by stringOption(
        default = null,
        name = "Bootloader",
        description = "The system bootloader version number.",
    )

    val brand by stringOption(
        default = null,
        name = "Brand",
        description = "The consumer-visible brand with which the product/hardware will be associated, if any.",
    )

    val cpuAbi by stringOption(
        default = null,
        name = "CPU ABI",
        description = "This field was deprecated in API level 21. Use SUPPORTED_ABIS instead.",
    )

    val cpuAbi2 by stringOption(
        default = null,
        name = "CPU ABI 2",
        description = "This field was deprecated in API level 21. Use SUPPORTED_ABIS instead.",
    )

    val device by stringOption(
        default = null,
        name = "Device",
        description = "The name of the industrial design.",
    )

    val display by stringOption(
        default = null,
        name = "Display",
        description = "A build ID string meant for displaying to the user.",
    )

    val fingerprint by stringOption(
        default = null,
        name = "Fingerprint",
        description = "A string that uniquely identifies this build.",
    )

    val hardware by stringOption(
        default = null,
        name = "Hardware",
        description = "The name of the hardware (from the kernel command line or /proc).",
    )

    val host by stringOption(
        default = null,
        name = "Host",
        description = "The host.",
    )

    val id by stringOption(
        default = null,
        name = "ID",
        description = "Either a changelist number, or a label like \"M4-rc20\".",
    )

    val manufacturer by stringOption(
        default = null,
        name = "Manufacturer",
        description = "The manufacturer of the product/hardware.",
    )

    val model by stringOption(
        default = null,
        name = "Model",
        description = "The end-user-visible name for the end product.",
    )

    val odmSku by stringOption(
        default = null,
        name = "ODM SKU",
        description = "The SKU of the device as set by the original design manufacturer (ODM).",
    )

    val product by stringOption(
        default = null,
        name = "Product",
        description = "The name of the overall product.",
    )

    val radio by stringOption(
        default = null,
        name = "Radio",
        description = "This field was deprecated in API level 15. " +
                "The radio firmware version is frequently not available when this class is initialized, " +
                "leading to a blank or \"unknown\" value for this string. Use getRadioVersion() instead.",
    )

    val serial by stringOption(
        default = null,
        name = "Serial",
        description = "This field was deprecated in API level 26. Use getSerial() instead.",
    )

    val sku by stringOption(
        default = null,
        name = "SKU",
        description = "The SKU of the hardware (from the kernel command line).",
    )

    val socManufacturer by stringOption(
        default = null,
        name = "SOC manufacturer",
        description = "The manufacturer of the device's primary system-on-chip.",
    )

    val socModel by stringOption(
        default = null,
        name = "SOC model",
        description = "The model name of the device's primary system-on-chip.",
    )

    val tags by stringOption(
        default = null,
        name = "Tags",
        description = "Comma-separated tags describing the build, like \"unsigned,debug\".",
    )

    val time by longOption(
        default = null,
        name = "Time",
        description = "The time at which the build was produced, given in milliseconds since the UNIX epoch.",
    )

    val type by stringOption(
        default = null,
        name = "Type",
        description = "The type of build, like \"user\" or \"eng\".",
    )

    val user by stringOption(
        default = null,
        name = "User",
        description = "The user.",
    )

    dependsOn(
        baseSpoofBuildInfoPatch {
            BuildInfo(
                board,
                bootloader,
                brand,
                cpuAbi,
                cpuAbi2,
                device,
                display,
                fingerprint,
                hardware,
                host,
                id,
                manufacturer,
                model,
                odmSku,
                product,
                radio,
                serial,
                sku,
                socManufacturer,
                socModel,
                tags,
                time,
                type,
                user,
            )
        },

        )
}
