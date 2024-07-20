package app.revanced.patches.all.misc.build

import app.revanced.patcher.patch.annotation.Patch
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.longPatchOption
import app.revanced.patcher.patch.options.PatchOption.PatchExtensions.stringPatchOption

@Patch(
    name = "Spoof build info",
    description = "Spoof the information about the current build.",
    use = false
)
@Suppress("unused")
class SpoofBuildInfoPatch : BaseSpoofBuildInfoPatch() {
    override val board by stringPatchOption(
        key = "board",
        default = null,
        title = "Board",
        description = "The name of the underlying board, like \"goldfish\"."
    )

    override val bootloader by stringPatchOption(
        key = "bootloader",
        default = null,
        title = "Bootloader",
        description = "The system bootloader version number."
    )

    override val brand by stringPatchOption(
        key = "brand",
        default = null,
        title = "Brand",
        description = "The consumer-visible brand with which the product/hardware will be associated, if any."
    )

    override val cpuAbi by stringPatchOption(
        key = "cpu-abi",
        default = null,
        title = "CPU ABI",
        description = "This field was deprecated in API level 21. Use SUPPORTED_ABIS instead."
    )

    override val cpuAbi2 by stringPatchOption(
        key = "cpu-abi-2",
        default = null,
        title = "CPU ABI 2",
        description = "This field was deprecated in API level 21. Use SUPPORTED_ABIS instead."
    )

    override val device by stringPatchOption(
        key = "device",
        default = null,
        title = "Device",
        description = "The name of the industrial design."
    )

    override val display by stringPatchOption(
        key = "display",
        default = null,
        title = "Display",
        description = "A build ID string meant for displaying to the user."
    )

    override val fingerprint by stringPatchOption(
        key = "fingerprint",
        default = null,
        title = "Fingerprint",
        description = "A string that uniquely identifies this build."
    )

    override val hardware by stringPatchOption(
        key = "hardware",
        default = null,
        title = "Hardware",
        description = "The name of the hardware (from the kernel command line or /proc)."
    )

    override val host by stringPatchOption(
        key = "host",
        default = null,
        title = "Host",
        description = "The host."
    )

    override val id by stringPatchOption(
        key = "id",
        default = null,
        title = "ID",
        description = "Either a changelist number, or a label like \"M4-rc20\"."
    )

    override val manufacturer by stringPatchOption(
        key = "manufacturer",
        default = null,
        title = "Manufacturer",
        description = "The manufacturer of the product/hardware."
    )

    override val model by stringPatchOption(
        key = "model",
        default = null,
        title = "Model",
        description = "The end-user-visible name for the end product."
    )

    override val odmSku by stringPatchOption(
        key = "odm-sku",
        default = null,
        title = "ODM SKU",
        description = "The SKU of the device as set by the original design manufacturer (ODM)."
    )

    override val product by stringPatchOption(
        key = "product",
        default = null,
        title = "Product",
        description = "The name of the overall product."
    )

    override val radio by stringPatchOption(
        key = "radio",
        default = null,
        title = "Radio",
        description = "This field was deprecated in API level 15. " +
                "The radio firmware version is frequently not available when this class is initialized, " +
                "leading to a blank or \"unknown\" value for this string. Use getRadioVersion() instead."
    )

    override val serial by stringPatchOption(
        key = "serial",
        default = null,
        title = "Serial",
        description = "This field was deprecated in API level 26. Use getSerial() instead."
    )

    override val sku by stringPatchOption(
        key = "sku",
        default = null,
        title = "SKU",
        description = "The SKU of the hardware (from the kernel command line)."
    )

    override val socManufacturer by stringPatchOption(
        key = "soc-manufacturer",
        default = null,
        title = "SOC Manufacturer",
        description = "The manufacturer of the device's primary system-on-chip."
    )

    override val socModel by stringPatchOption(
        key = "soc-model",
        default = null,
        title = "SOC Model",
        description = "The model name of the device's primary system-on-chip."
    )

    override val tags by stringPatchOption(
        key = "tags",
        default = null,
        title = "Tags",
        description = "Comma-separated tags describing the build, like \"unsigned,debug\"."
    )

    override val time by longPatchOption(
        key = "time",
        default = null,
        title = "Time",
        description = "The time at which the build was produced, given in milliseconds since the UNIX epoch."
    )

    override val type by stringPatchOption(
        key = "type",
        default = null,
        title = "Type",
        description = "The type of build, like \"user\" or \"eng\"."
    )

    override val user by stringPatchOption(
        key = "user",
        default = null,
        title = "User",
        description = "The user."
    )
}