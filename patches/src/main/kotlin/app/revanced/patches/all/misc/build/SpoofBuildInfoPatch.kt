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
        key = "board",
        default = null,
        title = "Board",
        description = "The name of the underlying board, like \"goldfish\".",
    )

    val bootloader by stringOption(
        key = "bootloader",
        default = null,
        title = "Bootloader",
        description = "The system bootloader version number.",
    )

    val brand by stringOption(
        key = "brand",
        default = null,
        title = "Brand",
        description = "The consumer-visible brand with which the product/hardware will be associated, if any.",
    )

    val cpuAbi by stringOption(
        key = "cpu-abi",
        default = null,
        title = "CPU ABI",
        description = "This field was deprecated in API level 21. Use SUPPORTED_ABIS instead.",
    )

    val cpuAbi2 by stringOption(
        key = "cpu-abi-2",
        default = null,
        title = "CPU ABI 2",
        description = "This field was deprecated in API level 21. Use SUPPORTED_ABIS instead.",
    )

    val device by stringOption(
        key = "device",
        default = null,
        title = "Device",
        description = "The name of the industrial design.",
    )

    val display by stringOption(
        key = "display",
        default = null,
        title = "Display",
        description = "A build ID string meant for displaying to the user.",
    )

    val fingerprint by stringOption(
        key = "fingerprint",
        default = null,
        title = "Fingerprint",
        description = "A string that uniquely identifies this build.",
    )

    val hardware by stringOption(
        key = "hardware",
        default = null,
        title = "Hardware",
        description = "The name of the hardware (from the kernel command line or /proc).",
    )

    val host by stringOption(
        key = "host",
        default = null,
        title = "Host",
        description = "The host.",
    )

    val id by stringOption(
        key = "id",
        default = null,
        title = "ID",
        description = "Either a changelist number, or a label like \"M4-rc20\".",
    )

    val manufacturer by stringOption(
        key = "manufacturer",
        default = null,
        title = "Manufacturer",
        description = "The manufacturer of the product/hardware.",
    )

    val model by stringOption(
        key = "model",
        default = null,
        title = "Model",
        description = "The end-user-visible name for the end product.",
    )

    val odmSku by stringOption(
        key = "odm-sku",
        default = null,
        title = "ODM SKU",
        description = "The SKU of the device as set by the original design manufacturer (ODM).",
    )

    val product by stringOption(
        key = "product",
        default = null,
        title = "Product",
        description = "The name of the overall product.",
    )

    val radio by stringOption(
        key = "radio",
        default = null,
        title = "Radio",
        description = "This field was deprecated in API level 15. " +
            "The radio firmware version is frequently not available when this class is initialized, " +
            "leading to a blank or \"unknown\" value for this string. Use getRadioVersion() instead.",
    )

    val serial by stringOption(
        key = "serial",
        default = null,
        title = "Serial",
        description = "This field was deprecated in API level 26. Use getSerial() instead.",
    )

    val sku by stringOption(
        key = "sku",
        default = null,
        title = "SKU",
        description = "The SKU of the hardware (from the kernel command line).",
    )

    val socManufacturer by stringOption(
        key = "soc-manufacturer",
        default = null,
        title = "SOC manufacturer",
        description = "The manufacturer of the device's primary system-on-chip.",
    )

    val socModel by stringOption(
        key = "soc-model",
        default = null,
        title = "SOC model",
        description = "The model name of the device's primary system-on-chip.",
    )

    val tags by stringOption(
        key = "tags",
        default = null,
        title = "Tags",
        description = "Comma-separated tags describing the build, like \"unsigned,debug\".",
    )

    val time by longOption(
        key = "time",
        default = null,
        title = "Time",
        description = "The time at which the build was produced, given in milliseconds since the UNIX epoch.",
    )

    val type by stringOption(
        key = "type",
        default = null,
        title = "Type",
        description = "The type of build, like \"user\" or \"eng\".",
    )

    val user by stringOption(
        key = "user",
        default = null,
        title = "User",
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
