package de.shyim.shopware6.index

import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import de.shyim.shopware6.index.dict.AdminModule
import de.shyim.shopware6.index.dict.AdminModuleRoute
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer
import de.shyim.shopware6.util.JavaScriptPattern
import de.shyim.shopware6.util.StringUtil

class AdminModuleIndex : FileBasedIndexExtension<String, AdminModule>() {
    private val _externalizer = ObjectStreamDataExternalizer<AdminModule>()

    override fun getName(): ID<String, AdminModule> {
        return key
    }

    override fun getVersion(): Int {
        return 3
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getIndexer(): DataIndexer<String, AdminModule, FileContent> {
        return DataIndexer { inputData ->
            val modules = HashMap<String, AdminModule>()

            if (inputData.file.parent.parent.name != "module") {
                return@DataIndexer modules
            }

            inputData.psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (JavaScriptPattern.getModuleBodyPattern()
                            .accepts(element) && element is JSObjectLiteralExpression
                    ) {
                        if (element.parent.children[1] !is JSLiteralExpression) {
                            return
                        }

                        val module =
                            AdminModule(StringUtil.stripQuotes(element.parent.children[1].text), inputData.file.path)
                        modules[module.name] = module

                        element.findProperty("routes")?.value?.children?.forEach { routeProperty ->
                            if (routeProperty is JSProperty) {
                                buildRoute(module, routeProperty, module.name.replace("-", "."))
                            }
                        }
                    }

                    super.visitElement(element)
                }
            })

            return@DataIndexer modules
        }
    }

    private fun buildRoute(module: AdminModule, routeProperty: JSProperty, path: String) {
        if (routeProperty.value !is JSObjectLiteralExpression) {
            return
        }

        val routePropertyObject = routeProperty.value as JSObjectLiteralExpression
        var componentName = ""
        val component = routePropertyObject.findProperty("component")?.value
        val components = routePropertyObject.findProperty("components")?.value
        if (component is JSLiteralExpression) {
            componentName = StringUtil.stripQuotes(component.text)
        } else if (components is JSObjectLiteralExpression) {
            val default = components.findProperty("default")

            if (default?.value is JSLiteralExpression) {
                componentName = StringUtil.stripQuotes((default.value as JSLiteralExpression).text)
            }
        }

        if (componentName.isNotEmpty()) {
            val key = "${path}.${routeProperty.name}"
            module.routes[key] = AdminModuleRoute(key, componentName)
        }

        if (routePropertyObject.findProperty("children") != null) {
            (routePropertyObject.findProperty("children") as JSProperty).value?.children?.forEach {
                if (it is JSProperty) {
                    buildRoute(module, it, "${path}.${routeProperty.name}")
                }
            }
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<AdminModule> {
        return _externalizer
    }

    companion object {
        val key = ID.create<String, AdminModule>("de.shyim.shopware6.admin.module")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(JavaScriptFileType.INSTANCE) {
        }
    }
}