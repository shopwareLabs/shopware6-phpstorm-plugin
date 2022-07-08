package de.shyim.shopware6.inspection.store.composer

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonFile

abstract class ExtensionComposerInspection : LocalInspectionTool() {
    protected fun isExtensionComposerFile(holder: ProblemsHolder): Boolean {
        if (holder.file !is JsonFile) {
            return false
        }

        if (holder.file.name != "composer.json") {
            return false
        }

        if (!isInsideCustomPlugins(holder)) {
            return false
        }

        return holder.file.text!!.contains("shopware-platform-plugin")
    }

    private fun isInsideCustomPlugins(holder: ProblemsHolder): Boolean {
        return holder.file.virtualFile.path.startsWith("${holder.project.basePath}/custom/plugins");
    }
}