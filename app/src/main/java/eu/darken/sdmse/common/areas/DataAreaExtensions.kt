package eu.darken.sdmse.common.areas

import kotlinx.coroutines.flow.first


val DataArea.Type.isPublic
    get() = DataArea.Type.PUBLIC_LOCATIONS.contains(this)


val DataArea.Type.restrictedCharset: Boolean
    get() = isPublic

/**
 * https://source.android.com/devices/storage/traditional
 */
val DataArea.Type.isCaseInsensitive: Boolean
    get() = isPublic

fun DataArea.hasFlags(vararg lookup: DataArea.Flag): Boolean = flags.containsAll(lookup.toList())

suspend fun DataAreaManager.currentAreas(): Collection<DataArea> = state.first().areas