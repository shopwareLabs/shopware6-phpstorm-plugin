package de.shyim.shopware6.inspection.quickfix.admin

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.json.JsonFileType
import com.intellij.json.psi.JsonElementGenerator
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonPsiUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import de.shyim.shopware6.util.StringUtil
import org.json.simple.JSONValue

class AddAdministrationSnippetFix : LocalQuickFix {
    override fun getFamilyName() = "Add missing administration snippet"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val text = StringUtil.stripQuotes(descriptor.psiElement.text)

        val snippets = findNearestSnippetFile(descriptor.psiElement?.containingFile?.containingDirectory)
            ?: return Messages.showInfoMessage("Cannot find related snippet file", "Info")
        invokeLater {
            val wrapper = AddAdministrationSnippetFixFormWrapper(text, snippets)
            val cfg = wrapper.showAndGetInfo() ?: return@invokeLater

            CommandProcessor.getInstance().executeCommand(project, {
                ApplicationManager.getApplication().runWriteAction {
                    createSnippets(cfg, snippets, project)
                }
            }, "Add snippet", null)
        }
    }

    private fun createSnippets(
        cfg: AddAdministrationSnippetFixConfig,
        snippets: MutableCollection<PsiFile>,
        project: Project
    ) {
        val psiFileFactory = PsiFileFactory.getInstance(project)
        val jsonBuilder = JsonElementGenerator(project)

        cfg.translations.forEach { (file, content) ->
            snippets.forEach snippet@{ psiFile ->
                if (psiFile.name == file) {
                    val path = cfg.key.split(".")

                    var jsonProp = psiFile.children[0]
                    if (jsonProp !is JsonObject) {
                        return@snippet
                    }

                    for (i in path.indices) {
                        if (i + 1 == path.size) {
                            JsonPsiUtil.addProperty(
                                jsonProp as JsonObject, jsonBuilder.createProperty(
                                    JSONValue.escape(
                                        path[i]
                                    ), "\"${JSONValue.escape(content)}\""
                                ), false
                            )
                        } else {
                            var foundChild: JsonObject? = null
                            jsonProp.children.forEach jsonChildren@{ child ->
                                if (child !is JsonProperty) {
                                    return@jsonChildren
                                }

                                if (child.name == path[i]) {
                                    foundChild = child.lastChild as JsonObject
                                }
                            }

                            jsonProp = if (foundChild != null) {
                                foundChild
                            } else {
                                val element = psiFileFactory.createFileFromText(
                                    "dummy." + JsonFileType.INSTANCE.defaultExtension,
                                    JsonFileType.INSTANCE,
                                    "{\"${path[i]}\": {}}"
                                ).firstChild.children[0] as JsonProperty

                                val newProperty = JsonPsiUtil.addProperty(jsonProp as JsonObject, element, false)
                                newProperty.lastChild
                            }
                        }
                    }
                }
            }
        }
    }

    @Suppress("USELESS_CAST")
    private fun findNearestSnippetFile(containingDirectory: PsiDirectory?): MutableCollection<PsiFile>? {
        if (containingDirectory == null) {
            return null
        }

        var maxTries = 5
        var dir = containingDirectory as PsiDirectory
        val snippetDir: PsiDirectory?

        while (true) {
            if (dir.findSubdirectory("snippet") != null) {
                snippetDir = dir.findSubdirectory("snippet")
                break
            }

            maxTries--

            if (maxTries == 0) {
                return null
            }

            dir = dir.parent!!
        }

        val snippetList: MutableCollection<PsiFile> = ArrayList()

        snippetDir?.files?.forEach { psiFile ->
            if (psiFile.name.endsWith(".json")) {
                snippetList.add(psiFile)
            }
        }

        return snippetList
    }
}