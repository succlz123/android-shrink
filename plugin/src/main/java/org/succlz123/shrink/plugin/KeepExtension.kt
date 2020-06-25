package org.succlz123.shrink.plugin

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import java.util.regex.Pattern

open class KeepExtension(project: Project) {
    var keepInfo: NamedDomainObjectContainer<KeepRInfo>
    var keepSortInfo: Map<String, List<KeepRInfo>>? = null

    init {
        keepInfo = project.container(KeepRInfo::class.java)
    }

    fun keepInfo(action: Action<NamedDomainObjectContainer<KeepRInfo>>) {
        action.execute(keepInfo)
        for (keepRInfo in keepInfo) {
            keepRInfo.resNamePattern = keepRInfo.resNameReg.map { Pattern.compile(it) }
        }
        keepSortInfo = keepInfo.groupBy { it.packageName.orEmpty() }
    }

    fun shouldKeepRFile(packageName: String): List<KeepRInfo>? {
        return keepSortInfo?.get(packageName)
    }

    class KeepRInfo {
        // "anim", "array", "attr", "bool", "color", "dimen",
        // "drawable", "id", "integer", "layout", "menu", "plurals", "string", "style", "styleable"
        var name: String? = null

        // android.fragment
        var packageName: String? = null

        // activity_second
        var resName = ArrayList<String>()

        // accessibility_.*
        var resNameReg = ArrayList<String>()

        var resNamePattern: List<Pattern>? = null

        var rClassString: String? = null
            get() {
                if (field == null) {
                    field = "${packageName?.replace(".", "/")}/R\$${name}"
                }
                return field
            }

        constructor(name: String) {
            this.name = name
        }

        fun shouldKeep(fieldName: String?): Boolean {
            fieldName ?: return false
            for (s in resName) {
                if (s == fieldName) {
                    return true
                }
            }
            resNamePattern?.let {
                for (p in it) {
                    if (p.matcher(fieldName).matches()) {
                        return true
                    }
                }
            }
            return false
        }

        // --- pn = org.succlz123.nrouter.app.test, classInnerName = layout, RClass = org/succlz123/nrouter/app/test/R$layout ---
        fun printInfo() {
            println("--- R.class keep info ---")
            println("--- pn = ${packageName}, classInnerName = $name, RClass = $rClassString ---")
            resName.forEach {
                println("--- resName: $it ---")
            }
        }
    }
}
